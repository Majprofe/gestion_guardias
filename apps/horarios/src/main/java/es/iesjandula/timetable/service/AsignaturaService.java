package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Asignatura;
import es.iesjandula.timetable.repository.AsignaturaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AsignaturaService {

    private final AsignaturaRepository repository;

    public AsignaturaService(AsignaturaRepository repository) {
        this.repository = repository;
    }

    public List<Asignatura> findAll() {
        return repository.findAll();
    }

    public Asignatura findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Asignatura save(Asignatura asignatura) { return repository.save(asignatura); }

    public void deleteAll() {
        repository.deleteAll();
    }

}
