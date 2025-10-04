package es.iesjandula.timetable.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Configuración CORS para permitir peticiones desde el frontend
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        
        // Permitir credenciales
        config.setAllowCredentials(true);
        
        // Orígenes permitidos (frontend en diferentes puertos)
        config.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*",
            "http://127.0.0.1:*"
        ));
        
        // Headers permitidos
        config.setAllowedHeaders(Arrays.asList("*"));
        
        // Métodos HTTP permitidos
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Headers expuestos en la respuesta
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        // Aplicar configuración a todas las rutas
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
