package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.AusenciaConGuardiasDTO;
import es.iesjandula.guardias.dto.CrearAusenciaDTO;
import es.iesjandula.guardias.dto.CrearAusenciaMultipleDTO;
import es.iesjandula.guardias.services.AusenciaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar ausencias
 */
@RestController
@RequestMapping("/api/ausencias")
@Tag(name = "Ausencias", description = "Gestión de ausencias de profesores")
public class AusenciaController {
    
    @Autowired
    private AusenciaService ausenciaService;
    
    /**
     * Crea una nueva ausencia
     */
    @PostMapping
    @Operation(summary = "Crear ausencia", description = "Crea una nueva ausencia y asigna automáticamente las guardias")
    public ResponseEntity<AusenciaConGuardiasDTO> crearAusencia(@RequestBody CrearAusenciaDTO crearAusenciaDTO) {
        AusenciaConGuardiasDTO ausencia = ausenciaService.guardarYAsignarCobertura(crearAusenciaDTO);
        return ResponseEntity.ok(ausencia);
    }
    
    /**
     * Crea múltiples ausencias para un día completo
     */
    @PostMapping("/multiple")
    @Operation(summary = "Crear ausencias múltiples", description = "Crea múltiples ausencias para un día completo y asigna automáticamente las guardias")
    public ResponseEntity<List<AusenciaConGuardiasDTO>> crearAusenciaMultiple(@RequestBody CrearAusenciaMultipleDTO crearAusenciaMultipleDTO) {
        List<AusenciaConGuardiasDTO> ausencias = ausenciaService.guardarYAsignarCoberturaMultiple(crearAusenciaMultipleDTO);
        return ResponseEntity.ok(ausencias);
    }
    
    /**
     * Obtiene todas las ausencias de una fecha específica agrupadas por hora
     */
    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Obtener ausencias por fecha", description = "Obtiene todas las ausencias de una fecha específica agrupadas por hora")
    public ResponseEntity<Map<String, List<AusenciaConGuardiasDTO>>> obtenerAusenciasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Map<String, List<AusenciaConGuardiasDTO>> ausencias = ausenciaService.listarAusenciasPorFechaAgrupadasPorHora(fecha);
        return ResponseEntity.ok(ausencias);
    }
    
    /**
     * Obtiene las ausencias de un profesor específico
     */
    @GetMapping("/profesor/{profesorEmail}")
    @Operation(summary = "Obtener ausencias por profesor", description = "Obtiene todas las ausencias de un profesor específico")
    public ResponseEntity<List<AusenciaConGuardiasDTO>> obtenerAusenciasProfesor(@PathVariable String profesorEmail) {
        List<AusenciaConGuardiasDTO> ausencias = ausenciaService.obtenerAusenciasPorProfesor(profesorEmail);
        return ResponseEntity.ok(ausencias);
    }
    
    /**
     * Obtiene el histórico de faltas
     */
    @GetMapping("/historico")
    @Operation(summary = "Obtener histórico de faltas", description = "Obtiene el histórico completo de ausencias")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>>> obtenerHistoricoFaltas() {
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = ausenciaService.historicoFaltas();
        return ResponseEntity.ok(historico);
    }
    
    /**
     * Obtiene el histórico de faltas por profesor
     */
    @GetMapping("/historico/profesor/{profesorEmail}")
    @Operation(summary = "Obtener histórico por profesor", description = "Obtiene el histórico de ausencias de un profesor específico")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>>> obtenerHistoricoFaltasProfesor(@PathVariable String profesorEmail) {
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = ausenciaService.historicoFaltasPorProfesor(profesorEmail);
        return ResponseEntity.ok(historico);
    }
    
    /**
     * Elimina una ausencia
     */
    @DeleteMapping("/{ausenciaId}")
    @Operation(summary = "Eliminar ausencia", description = "Elimina una ausencia y su cobertura asociada")
    public ResponseEntity<Void> eliminarAusencia(@PathVariable Long ausenciaId) {
        ausenciaService.eliminarPorId(ausenciaId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * Obtiene las ausencias del día actual
     */
    @GetMapping("/hoy")
    @Operation(summary = "Obtener ausencias de hoy", description = "Obtiene todas las ausencias del día actual")
    public ResponseEntity<Map<String, List<AusenciaConGuardiasDTO>>> obtenerAusenciasHoy() {
        Map<String, List<AusenciaConGuardiasDTO>> ausencias = ausenciaService.listarAusenciasPorFechaAgrupadasPorHora(LocalDate.now());
        return ResponseEntity.ok(ausencias);
    }
    
    /**
     * Test simple para verificar conectividad con la base de datos
     */
    @GetMapping("/test")
    @Operation(summary = "Test de conectividad", description = "Endpoint simple para verificar que el servicio funciona")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDate.now());
        response.put("message", "Servicio de ausencias funcionando correctamente");
        
        try {
            // Test simple de base de datos
            long count = ausenciaService.contarTotalAusencias();
            response.put("totalAusenciasEnBD", count);
        } catch (Exception e) {
            response.put("errorBD", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
