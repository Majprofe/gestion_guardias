package es.iesjandula.guardias.services;

import es.iesjandula.guardias.dto.ProfesorGuardiaSimpleDto;
import es.iesjandula.guardias.models.ContadorGuardias;
import es.iesjandula.guardias.models.DiaSemana;
import es.iesjandula.guardias.repositories.ContadorGuardiasRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para calcular asignaciones de cobertura dinámicas sin persistirlas en BD.
 * Se utiliza principalmente para determinar qué profesor debe estar en el aula de convivencia
 * cuando no hay ausencias registradas.
 */
@Service
public class CoberturaCalculoService {

    private static final Logger logger = LoggerFactory.getLogger(CoberturaCalculoService.class);

    @Autowired
    private ContadorGuardiasRepository contadorGuardiasRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${horarios.api.url:http://localhost:8082}")
    private String horariosApiUrl;

    /**
     * Calcula qué profesor debería estar en el aula de convivencia para una fecha/hora específica,
     * basándose en los contadores históricos de guardias de convivencia.
     * 
     * Este método NO persiste la asignación en BD, solo calcula y devuelve quién debería estar.
     * 
     * @param fecha Fecha para la cual calcular
     * @param hora Hora del día (1-8)
     * @return Información del profesor asignado al aula de convivencia
     * @throws RuntimeException Si no hay profesores de guardia disponibles
     */
    public ProfesorConvivenciaDto calcularConvivenciaParaHora(LocalDate fecha, Integer hora) {
        logger.debug("Calculando aula de convivencia para fecha {} hora {}", fecha, hora);
        
        // Convertir fecha a día de la semana
        DiaSemana diaSemana = convertirADiaSemana(fecha.getDayOfWeek());
        
        // Obtener profesores de guardia desde el backend de horarios usando RestTemplate
        List<ProfesorGuardiaSimpleDto> profesoresGuardia = obtenerProfesoresGuardia(diaSemana.ordinal() + 1, hora);
        
        if (profesoresGuardia.isEmpty()) {
            logger.warn("No hay profesores de guardia disponibles para {} hora {}", fecha, hora);
            throw new RuntimeException(
                String.format("No hay profesores de guardia disponibles para la fecha %s hora %d", fecha, hora)
            );
        }
        
        logger.debug("Encontrados {} profesores de guardia: {}", 
            profesoresGuardia.size(), 
            profesoresGuardia.stream().map(ProfesorGuardiaSimpleDto::getEmail).toList());
        
        // Obtener o crear contadores para todos los profesores de guardia
        List<ContadorGuardias> contadores = profesoresGuardia.stream()
            .map(profesor -> {
                Optional<ContadorGuardias> contadorOpt = contadorGuardiasRepository
                    .findByProfesorEmailAndDiaSemanaAndHora(profesor.getEmail(), diaSemana, hora);
                
                if (contadorOpt.isPresent()) {
                    return contadorOpt.get();
                } else {
                    // Crear contador en memoria (no persistir)
                    ContadorGuardias nuevoContador = new ContadorGuardias();
                    nuevoContador.setProfesorEmail(profesor.getEmail());
                    nuevoContador.setDiaSemana(diaSemana);
                    nuevoContador.setHora(hora);
                    nuevoContador.setGuardiasNormales(0);
                    nuevoContador.setGuardiasProblematicas(0);
                    nuevoContador.setGuardiasConvivencia(0);
                    return nuevoContador;
                }
            })
            .toList();
        
        // Ordenar por guardiasConvivencia ASC, luego por email alfabéticamente (tiebreaker)
        ContadorGuardias contadorSeleccionado = contadores.stream()
            .min(Comparator.comparingInt(ContadorGuardias::getGuardiasConvivencia)
                          .thenComparing(ContadorGuardias::getProfesorEmail))
            .orElseThrow(() -> new RuntimeException("Error al seleccionar profesor para convivencia"));
        
        // Buscar el objeto ProfesorGuardiaSimpleDto correspondiente para obtener nombre completo
        ProfesorGuardiaSimpleDto profesorSeleccionado = profesoresGuardia.stream()
            .filter(p -> p.getEmail().equals(contadorSeleccionado.getProfesorEmail()))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("No se encontró información del profesor seleccionado"));
        
        logger.info("Profesor calculado para aula convivencia {} hora {}: {} ({} guardias previas)",
            fecha, hora, contadorSeleccionado.getProfesorEmail(), 
            contadorSeleccionado.getGuardiasConvivencia());
        
        // Crear DTO de respuesta
        ProfesorConvivenciaDto resultado = new ProfesorConvivenciaDto();
        resultado.setProfesorEmail(contadorSeleccionado.getProfesorEmail());
        resultado.setProfesorNombre(profesorSeleccionado.getNombre());
        resultado.setGuardiasConvivenciaRealizadas(contadorSeleccionado.getGuardiasConvivencia());
        resultado.setFecha(fecha);
        resultado.setHora(hora);
        
        return resultado;
    }
    
    /**
     * Obtiene los profesores de guardia desde el backend de horarios usando RestTemplate
     */
    @SuppressWarnings("unchecked")
    private List<ProfesorGuardiaSimpleDto> obtenerProfesoresGuardia(int dia, int hora) {
        try {
            String url = horariosApiUrl + "/horario/guardia/dia/" + dia + "/hora/" + hora;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("profesores")) {
                List<Map<String, Object>> profesores = (List<Map<String, Object>>) response.get("profesores");
                
                return profesores.stream()
                        .map(p -> new ProfesorGuardiaSimpleDto(
                                (String) p.get("email"),
                                (String) p.get("nombre"),
                                (String) p.get("abreviatura")
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error obteniendo profesores de guardia: {}", e.getMessage(), e);
        }

        return List.of();
    }
    
    /**
     * Convierte DayOfWeek de Java a DiaSemana del modelo de la aplicación
     */
    private DiaSemana convertirADiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            default -> throw new IllegalArgumentException("No se pueden calcular guardias para fin de semana: " + dayOfWeek);
        };
    }
    
    /**
     * DTO interno para la respuesta del cálculo de convivencia
     */
    public static class ProfesorConvivenciaDto {
        private String profesorEmail;
        private String profesorNombre;
        private Integer guardiasConvivenciaRealizadas;
        private LocalDate fecha;
        private Integer hora;
        
        // Getters y Setters
        public String getProfesorEmail() {
            return profesorEmail;
        }
        
        public void setProfesorEmail(String profesorEmail) {
            this.profesorEmail = profesorEmail;
        }
        
        public String getProfesorNombre() {
            return profesorNombre;
        }
        
        public void setProfesorNombre(String profesorNombre) {
            this.profesorNombre = profesorNombre;
        }
        
        public Integer getGuardiasConvivenciaRealizadas() {
            return guardiasConvivenciaRealizadas;
        }
        
        public void setGuardiasConvivenciaRealizadas(Integer guardiasConvivenciaRealizadas) {
            this.guardiasConvivenciaRealizadas = guardiasConvivenciaRealizadas;
        }
        
        public LocalDate getFecha() {
            return fecha;
        }
        
        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
        }
        
        public Integer getHora() {
            return hora;
        }
        
        public void setHora(Integer hora) {
            this.hora = hora;
        }
    }
}
