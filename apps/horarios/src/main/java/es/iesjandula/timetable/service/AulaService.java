package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Aula;
import es.iesjandula.timetable.repository.AulaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AulaService {

    private final AulaRepository repository;

    public AulaService(AulaRepository repository) {
        this.repository = repository;
    }

    public List<Aula> findAll() {
        return repository.findAll();
    }

    public Aula findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Aula save(Aula aula) { return repository.save(aula); }

    public void deleteAll() {
        repository.deleteAll();
    }

}
