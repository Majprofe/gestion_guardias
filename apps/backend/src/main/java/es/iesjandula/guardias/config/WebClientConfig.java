package es.iesjandula.guardias.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para WebClient utilizado en integraciones entre servicios.
 * Permite comunicación entre backend de guardias y backend de horarios.
 */
@Configuration
public class WebClientConfig {

    /**
     * Bean WebClient para hacer llamadas HTTP a otros servicios.
     * Configurado con timeout y headers básicos.
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024)) // 1MB buffer
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("User-Agent", "GestionGuardias-Backend/1.0");
    }
}
