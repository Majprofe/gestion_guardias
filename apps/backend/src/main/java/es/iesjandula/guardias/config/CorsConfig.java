package es.iesjandula.guardias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración CORS para permitir peticiones desde el frontend.
 * Define las reglas de Cross-Origin Resource Sharing para la aplicación.
 */
@Configuration
public class CorsConfig {

    /**
     * Configura las reglas CORS para toda la aplicación.
     * Permite peticiones desde el frontend local y desde el dominio de producción.
     *
     * @return Configurador de CORS para toda la aplicación.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                                "http://localhost:5500",  // Desarrollo local
                                "http://localhost:5173",  // Vite dev server
                                "http://localhost:3000",  // Posible puerto alternativo
                                "https://gestion-guardias.iesjandula.es" // Producción
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                        .allowedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // Cache preflight por 1 hora
            }
        };
    }
}
