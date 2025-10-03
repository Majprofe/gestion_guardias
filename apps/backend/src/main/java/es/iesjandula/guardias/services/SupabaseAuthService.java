package es.iesjandula.guardias.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.HashMap;

/**
 * Servicio para integración con Supabase Auth
 */
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class SupabaseAuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(SupabaseAuthService.class);
    
    @Value("${supabase.url:}")
    private String supabaseUrl;
    
    @Value("${supabase.key:}")
    private String supabaseKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    /**
     * Valida un token de Supabase
     */
    public boolean validarToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("apikey", supabaseKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/user",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getStatusCode() == HttpStatus.OK;
            
        } catch (Exception e) {
            logger.warn("Error validando token de Supabase: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene información del usuario desde Supabase
     */
    public Map<String, Object> obtenerUsuario(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            headers.set("apikey", supabaseKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                supabaseUrl + "/auth/v1/user",
                HttpMethod.GET,
                entity,
                Map.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            }
            
        } catch (Exception e) {
            logger.error("Error obteniendo usuario de Supabase: {}", e.getMessage());
        }
        
        return new HashMap<>();
    }
    
    /**
     * Extrae el email del usuario desde el token
     */
    public String extraerEmailDeToken(String token) {
        Map<String, Object> usuario = obtenerUsuario(token);
        return (String) usuario.get("email");
    }
    
    /**
     * Verifica si un usuario es administrador
     */
    public boolean esAdministrador(String email) {
        // Por ahora, considera administradores a los emails que contengan "admin"
        // En una implementación real, esto debería consultar una base de datos
        return email != null && email.toLowerCase().contains("admin");
    }
}
