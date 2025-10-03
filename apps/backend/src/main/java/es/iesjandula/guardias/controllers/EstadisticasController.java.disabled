package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.EstadisticasContabilizacionDTO;
import es.iesjandula.guardias.models.ContadorGuardias;
import es.iesjandula.guardias.models.DiaSemana;
import es.iesjandula.guardias.services.ContabilizacionGuardiasService;
import es.iesjandula.guardias.services.ArchivoAusenciaService;
import es.iesjandula.guardias.repositories.ContadorGuardiasRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para métricas y estadísticas del sistema de guardias.
 */
@RestController
@RequestMapping("/api/estadisticas")
@Tag(name = "Estadísticas", description = "Métricas y reportes del sistema de guardias")
public class EstadisticasController {

    @Autowired
    private ContabilizacionGuardiasService contabilizacionService;

    @Autowired
    private ContadorGuardiasRepository contadorRepository;

    @Autowired
    private ArchivoAusenciaService archivoService;

    /**
     * Obtiene estadísticas de contabilización para un día específico.
     */
    @GetMapping("/contabilizacion/{fecha}")
    @Operation(summary = "Estadísticas del día", description = "Obtiene estadísticas de contabilización para un día específico")
    public ResponseEntity<EstadisticasContabilizacionDTO> obtenerEstadisticasDia(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        EstadisticasContabilizacionDTO estadisticas = contabilizacionService.obtenerEstadisticasDia(fecha);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Obtiene los contadores de guardias de un profesor específico.
     */
    @GetMapping("/profesor/{profesorEmail}/contadores")
    @Operation(summary = "Contadores de profesor", description = "Obtiene todos los contadores de guardias de un profesor")
    public ResponseEntity<List<ContadorGuardias>> obtenerContadoresProfesor(@PathVariable String profesorEmail) {
        List<ContadorGuardias> contadores = contadorRepository.findByProfesorEmailOrderByDiaSemanaAscHoraAsc(profesorEmail);
        return ResponseEntity.ok(contadores);
    }

    /**
     * Obtiene contadores de guardias por día y hora específicos.
     */
    @GetMapping("/contadores/{dia}/{hora}")
    @Operation(summary = "Contadores por día y hora", description = "Obtiene todos los contadores para un día y hora específicos")
    public ResponseEntity<List<ContadorGuardias>> obtenerContadoresPorDiaHora(
            @PathVariable DiaSemana dia, 
            @PathVariable Integer hora) {
        List<ContadorGuardias> contadores = contadorRepository.findByDiaSemanaAndHoraOrderByProfesorEmailAsc(dia, hora);
        return ResponseEntity.ok(contadores);
    }

    /**
     * Obtiene el dashboard de equidad: estadísticas generales de distribución de guardias.
     */
    @GetMapping("/dashboard-equidad")
    @Operation(summary = "Dashboard de equidad", description = "Obtiene estadísticas generales de distribución de guardias")
    public ResponseEntity<DashboardEquidadDTO> obtenerDashboardEquidad() {
        // Estadísticas por día
        List<Object[]> estadisticasPorDia = contadorRepository.getEstadisticasPorDia();
        Map<DiaSemana, Long> guardiasPorDia = estadisticasPorDia.stream()
                .collect(Collectors.toMap(
                        row -> (DiaSemana) row[0],
                        row -> (Long) row[1]
                ));

        // Estadísticas por hora
        List<Object[]> estadisticasPorHora = contadorRepository.getEstadisticasPorHora();
        Map<Integer, Long> guardiasPorHora = estadisticasPorHora.stream()
                .collect(Collectors.toMap(
                        row -> (Integer) row[0],
                        row -> (Long) row[1]
                ));

        // Estadísticas de archivos
        ArchivoAusenciaService.ArchivoEstadisticas estadisticasArchivos = archivoService.obtenerEstadisticas();

        DashboardEquidadDTO dashboard = new DashboardEquidadDTO(
                guardiasPorDia,
                guardiasPorHora,
                estadisticasArchivos.getTotalArchivos(),
                estadisticasArchivos.getTamañoTotalFormateado()
        );

        return ResponseEntity.ok(dashboard);
    }

    /**
     * Obtiene el reporte de desbalance: profesores con cargas muy dispares.
     */
    @GetMapping("/reporte-desbalance")
    @Operation(summary = "Reporte de desbalance", description = "Identifica profesores con cargas de guardias muy dispares")
    public ResponseEntity<List<ReporteDesbalanceDTO>> obtenerReporteDesbalance() {
        // Obtener todos los contadores
        List<ContadorGuardias> todosLosContadores = contadorRepository.findAll();

        // Agrupar por profesor y calcular totales
        Map<String, ReporteDesbalanceDTO> reportePorProfesor = todosLosContadores.stream()
                .collect(Collectors.groupingBy(
                        ContadorGuardias::getProfesorEmail,
                        Collectors.reducing(
                                new ReporteDesbalanceDTO("", 0, 0, 0, 0),
                                contador -> new ReporteDesbalanceDTO(
                                        contador.getProfesorEmail(),
                                        contador.getGuardiasNormales(),
                                        contador.getGuardiasProblematicas(),
                                        contador.getGuardiasConvivencia(),
                                        contador.getTotalGuardias()
                                ),
                                (a, b) -> new ReporteDesbalanceDTO(
                                        a.getProfesorEmail().isEmpty() ? b.getProfesorEmail() : a.getProfesorEmail(),
                                        a.getTotalNormales() + b.getTotalNormales(),
                                        a.getTotalProblematicas() + b.getTotalProblematicas(),
                                        a.getTotalConvivencia() + b.getTotalConvivencia(),
                                        a.getTotalGuardias() + b.getTotalGuardias()
                                )
                        )
                ));

        List<ReporteDesbalanceDTO> reporte = reportePorProfesor.values()
                .stream()
                .sorted((a, b) -> Integer.compare(b.getTotalGuardias(), a.getTotalGuardias()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(reporte);
    }

    /**
     * DTO para el dashboard de equidad.
     */
    public static class DashboardEquidadDTO {
        private final Map<DiaSemana, Long> guardiasPorDia;
        private final Map<Integer, Long> guardiasPorHora;
        private final long totalArchivos;
        private final String tamañoTotalArchivos;

        public DashboardEquidadDTO(Map<DiaSemana, Long> guardiasPorDia, Map<Integer, Long> guardiasPorHora, 
                                   long totalArchivos, String tamañoTotalArchivos) {
            this.guardiasPorDia = guardiasPorDia;
            this.guardiasPorHora = guardiasPorHora;
            this.totalArchivos = totalArchivos;
            this.tamañoTotalArchivos = tamañoTotalArchivos;
        }

        public Map<DiaSemana, Long> getGuardiasPorDia() { return guardiasPorDia; }
        public Map<Integer, Long> getGuardiasPorHora() { return guardiasPorHora; }
        public long getTotalArchivos() { return totalArchivos; }
        public String getTamañoTotalArchivos() { return tamañoTotalArchivos; }
    }

    /**
     * DTO para el reporte de desbalance.
     */
    public static class ReporteDesbalanceDTO {
        private final String profesorEmail;
        private final int totalNormales;
        private final int totalProblematicas;
        private final int totalConvivencia;
        private final int totalGuardias;

        public ReporteDesbalanceDTO(String profesorEmail, int totalNormales, int totalProblematicas, 
                                    int totalConvivencia, int totalGuardias) {
            this.profesorEmail = profesorEmail;
            this.totalNormales = totalNormales;
            this.totalProblematicas = totalProblematicas;
            this.totalConvivencia = totalConvivencia;
            this.totalGuardias = totalGuardias;
        }

        public String getProfesorEmail() { return profesorEmail; }
        public int getTotalNormales() { return totalNormales; }
        public int getTotalProblematicas() { return totalProblematicas; }
        public int getTotalConvivencia() { return totalConvivencia; }
        public int getTotalGuardias() { return totalGuardias; }
    }
}
