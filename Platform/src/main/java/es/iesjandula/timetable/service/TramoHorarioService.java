package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.TramoHorario;
import es.iesjandula.timetable.repository.TramoHorarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TramoHorarioService {

    private final TramoHorarioRepository repository;

    public TramoHorarioService(TramoHorarioRepository repository) {
        this.repository = repository;
    }

    public List<TramoHorario> findAll() {
        return repository.findAll();
    }

    public TramoHorario findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public TramoHorario save(TramoHorario tramoHorario) { return repository.save(tramoHorario); }

    public void deleteAll() {
        repository.deleteAll();
    }

}
