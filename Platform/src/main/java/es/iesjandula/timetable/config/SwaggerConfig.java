package es.iesjandula.timetable.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API de Horarios - IES Jándula")
                        .version("1.0")
                        .description("Microservicio para la gestión de horarios del profesorado"))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Servidor Local"),
                        new Server()
                                .url("https://sweet-chebyshev.194-164-171-3.plesk.page")
                                .description("Servidor de Producción")
                ));
    }
}
