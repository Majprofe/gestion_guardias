package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.TramoHorario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TramoHorarioRepository extends JpaRepository<TramoHorario, Long> {
}
