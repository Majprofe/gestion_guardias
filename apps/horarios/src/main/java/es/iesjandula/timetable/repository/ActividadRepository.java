package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.model.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    /**
     * Busca todas las actividades asociadas a un profesor por su ID.
     *
     * @param profesorId ID del profesor
     * @return lista de actividades
     */
    List<Actividad> findByProfesorId(Long profesorId);
    List<Actividad> findByProfesorIdAndTramoDiaSemana(Long idProfesor, int diaSemana);
    
    /**
     * Busca actividades por tipo, día de la semana y hora del día
     * 
     * @param tipo Tipo de actividad
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @param horaDia Hora del día
     * @return Lista de actividades
     */
    List<Actividad> findByTipoAndTramoDiaSemanaAndTramoHoraDia(TipoActividad tipo, int diaSemana, int horaDia);
    
    /**
     * Busca actividades por tipo y día de la semana
     * 
     * @param tipo Tipo de actividad
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @return Lista de actividades
     */
    List<Actividad> findByTipoAndTramoDiaSemana(TipoActividad tipo, int diaSemana);
}
