package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    Optional<Profesor> findByEmail(String email);
    List<Profesor> findByNombreContainingIgnoreCase(String nombre);
    Optional<Profesor> findByNombreIgnoreCase(String nombre);

}
