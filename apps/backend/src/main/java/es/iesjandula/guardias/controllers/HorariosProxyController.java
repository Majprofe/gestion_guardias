package es.iesjandula.guardias.controllers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * Controlador proxy para centralizar llamadas al backend de horarios
 * Permite que el frontend solo trabaje con un endpoint (8081)
 */
@RestController("Horarios Proxy")
@RequestMapping("/api/horarios")
public class HorariosProxyController {

    @Value("${app.horarios.api.url:http://localhost:8082}")
    private String horariosApiUrl;

    private final RestTemplate restTemplate;

    public HorariosProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        System.out.println("=== DEBUG: HorariosProxyController inicializado");
        System.out.println("=== DEBUG: RestTemplate: " + (restTemplate != null ? "OK" : "NULL"));
        System.out.println("=== DEBUG: horariosApiUrl será: " + horariosApiUrl);
    }

    @PostConstruct
    public void init() {
        System.out.println("=== DEBUG: PostConstruct ejecutado");
        System.out.println("=== DEBUG: horariosApiUrl final: " + horariosApiUrl);
    }

    /**
     * Obtener todas las actividades
     * GET /api/horarios/actividades
     */
    @GetMapping("/actividades")
    public ResponseEntity<?> getActividades() {
        try {
            String url = horariosApiUrl + "/actividades";
            System.out.println("=== DEBUG: Proxy llamando a: " + url);
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            System.out.println("=== DEBUG: Respuesta exitosa del backend horarios");
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            System.out.println("=== DEBUG: Error en proxy: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", 
                            "details", e.getMessage(),
                            "type", e.getClass().getSimpleName()));
        }
    }

    /**
     * Obtener todas las asignaturas
     * GET /api/horarios/asignaturas
     */
    @GetMapping("/asignaturas")
    public ResponseEntity<?> getAsignaturas() {
        try {
            String url = horariosApiUrl + "/asignaturas";
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener todas las aulas
     * GET /api/horarios/aulas
     */
    @GetMapping("/aulas")
    public ResponseEntity<?> getAulas() {
        try {
            String url = horariosApiUrl + "/aulas";
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener todos los grupos
     * GET /api/horarios/grupos
     */
    @GetMapping("/grupos")
    public ResponseEntity<?> getGrupos() {
        try {
            String url = horariosApiUrl + "/grupos";
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener horario por ID de profesor
     * GET /api/horarios/horario/{id}
     */
    @GetMapping("/horario/{id}")
    public ResponseEntity<?> getHorarioByIdProfesor(@PathVariable String id) {
        try {
            String url = horariosApiUrl + "/horario/" + id;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener tramos horarios
     * GET /api/horarios/tramohorarios
     */
    @GetMapping("/tramohorarios")
    public ResponseEntity<?> getTramosHorario() {
        try {
            String url = horariosApiUrl + "/tramohorarios";
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener horario de profesor por ID y día
     * GET /api/horarios/horario/profesor/{id}/dia/{diaSemana}
     */
    @GetMapping("/horario/profesor/{id}/dia/{diaSemana}")
    public ResponseEntity<?> getHorarioProfesorPorDia(
            @PathVariable String id, 
            @PathVariable String diaSemana) {
        try {
            String url = horariosApiUrl + "/horario/profesor/" + id + "/dia/" + diaSemana;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener guardias por día y hora
     * GET /api/horarios/horario/guardia/dia/{diaSemana}/hora/{hora}
     */
    @GetMapping("/horario/guardia/dia/{diaSemana}/hora/{hora}")
    public ResponseEntity<?> getGuardiasPorDiaYHora(
            @PathVariable String diaSemana, 
            @PathVariable String hora) {
        try {
            String url = horariosApiUrl + "/horario/guardia/dia/" + diaSemana + "/hora/" + hora;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Obtener profesor por email
     * GET /api/horarios/horario/profesor/email?email={email}
     */
    @GetMapping("/horario/profesor/email")
    public ResponseEntity<?> getProfesorPorEmail(@RequestParam String email) {
        try {
            String url = horariosApiUrl + "/horario/profesor/email?email=" + email;
            ResponseEntity<Object> response = restTemplate.getForEntity(url, Object.class);
            return ResponseEntity.ok(response.getBody());
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Error conectando con servicio de horarios", "details", e.getMessage()));
        }
    }

    /**
     * Endpoint de test simple para verificar que el proxy funciona
     * GET /api/horarios/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        try {
            return ResponseEntity.ok("Test OK - Proxy funcionando");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error en test: " + e.getMessage());
        }
    }

    /**
     * Endpoint de salud para verificar conectividad con horarios
     * GET /api/horarios/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        try {
            // Probamos con un endpoint que sí existe (actividades)
            String url = horariosApiUrl + "/actividades";
            restTemplate.getForEntity(url, List.class);
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "horariosService", "UP",
                "horariosUrl", horariosApiUrl,
                "message", "Conexión con servicio de horarios OK"
            ));
        } catch (RestClientException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                    "status", "DOWN",
                    "horariosService", "DOWN",
                    "horariosUrl", horariosApiUrl,
                    "error", "No se puede conectar con el servicio de horarios",
                    "details", e.getMessage()
                ));
        }
    }
}
