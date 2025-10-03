package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Grupo;
import es.iesjandula.timetable.repository.GrupoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrupoService {

    private final GrupoRepository repository;

    public GrupoService(GrupoRepository repository) {
        this.repository = repository;
    }

    public List<Grupo> findAll() {
        return repository.findAll();
    }

    public Grupo findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Grupo save(Grupo grupo) { return repository.save(grupo); }

    public void deleteAll() {
        repository.deleteAll();
    }

}
