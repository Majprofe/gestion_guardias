package com.FranGarcia.NuevaVersionGuardias.controllers;

import com.FranGarcia.NuevaVersionGuardias.dto.AusenciaConGuardiasDTO;
import com.FranGarcia.NuevaVersionGuardias.dto.AusenciaDTO;
import com.FranGarcia.NuevaVersionGuardias.services.AusenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/historico")
@CrossOrigin(origins = "*")
@Tag(name = "Histórico de ausencias", description = "Operaciones relacionadas con el histórico de ausencias de los profesores.")
public class HistoricoController {

    @Autowired
    private AusenciaService ausenciaService;

    /**
     * Endpoint para obtener el histórico completo de faltas.
     *
     * @return Mapa con fechas, horas y lista de ausencias con guardias.
     */
    @GetMapping("/listar")
    @Operation(summary = "Obtener histórico completo de faltas",
            description = "Devuelve el histórico completo de faltas con sus detalles, incluyendo las guardias.")
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> listarHistorico() {
        return ausenciaService.historicoFaltas();
    }
    /**
     * Endpoint para obtener el histórico de faltas de un profesor específico.
     *
     * @param emailProfesor El email del profesor cuya información se solicita.
     * @return Mapa con fechas, horas y lista de ausencias con guardias para el profesor.
     */
    @GetMapping("/listar/{emailProfesor}")
    @Operation(summary = "Obtener histórico de faltas de un profesor",
            description = "Devuelve el histórico de faltas de un profesor específico, incluyendo las guardias.")
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> listarHistoricoPorProfesor(
            @PathVariable String emailProfesor) {
        return ausenciaService.historicoFaltasPorProfesor(emailProfesor);
    }

    /**
     * Endpoint para obtener todas las ausencias.
     *
     * @return Respuesta con el mapa del histórico completo de ausencias.
     */
    @GetMapping("/todas")
    @Operation(summary = "Obtener todas las ausencias",
            description = "Devuelve todas las ausencias con sus respectivas guardias.")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>>> obtenerTodasLasAusencias() {
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = ausenciaService.historicoFaltas();
        return ResponseEntity.ok(historico);
    }
}


