package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Aula;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AulaRepository extends JpaRepository<Aula, Long> {
}
