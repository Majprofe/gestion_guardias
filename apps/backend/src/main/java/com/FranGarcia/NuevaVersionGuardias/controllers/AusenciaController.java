package com.FranGarcia.NuevaVersionGuardias.controllers;

import com.FranGarcia.NuevaVersionGuardias.dto.AusenciaConGuardiasDTO;
import com.FranGarcia.NuevaVersionGuardias.dto.CrearAusenciaDTO;
import com.FranGarcia.NuevaVersionGuardias.models.Ausencia;
import com.FranGarcia.NuevaVersionGuardias.services.AusenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Controlador REST para gestionar las operaciones relacionadas con ausencias y sus coberturas.
 * Permite registrar nuevas ausencias, consultar por fecha u obtener históricos por profesor.
 */
@RestController
@RequestMapping("/api/ausencias")
@Tag(name = "Ausencias", description = "Operaciones relacionadas con ausencias y coberturas")
public class AusenciaController {

    @Autowired
    private AusenciaService ausenciaService;

    /**
     * Registra una nueva ausencia y asigna una cobertura si hay profesores disponibles.
     * IMPORTANTE: El ID se genera automáticamente, NO debe ser proporcionado.
     *
     * @param dto DTO con la información de la ausencia (SIN ID).
     * @return La entidad {@link AusenciaConGuardiasDTO} registrada (CON ID generado).
     */
    @PostMapping
    @Operation(summary = "Registrar una nueva ausencia",
               description = "Registra una nueva ausencia en el sistema. El ID se genera automáticamente. " +
                           "Opcionalmente asigna un profesor de guardia si se proporciona.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Ausencia registrada exitosamente con ID generado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Ya existe una ausencia para este profesor en esta fecha y hora")
    })
    public ResponseEntity<AusenciaConGuardiasDTO> registrarAusencia(
            @Valid @RequestBody CrearAusenciaDTO dto) {
        AusenciaConGuardiasDTO ausenciaCreada = ausenciaService.guardarYAsignarCobertura(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ausenciaCreada);
    }

    /**
     * Lista las ausencias registradas en una fecha y hora específicas.
     *
     * @param fecha Fecha de la ausencia (formato ISO).
     * @param hora Hora de la ausencia.
     * @return Lista de ausencias para la fecha y hora proporcionadas, si existen.
     */
    @GetMapping("/por-fecha-hora")
    @Operation(summary = "Listar ausencias por fecha y hora",
               description = "Obtiene todas las ausencias registradas para una fecha y hora específicas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Ausencias encontradas"),
        @ApiResponse(responseCode = "404", description = "No se encontraron ausencias para la fecha y hora especificadas")
    })
    public ResponseEntity<List<Ausencia>> listarPorFechaHora(
            @Parameter(description = "Fecha de la ausencia en formato YYYY-MM-DD", example = "2025-08-21")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @Parameter(description = "Hora de la ausencia (1-8)", example = "3")
            @RequestParam @Min(1) @Max(8) Integer hora) {
        Optional<List<Ausencia>> ausencias = ausenciaService.listarPorFechaHora(fecha, hora);
        return ausencias.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Lista todas las ausencias registradas en una fecha específica, agrupadas por hora.
     *
     * @param fecha Fecha para la cual se desea consultar.
     * @return Mapa con la hora como clave y listas de ausencias como valor.
     */
    @GetMapping("/por-fecha")
    @Operation(summary = "Listar ausencias agrupadas por hora en una fecha específica")
    public Map<String, List<AusenciaConGuardiasDTO>> listarPorFecha(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ausenciaService.listarAusenciasPorFechaAgrupadasPorHora(fecha);
    }

    /**
     * Obtiene el histórico completo de ausencias, agrupado por fecha y hora.
     *
     * @return Mapa de fechas con mapas internos de hora y listas de ausencias.
     */
    @GetMapping("/historico")
    @Operation(summary = "Obtener el histórico completo de ausencias agrupado por fecha y hora")
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoFaltas() {
        return ausenciaService.historicoFaltas();
    }

    /**
     * Obtiene el histórico de ausencias de un profesor específico, agrupado por fecha y hora.
     *
     * @param emailProfesor Correo electrónico del profesor.
     * @return Mapa de fechas con mapas internos de hora y listas de ausencias del profesor.
     */
    @GetMapping("/historico/{emailProfesor}")
    @Operation(summary = "Obtener el histórico de ausencias de un profesor específico")
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoPorProfesor(
            @PathVariable String emailProfesor) {
        return ausenciaService.historicoFaltasPorProfesor(emailProfesor);
    }

    /**
     * Obtiene todas las ausencias registradas por un profesor.
     *
     * @param emailProfesor Correo electrónico del profesor.
     * @return Lista de ausencias correspondientes al profesor.
     */
    @GetMapping("/por-profesor/{emailProfesor}")
    @Operation(summary = "Obtener todas las ausencias de un profesor")
    public List<AusenciaConGuardiasDTO> ausenciasPorProfesor(@PathVariable String emailProfesor) {
        return ausenciaService.obtenerAusenciasPorProfesor(emailProfesor);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una ausencia",
               description = "Elimina una ausencia específica del sistema por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Ausencia eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Ausencia no encontrada")
    })
    public ResponseEntity<Void> eliminarAusencia(
            @Parameter(description = "ID único de la ausencia a eliminar", example = "1")
            @PathVariable Long id) {
        ausenciaService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }
}