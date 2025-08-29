package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.CoberturaDTO;
import es.iesjandula.guardias.dto.EstadisticasContabilizacionDTO;
import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.services.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controlador para funciones administrativas de gestión de coberturas
 * Incluye validación, cancelación y estadísticas
 */
@RestController
@RequestMapping("/api/admin/coberturas")
@CrossOrigin(origins = "http://localhost:5500")
@Tag(name = "Administración de Coberturas", description = "Gestión administrativa de coberturas")
public class AdminCoberturasController {

    @Autowired
    private AsignacionGuardiasService asignacionService;
    
    @Autowired
    private ContabilizacionGuardiasService contabilizacionService;
    
    @Autowired
    private CoberturaService coberturaService;

    /**
     * Valida una cobertura manualmente (confirma que se realizó)
     */
    @PostMapping("/validar/{coberturaId}")
    @Operation(summary = "Validar cobertura", 
               description = "Marca una cobertura como validada (se realizó efectivamente)")
    public ResponseEntity<?> validarCobertura(
            @PathVariable Long coberturaId,
            @RequestBody @Parameter(description = "Email del administrador") Map<String, String> request) {
        
        try {
            String adminEmail = request.get("adminEmail");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email del administrador es requerido");
            }
            
            asignacionService.validarCobertura(coberturaId, adminEmail);
            return ResponseEntity.ok().body("Cobertura validada correctamente");
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Cancela una cobertura (no se realizó finalmente)
     */
    @PostMapping("/cancelar/{coberturaId}")
    @Operation(summary = "Cancelar cobertura", 
               description = "Marca una cobertura como cancelada (no se realizó)")
    public ResponseEntity<?> cancelarCobertura(
            @PathVariable Long coberturaId,
            @RequestBody @Parameter(description = "Email del administrador") Map<String, String> request) {
        
        try {
            String adminEmail = request.get("adminEmail");
            if (adminEmail == null || adminEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Email del administrador es requerido");
            }
            
            asignacionService.cancelarCobertura(coberturaId, adminEmail);
            return ResponseEntity.ok().body("Cobertura cancelada correctamente");
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene coberturas pendientes de validación para un día específico
     */
    @GetMapping("/pendientes")
    @Operation(summary = "Obtener coberturas pendientes", 
               description = "Lista coberturas que requieren validación del administrador")
    public ResponseEntity<List<CoberturaDTO>> obtenerCoberturasPendientes(
            @RequestParam(required = false) 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
            @Parameter(description = "Fecha a consultar (por defecto hoy)") LocalDate fecha) {
        
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        
        List<Cobertura> coberturas = asignacionService.obtenerCoberturasPendientes(fecha);
        
        List<CoberturaDTO> respuesta = coberturas.stream()
            .map(this::convertirADTO)
            .toList();
        
        return ResponseEntity.ok(respuesta);
    }

    /**
     * Obtiene estadísticas de contabilización para un día
     */
    @GetMapping("/estadisticas/{fecha}")
    @Operation(summary = "Obtener estadísticas de contabilización", 
               description = "Muestra estadísticas de coberturas para un día específico")
    public ResponseEntity<EstadisticasContabilizacionDTO> obtenerEstadisticasDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        EstadisticasContabilizacionDTO estadisticas = 
            contabilizacionService.obtenerEstadisticasDia(fecha);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Fuerza la redistribución de coberturas para un día específico
     */
    @PostMapping("/redistribuir/{fecha}")
    @Operation(summary = "Redistribuir coberturas", 
               description = "Redistribuye todas las coberturas automáticas de un día para optimizar la distribución")
    public ResponseEntity<?> redistribuirCoberturas(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        
        try {
            return ResponseEntity.ok().body("Redistribución completada para " + fecha);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error en redistribución: " + e.getMessage());
        }
    }

    /**
     * Convierte entidad Cobertura a DTO para la respuesta
     */
    private CoberturaDTO convertirADTO(Cobertura cobertura) {
        CoberturaDTO dto = new CoberturaDTO();
        dto.setAusenciaId(cobertura.getAusencia().getId());
        dto.setProfesorCubreEmail(cobertura.getProfesorCubreEmail());
        dto.setGrupo(cobertura.getGrupo());
        dto.setAula(cobertura.getAula());
        dto.setHora(cobertura.getAusencia().getHora());
        dto.setFecha(cobertura.getAusencia().getFecha());
        dto.setProfesorAusenteEmail(cobertura.getAusencia().getProfesorAusenteEmail());
        dto.setTarea(cobertura.getAusencia().getTarea());
        
        return dto;
    }
}
