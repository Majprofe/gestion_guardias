package es.iesjandula.guardias.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@Tag(name = "Sistema", description = "Endpoints públicos del sistema")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Estado del sistema", description = "Verifica el estado de salud del servicio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente")
    })
    public Map<String, Object> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("service", "guardias-backend");
        response.put("version", "1.0.0");
        return response;
    }

    @GetMapping("/simple")
    @Operation(summary = "Test simple", description = "Endpoint simple para verificar que el backend responde")
    public ResponseEntity<Map<String, String>> simple() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Backend funcionando correctamente");
        response.put("status", "OK");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/info")
    @Operation(summary = "Información del sistema", description = "Información general sobre el servicio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información obtenida correctamente")
    })
    public Map<String, Object> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Sistema de Gestión de Guardias");
        response.put("description", "Backend para gestión de ausencias y guardias del profesorado");
        response.put("version", "1.0.0");
        response.put("environment", "development");
        response.put("swagger", "http://localhost:8081/swagger-ui.html");
        return response;
    }
}
