package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.repository.ActividadRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class ActividadService {

    private final ActividadRepository repository;

    public ActividadService(ActividadRepository repository) {
        this.repository = repository;
    }

    public List<Actividad> findAll() {
        return repository.findAll();
    }

    public Actividad findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Actividad save(Actividad actividad) {
        return repository.save(actividad);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    /**
     * Encuentra todas las clases de un profesor por su ID, ordenadas por hora.
     */
    public List<Actividad> findClasesByProfesorId(Long profesorId) {
        return repository.findAll().stream()
                .filter(a -> a.getProfesor() != null && a.getProfesor().getId().equals(profesorId))
                .filter(a -> a.getTramo() != null)
                .sorted(Comparator
                        .comparingInt((Actividad a) -> a.getTramo().getDiaSemana())
                        .thenComparingInt(a -> a.getTramo().getHoraDia())
                )
                .toList();
    }
}
