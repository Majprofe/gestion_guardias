package es.iesjandula.timetable.service;

import es.iesjandula.timetable.dto.ActividadDto;
import es.iesjandula.timetable.dto.HorarioDiaResponse;
import es.iesjandula.timetable.dto.HorarioProfesorResponse;
import es.iesjandula.timetable.dto.ProfesorGuardiaDto;
import es.iesjandula.timetable.dto.ProfesorGuardiaResponse;
import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.model.Profesor;
import es.iesjandula.timetable.model.TipoActividad;
import es.iesjandula.timetable.repository.ActividadRepository;
import es.iesjandula.timetable.repository.ProfesorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private ProfesorRepository profesorRepository;

    private static final String[] DIAS_SEMANA = {
            "", "lunes", "martes", "miércoles", "jueves", "viernes"
    };

    public HorarioProfesorResponse getHorarioProfesor(Long profesorId) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesorId);

        Map<String, List<ActividadDto>> horario = buildHorarioPorDias(actividades);

        return new HorarioProfesorResponse(profesor.getId(), profesor.getNombre(), horario);
    }

    public HorarioProfesorResponse getHorarioProfesorPorEmail(String email) {
        Profesor profesor = profesorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesor.getId());

        Map<String, List<ActividadDto>> horario = buildHorarioPorDias(actividades);

        return new HorarioProfesorResponse(profesor.getId(), profesor.getNombre(), horario);
    }

    public HorarioDiaResponse getHorarioProfesorEnDia(Long profesorId, int diaSemana) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesorId);

        List<ActividadDto> actividadesDia = buildActividadesPorDia(actividades, diaSemana);

        return new HorarioDiaResponse(profesor.getId(), profesor.getNombre(), DIAS_SEMANA[diaSemana], actividadesDia);
    }

    public HorarioDiaResponse getHorarioProfesorEnDiaPorEmail(String email, int diaSemana) {
        Profesor profesor = profesorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesor.getId());

        List<ActividadDto> actividadesDia = buildActividadesPorDia(actividades, diaSemana);

        return new HorarioDiaResponse(profesor.getId(), profesor.getNombre(), DIAS_SEMANA[diaSemana], actividadesDia);
    }

    public HorarioProfesorResponse getHorarioProfesorSinOtras(Long profesorId) {
        Profesor profesor = profesorRepository.findById(profesorId)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesorId);

        Map<String, List<ActividadDto>> horario = buildHorarioPorDias(actividades, true);

        return new HorarioProfesorResponse(profesor.getId(), profesor.getNombre(), horario);
    }

    public HorarioProfesorResponse getHorarioProfesorSinOtrasPorEmail(String email) {
        Profesor profesor = profesorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorId(profesor.getId());

        Map<String, List<ActividadDto>> horario = buildHorarioPorDias(actividades, true);

        return new HorarioProfesorResponse(profesor.getId(), profesor.getNombre(), horario);
    }
    
    /**
     * Obtiene las actividades de tipo CLASE de un profesor en un día específico por email
     * 
     * @param email Email del profesor
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @return Respuesta con las actividades de tipo CLASE del profesor en el día especificado
     */
    public HorarioDiaResponse getHorarioClasesPorEmailYDia(String email, int diaSemana) {
        Profesor profesor = profesorRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado"));

        List<Actividad> actividades = actividadRepository.findByProfesorIdAndTramoDiaSemana(profesor.getId(), diaSemana);
        
        // Filtrar solo actividades de tipo CLASE
        List<ActividadDto> actividadesClase = actividades.stream()
                .filter(a -> a.getTipo() == TipoActividad.CLASE)
                .map(a -> new ActividadDto(
                        a.getTramo().getHoraDia(),
                        a.getTipo().name(),
                        a.getAsignatura().getNombre(),
                        a.getGrupos().isEmpty() ? null : a.getGrupos().get(0).getNombre(),
                        a.getAula().getNombre(),
                        a.getAula().getAbreviatura()
                ))
                .sorted(Comparator.comparingInt(ActividadDto::getHora))
                .collect(Collectors.toList());

        return new HorarioDiaResponse(profesor.getId(), profesor.getNombre(), DIAS_SEMANA[diaSemana], actividadesClase);
    }
    
    /**
     * Encuentra profesores que están de guardia en un día y hora específicos
     * 
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @param horaDia Hora del día
     * @return Respuesta con la lista de profesores de guardia
     */
    public ProfesorGuardiaResponse getProfesoresGuardia(int diaSemana, int horaDia) {
        List<Actividad> actividadesGuardia = actividadRepository
                .findByTipoAndTramoDiaSemanaAndTramoHoraDia(TipoActividad.GUARDIA, diaSemana, horaDia);
        
        List<ProfesorGuardiaDto> profesoresGuardia = actividadesGuardia.stream()
                .map(actividad -> {
                    Profesor profesor = actividad.getProfesor();
                    return new ProfesorGuardiaDto(
                            profesor.getId(),
                            profesor.getNombre(),
                            profesor.getEmail(),
                            profesor.getAbreviatura(),
                            profesor.getDepartamento(),
                            profesor.getGuardiasRealizadas(),
                            profesor.getGuardiasProblematicas()
                    );
                })
                .collect(Collectors.toList());
        
        return new ProfesorGuardiaResponse(diaSemana, horaDia, profesoresGuardia);
    }

    private Map<String, List<ActividadDto>> buildHorarioPorDias(List<Actividad> actividades) {
        return buildHorarioPorDias(actividades, false);
    }

    private Map<String, List<ActividadDto>> buildHorarioPorDias(List<Actividad> actividades, boolean excluirOtras) {
        Map<String, List<ActividadDto>> horario = new LinkedHashMap<>();

        for (int i = 1; i <= 5; i++) {
            int dia = i;
            List<ActividadDto> actividadesDia = actividades.stream()
                    .filter(a -> a.getTramo().getDiaSemana() == dia)
                    .filter(a -> !excluirOtras || a.getTipo() != TipoActividad.OTRA)
                    .map(a -> new ActividadDto(
                            a.getTramo().getHoraDia(),
                            a.getTipo().name(),
                            a.getAsignatura().getNombre(),
                            a.getGrupos().isEmpty() ? null : a.getGrupos().get(0).getNombre(),
                            a.getAula().getNombre(),
                            a.getAula().getAbreviatura()
                    ))
                    .sorted(Comparator.comparingInt(ActividadDto::getHora))
                    .collect(Collectors.toList());

            if (!actividadesDia.isEmpty()) {
                horario.put(DIAS_SEMANA[dia], actividadesDia);
            }
        }
        return horario;
    }

    private List<ActividadDto> buildActividadesPorDia(List<Actividad> actividades, int diaSemana) {
        return actividades.stream()
                .filter(a -> a.getTramo().getDiaSemana() == diaSemana)
                .map(a -> new ActividadDto(
                        a.getTramo().getHoraDia(),
                        a.getTipo().name(),
                        a.getAsignatura().getNombre(),
                        a.getGrupos().isEmpty() ? null : a.getGrupos().get(0).getNombre(),
                        a.getAula().getNombre(),
                        a.getAula().getAbreviatura()
                ))
                .sorted(Comparator.comparingInt(ActividadDto::getHora))
                .collect(Collectors.toList());
    }
}
