package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.service.ActividadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/actividades")
@Tag(name = "Actividades", description = "Operaciones sobre actividades y horarios de profesores")
public class ActividadController {

    private final ActividadService service;

    public ActividadController(ActividadService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todas las actividades")
    public List<Actividad> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener una actividad por ID")
    public Actividad getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/profesor/{profesorId}/horario")
    @Operation(summary = "Obtener el horario completo de un profesor agrupado por día")
    public Map<Integer, List<Actividad>> getHorarioCompleto(
            @Parameter(description = "ID del profesor") @PathVariable Long profesorId) {
        List<Actividad> actividades = service.findClasesByProfesorId(profesorId);

        return actividades.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getTramo().getDiaSemana(),
                        TreeMap::new, // ordena los días de la semana
                        Collectors.toList()
                ));
    }

    @GetMapping("/profesor/{profesorId}/dia/{dia}")
    @Operation(summary = "Obtener el horario de un profesor para un día concreto")
    public List<Actividad> getHorarioPorDia(
            @Parameter(description = "ID del profesor") @PathVariable Long profesorId,
            @Parameter(description = "Día de la semana (1=Lunes, 2=Martes, ..., 5=Viernes)") @PathVariable int dia) {

        return service.findClasesByProfesorId(profesorId).stream()
                .filter(a -> a.getTramo().getDiaSemana() == dia)
                .toList();
    }
}
