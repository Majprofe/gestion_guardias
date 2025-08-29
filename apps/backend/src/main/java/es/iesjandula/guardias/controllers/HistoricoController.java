package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.AusenciaConGuardiasDTO;
import es.iesjandula.guardias.services.AusenciaService;
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
     * @param emailProfesor Email del profesor del cual obtener el histórico.
     * @return Mapa con fechas, horas y listas de ausencias específicas del profesor.
     */
    @GetMapping("/profesor/{emailProfesor}")
    @Operation(summary = "Obtener histórico de faltas de un profesor específico",
            description = "Devuelve el histórico de faltas de un profesor específico con sus detalles.")
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoPorProfesor(
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
