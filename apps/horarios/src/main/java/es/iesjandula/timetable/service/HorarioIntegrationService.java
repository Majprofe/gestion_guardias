package es.iesjandula.timetable.service;

import es.iesjandula.timetable.dto.ProfesorGuardiaDto;
import es.iesjandula.timetable.dto.ActualizarContadorDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio de integración con el backend de gestión de guardias
 * Proporciona métodos específicos para la sincronización bidireccional
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HorarioIntegrationService {

    private final ProfesorService profesorService;

    /**
     * Sincroniza contadores desde el backend de guardias hacia horarios
     * @param actualizaciones Lista de actualizaciones de contadores
     * @return Lista de profesores actualizados
     */
    public List<ProfesorGuardiaDto> sincronizarContadoresDesdeBackend(List<ActualizarContadorDto> actualizaciones) {
        log.info("Sincronizando {} actualizaciones de contadores desde backend", actualizaciones.size());
        
        try {
            List<ProfesorGuardiaDto> profesoresActualizados = profesorService.actualizarContadoresLote(actualizaciones);
            log.info("Sincronización completada exitosamente para {} profesores", profesoresActualizados.size());
            return profesoresActualizados;
        } catch (Exception e) {
            log.error("Error durante la sincronización de contadores: {}", e.getMessage(), e);
            throw new RuntimeException("Error en sincronización de contadores", e);
        }
    }

    /**
     * Obtiene el estado actual de todos los contadores para envío al backend
     * @return Mapa con información de contadores por profesor
     */
    public Map<String, ProfesorGuardiaDto> obtenerEstadoContadores() {
        log.info("Obteniendo estado actual de contadores para sincronización");
        
        List<ProfesorGuardiaDto> profesores = profesorService.getProfesoresConGuardias();
        
        return profesores.stream()
                .collect(Collectors.toMap(
                    ProfesorGuardiaDto::getEmail,
                    profesor -> profesor
                ));
    }

    /**
     * Actualiza un contador específico (usado para sincronización en tiempo real)
     * @param email Email del profesor
     * @param dia Día de la semana
     * @param hora Hora del día
     * @param tipoGuardia Tipo de guardia (normales, problematicas, convivencia)
     * @param cantidad Cantidad a incrementar
     * @return Profesor actualizado
     */
    public ProfesorGuardiaDto incrementarContadorEspecifico(String email, Integer dia, Integer hora, 
                                                            String tipoGuardia, Integer cantidad) {
        log.debug("Incrementando contador {} para profesor {} en día {} hora {} con cantidad {}", 
                 tipoGuardia, email, dia, hora, cantidad);
        
        ActualizarContadorDto actualizacion = new ActualizarContadorDto();
        actualizacion.setEmail(email);
        actualizacion.setDia(dia);
        actualizacion.setHora(hora);
        actualizacion.setOperacion("INCREMENT");
        
        // Establecer la cantidad según el tipo de guardia
        switch (tipoGuardia.toLowerCase()) {
            case "normales":
                actualizacion.setGuardiasNormales(cantidad);
                break;
            case "problematicas":
                actualizacion.setGuardiasProblematicas(cantidad);
                break;
            case "convivencia":
                actualizacion.setGuardiasConvivencia(cantidad);
                break;
            default:
                throw new IllegalArgumentException("Tipo de guardia no válido: " + tipoGuardia);
        }
        
        return profesorService.actualizarContadores(actualizacion);
    }

    /**
     * Resetea contadores de un profesor (usado para limpieza de datos)
     * @param email Email del profesor
     */
    public void limpiarContadoresProfesor(String email) {
        log.info("Limpiando contadores del profesor: {}", email);
        profesorService.resetearContadores(email);
    }

    /**
     * Valida la integridad de los contadores entre sistemas
     * @param contadoresBackend Contadores del backend de guardias
     * @return Reporte de diferencias encontradas
     */
    public IntegrityReport validarIntegridadContadores(Map<String, Map<String, Integer>> contadoresBackend) {
        log.info("Validando integridad de contadores entre sistemas");
        
        Map<String, ProfesorGuardiaDto> contadoresHorarios = obtenerEstadoContadores();
        IntegrityReport report = new IntegrityReport();
        
        for (Map.Entry<String, Map<String, Integer>> entry : contadoresBackend.entrySet()) {
            String email = entry.getKey();
            Map<String, Integer> contadoresProfesorBackend = entry.getValue();
            
            ProfesorGuardiaDto profesorHorarios = contadoresHorarios.get(email);
            
            if (profesorHorarios == null) {
                report.addDiscrepancy(email, "Profesor no encontrado en horarios");
                continue;
            }
            
            // Comparar totales
            Integer totalBackend = contadoresProfesorBackend.getOrDefault("total", 0);
            Integer totalHorarios = profesorHorarios.getGuardiasRealizadas() + 
                                   profesorHorarios.getGuardiasProblematicas() + 
                                   profesorHorarios.getGuardiasConvivencia();
            
            if (!totalBackend.equals(totalHorarios)) {
                report.addDiscrepancy(email, 
                    String.format("Total diferentes - Backend: %d, Horarios: %d", totalBackend, totalHorarios));
            }
        }
        
        log.info("Validación completada. Encontradas {} discrepancias", report.getDiscrepancies().size());
        return report;
    }

    /**
     * Clase para reportar diferencias en la integridad de datos
     */
    public static class IntegrityReport {
        private final Map<String, String> discrepancies = new java.util.HashMap<>();
        
        public void addDiscrepancy(String profesorEmail, String description) {
            discrepancies.put(profesorEmail, description);
        }
        
        public Map<String, String> getDiscrepancies() {
            return discrepancies;
        }
        
        public boolean hasDiscrepancies() {
            return !discrepancies.isEmpty();
        }
        
        public int getDiscrepancyCount() {
            return discrepancies.size();
        }
    }
}
