package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Profesor;
import es.iesjandula.timetable.repository.ProfesorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProfesorService {

    private final ProfesorRepository repository;

    public ProfesorService(ProfesorRepository repository) {
        this.repository = repository;
    }

    public List<Profesor> findAll() {
        return repository.findAll();
    }

    public Profesor findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Profesor findByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public Profesor save(Profesor profesor) {
        return repository.save(profesor);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
    
    @Transactional
    public Profesor incrementarGuardiaRealizada(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() + 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor incrementarGuardiaProblematica(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() + 1);
            profesor.setGuardiasProblematicas(profesor.getGuardiasProblematicas() + 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor decrementarGuardiaRealizada(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null && profesor.getGuardiasRealizadas() > 0) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() - 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor decrementarGuardiaProblematica(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null && profesor.getGuardiasRealizadas() > 0 && profesor.getGuardiasProblematicas() > 0) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() - 1);
            profesor.setGuardiasProblematicas(profesor.getGuardiasProblematicas() - 1);
            return repository.save(profesor);
        }
        return null;
    }

    public List<String> buscarNombresParciales(String nombreParcial) {
        return repository.findByNombreContainingIgnoreCase(nombreParcial)
                .stream()
                .map(Profesor::getNombre)
                .collect(Collectors.toList());
    }
    public Optional<String> obtenerEmailPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre)
                .map(Profesor::getEmail);
    }
}
