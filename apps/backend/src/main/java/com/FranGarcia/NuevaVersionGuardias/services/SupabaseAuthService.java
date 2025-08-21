package com.FranGarcia.NuevaVersionGuardias.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.FranGarcia.NuevaVersionGuardias.config.SupabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class SupabaseAuthService {

    @Autowired
    private SupabaseConfig supabaseConfig;

    private final WebClient webClient;

    public SupabaseAuthService() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Valida un token JWT de Supabase
     */
    public boolean validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(supabaseConfig.getAnonKey());
            JWTVerifier verifier = JWT.require(algorithm).build();
            
            DecodedJWT jwt = verifier.verify(token);
            return jwt != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extrae información del usuario del token
     */
    public Map<String, Object> getUserFromToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return Map.of(
                "email", jwt.getClaim("email").asString(),
                "sub", jwt.getClaim("sub").asString(),
                "role", jwt.getClaim("role").asString()
            );
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Verifica si un usuario existe en Supabase
     */
    public Mono<Boolean> userExists(String email) {
        return webClient.get()
            .uri(supabaseConfig.getUrl() + "/auth/v1/admin/users")
            .header("Authorization", "Bearer " + supabaseConfig.getServiceRoleKey())
            .header("apikey", supabaseConfig.getServiceRoleKey())
            .retrieve()
            .bodyToMono(String.class)
            .map(response -> response.contains(email))
            .onErrorReturn(false);
    }
}
