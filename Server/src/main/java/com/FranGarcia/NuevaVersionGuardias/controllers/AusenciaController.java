package com.FranGarcia.NuevaVersionGuardias.controllers;

import com.FranGarcia.NuevaVersionGuardias.dto.AusenciaConGuardiasDTO;
import com.FranGarcia.NuevaVersionGuardias.models.Ausencia;
import com.FranGarcia.NuevaVersionGuardias.services.AusenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
@CrossOrigin(origins = "http://127.0.0.1:5500")
@Tag(name = "Ausencias", description = "Operaciones relacionadas con ausencias y coberturas")
public class AusenciaController {

    @Autowired
    private AusenciaService ausenciaService;

    /**
     * Registra una nueva ausencia y asigna una cobertura si hay profesores disponibles.
     *
     * @param dto DTO con la información de la ausencia y posibles profesores de guardia.
     * @return La entidad {@link Ausencia} registrada.
     */
    @PostMapping
    @Operation(summary = "Registrar una ausencia y asignar cobertura si hay profesores disponibles")
    public Ausencia registrarAusencia(@RequestBody AusenciaConGuardiasDTO dto) {
        return ausenciaService.guardarYAsignarCobertura(dto);
    }

    /**
     * Lista las ausencias registradas en una fecha y hora específicas.
     *
     * @param fecha Fecha de la ausencia (formato ISO).
     * @param hora Hora de la ausencia.
     * @return Lista de ausencias para la fecha y hora proporcionadas, si existen.
     */
    @GetMapping("/por-fecha-hora")
    @Operation(summary = "Listar ausencias por fecha y hora")
    public Optional<List<Ausencia>> listarPorFechaHora(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam Integer hora) {
        return ausenciaService.listarPorFechaHora(fecha, hora);
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
    public ResponseEntity<Void> eliminarAusencia(@PathVariable Long id) {
        ausenciaService.eliminarPorId(id);
        return ResponseEntity.noContent().build();
    }


}




    /* CODIGO ANTIGUO

    @Autowired
    private AusenciaService ausenciaService;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarAusencias(@RequestBody List<AusenciaDTO> ausenciasDTO) {
        try {
            List<Ausencia> ausencias = new ArrayList<>();
            for (AusenciaDTO ausenciaDTO : ausenciasDTO) {
                Ausencia ausencia = ausenciaService.registrarAusencia(ausenciaDTO);
                ausencias.add(ausencia);
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(ausencias);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error al registrar las ausencias: " + e.getMessage());
        }
    }

    @DeleteMapping("/eliminar")
    public ResponseEntity<String> eliminarFalta(
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("hora") Integer hora,
            @RequestParam("emailProfesor") String emailProfesor) {
        try {
            boolean eliminado = ausenciaService.eliminarFalta(fecha, hora, emailProfesor);

            if (eliminado) {
                return ResponseEntity.ok("Falta eliminada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Falta no encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al eliminar la falta.");
        }
    }

    @GetMapping("/listar/{fecha}")
    public ResponseEntity<Map<String, List<AusenciaDTO>>> listarAusenciasPorFecha(@PathVariable("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            Map<String, List<AusenciaDTO>> ausenciasPorHora = ausenciaService.listarAusenciasPorFechaAgrupadasPorHora(fecha);
            if (ausenciasPorHora.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);     //Si esta vacio, querra decir que no hay ausencias ese dia, por lo que devolvera vacio
            }
            return ResponseEntity.ok(ausenciasPorHora);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/historico")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaDTO>>>> obtenerHistoricoFaltas() {
        try {
            Map<LocalDate, Map<String, List<AusenciaDTO>>> historicoFaltas = ausenciaService.historicoFaltas();

            // Si el histórico está vacío, devolvemos un 204
            if (historicoFaltas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }

            // Devolvemos el histórico con un 200
            return ResponseEntity.ok(historicoFaltas);
        } catch (Exception e) {
            // En caso de error, devolvemos un 400
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/historico/{email}")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaDTO>>>> obtenerHistoricoFaltasPorProfesor(@PathVariable String email) {
        try {
            Map<LocalDate, Map<String, List<AusenciaDTO>>> historicoFaltas = ausenciaService.historicoFaltasPorProfesor(email);
            if (historicoFaltas.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
            }
            return ResponseEntity.ok(historicoFaltas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping("/misAusencias/{email}")
    public ResponseEntity<List<AusenciaDTO>> obtenerMisAusencias(@PathVariable String email) {
        List<AusenciaDTO> ausencias = ausenciaService.obtenerAusenciasPorProfesor(email);
        return ResponseEntity.ok(ausencias);
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarAusencia(@PathVariable Long id) {
        try {
            Optional<Ausencia> ausencia = ausenciaRepository.findById(id);
            if (ausencia.isPresent()) {
                ausenciaRepository.delete(ausencia.get());
                return ResponseEntity.ok("Ausencia eliminada correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ausencia no encontrada.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error eliminando ausencia: " + e.getMessage());
        }
    }


}



     */