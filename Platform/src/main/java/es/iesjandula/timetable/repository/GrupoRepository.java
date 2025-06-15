package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
}
