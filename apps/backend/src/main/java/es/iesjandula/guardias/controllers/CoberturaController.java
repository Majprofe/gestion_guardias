package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.CoberturaValidacionRequest;
import es.iesjandula.guardias.models.Cobertura;
import es.iesjandula.guardias.services.CoberturaAsignacionService;
import es.iesjandula.guardias.services.CoberturaCalculoService;
import es.iesjandula.guardias.services.CoberturaValidacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador para gestión y validación de coberturas.
 * Incluye endpoints para:
 * - Validar coberturas (administrador)
 * - Cancelar coberturas (administrador)
 * - Reasignar coberturas de un día completo
 */
@RestController
@RequestMapping("/api/coberturas")
@Tag(name = "Coberturas", description = "Gestión y validación de coberturas de guardias")
public class CoberturaController {

    private static final Logger logger = LoggerFactory.getLogger(CoberturaController.class);

    private final CoberturaValidacionService validacionService;
    private final CoberturaAsignacionService asignacionService;
    private final CoberturaCalculoService calculoService;

    public CoberturaController(CoberturaValidacionService validacionService,
                              CoberturaAsignacionService asignacionService,
                              CoberturaCalculoService calculoService) {
        this.validacionService = validacionService;
        this.asignacionService = asignacionService;
        this.calculoService = calculoService;
    }

