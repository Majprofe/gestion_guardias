package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Asignatura;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AsignaturaRepository extends JpaRepository<Asignatura, Long> {
}