    /**
     * Valida una cobertura específica e incrementa el contador del profesor
     * Solo puede ser ejecutado por un administrador
     */
    @PostMapping("/{id}/validar")
    @Operation(
        summary = "Validar cobertura",
        description = "Valida una cobertura realizada e incrementa el contador del profesor según el tipo de guardia. " +
                     "Solo puede ser ejecutado por un administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cobertura validada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cobertura no encontrada"),
        @ApiResponse(responseCode = "409", description = "La cobertura ya está validada o cancelada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> validarCobertura(
            @Parameter(description = "ID de la cobertura a validar", required = true)
            @PathVariable Long id,
            @RequestBody CoberturaValidacionRequest request) {
        
        logger.info("Solicitud de validación de cobertura {} por admin: {}", id, request.getAdminEmail());

        try {
            Cobertura cobertura = validacionService.validarCobertura(id, request.getAdminEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cobertura validada exitosamente",
                "cobertura", Map.of(
                    "id", cobertura.getId(),
                    "profesorCubreEmail", cobertura.getProfesorCubreEmail(),
                    "tipoGuardia", cobertura.getTipoGuardia(),
                    "estado", cobertura.getEstado(),
                    "fechaValidacion", cobertura.getFechaValidacion()
                )
            ));
        } catch (RuntimeException e) {
            logger.error("Error validando cobertura {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Cancela una cobertura (no se incrementa el contador)
     * Solo puede ser ejecutado por un administrador
     */
    @PostMapping("/{id}/cancelar")
    @Operation(
        summary = "Cancelar cobertura",
        description = "Cancela una cobertura que no se realizó. No se incrementan contadores. " +
                     "Solo puede ser ejecutado por un administrador."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cobertura cancelada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Cobertura no encontrada"),
        @ApiResponse(responseCode = "409", description = "La cobertura ya está validada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> cancelarCobertura(
            @Parameter(description = "ID de la cobertura a cancelar", required = true)
            @PathVariable Long id,
            @RequestBody CoberturaValidacionRequest request) {
        
        logger.info("Solicitud de cancelación de cobertura {} por admin: {}", id, request.getAdminEmail());

        try {
            Cobertura cobertura = validacionService.cancelarCobertura(
                    id, request.getAdminEmail(), request.getObservaciones());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cobertura cancelada exitosamente",
                "cobertura", Map.of(
                    "id", cobertura.getId(),
                    "profesorCubreEmail", cobertura.getProfesorCubreEmail(),
                    "estado", cobertura.getEstado()
                )
            ));
        } catch (RuntimeException e) {
            logger.error("Error cancelando cobertura {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Valida todas las coberturas de una fecha específica
     * Útil para validaciones masivas al final del día
     */
    @PostMapping("/validar-dia")
    @Operation(
        summary = "Validar coberturas del día",
        description = "Valida todas las coberturas asignadas de una fecha específica. " +
                     "Útil para validaciones masivas al final del día."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coberturas validadas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> validarCoberturasDelDia(
            @Parameter(description = "Fecha de las coberturas a validar", required = true, example = "2025-10-03")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestBody CoberturaValidacionRequest request) {
        
        logger.info("Solicitud de validación masiva para fecha {} por admin: {}", fecha, request.getAdminEmail());

        try {
            int validadas = validacionService.validarCoberturasDelDia(fecha, request.getAdminEmail());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Coberturas validadas exitosamente",
                "coberturasValidadas", validadas,
                "fecha", fecha
            ));
        } catch (Exception e) {
            logger.error("Error validando coberturas del día {}: {}", fecha, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Reasigna todas las coberturas de un día específico y horas específicas
     * Se usa cuando hay nuevas ausencias que requieren redistribuir profesores
     */
    @PostMapping("/reasignar")
    @Operation(
        summary = "Reasignar coberturas",
        description = "Reasigna todas las coberturas de una fecha y horas específicas. " +
                     "Útil cuando se agregan nuevas ausencias durante el día."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Coberturas reasignadas exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Map<String, Object>> reasignarCoberturas(
            @Parameter(description = "Fecha de las coberturas a reasignar", required = true, example = "2025-10-03")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Lista de horas a reasignar", required = true, 
                      schema = @Schema(example = "[1,2,3]"))
            @RequestParam List<Integer> horas) {
        
        logger.info("Solicitud de reasignación de coberturas para fecha {} horas {}", fecha, horas);

        try {
            asignacionService.reasignarCoberturasDelDia(fecha, horas);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Coberturas reasignadas exitosamente",
                "fecha", fecha,
                "horas", horas
            ));
        } catch (Exception e) {
            logger.error("Error reasignando coberturas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    /**
     * Calcula dinámicamente qué profesor debe estar en el aula de convivencia
     * para una fecha y hora específica, sin persistir la asignación en BD.
     * 
     * Se utiliza para mostrar en el tablón quién cubre convivencia en horas
     * donde no hay ausencias registradas.
     */
    @GetMapping("/convivencia-calculada")
    @Operation(
        summary = "Calcular profesor de convivencia",
        description = "Calcula dinámicamente qué profesor debe cubrir el aula de convivencia para una fecha/hora " +
                     "específica basándose en los contadores históricos. No persiste la asignación en BD."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Profesor calculado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
        @ApiResponse(responseCode = "404", description = "No hay profesores de guardia disponibles"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<?> calcularConvivencia(
            @Parameter(description = "Fecha para calcular (formato: YYYY-MM-DD)", example = "2025-10-03")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            
            @Parameter(description = "Hora del día (1-8)", example = "1")
            @RequestParam Integer hora
    ) {
        try {
            logger.debug("Calculando convivencia para fecha={} hora={}", fecha, hora);
            
            // Validar parámetros
            if (hora < 1 || hora > 8) {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "error", "La hora debe estar entre 1 y 8"));
            }
            
            // Calcular profesor de convivencia
            CoberturaCalculoService.ProfesorConvivenciaDto resultado = 
                calculoService.calcularConvivenciaParaHora(fecha, hora);
            
            logger.info("Convivencia calculada: {} para {} hora {}", 
                resultado.getProfesorEmail(), fecha, hora);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "profesorEmail", resultado.getProfesorEmail(),
                "profesorNombre", resultado.getProfesorNombre(),
                "guardiasConvivenciaRealizadas", resultado.getGuardiasConvivenciaRealizadas(),
                "fecha", resultado.getFecha(),
                "hora", resultado.getHora()
            ));
        } catch (IllegalArgumentException e) {
            // Error de fin de semana o parámetros inválidos
            logger.warn("Parámetros inválidos para cálculo de convivencia: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (RuntimeException e) {
            // No hay profesores de guardia disponibles
            logger.error("Error calculando convivencia: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado calculando convivencia: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Error interno del servidor"));
        }
    }
}
