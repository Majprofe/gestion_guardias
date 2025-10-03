package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.model.TipoActividad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio optimizado para operaciones con Actividades.
 * Incluye queries específicas para mejorar el rendimiento.
 */
@Repository
public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    /**
     * Busca todas las actividades asociadas a un profesor por su ID.
     * OPTIMIZADO: Query específica en lugar de findAll() + filtro
     *
     * @param profesorId ID del profesor
     * @return lista de actividades ordenadas por día y hora
     */
    @Query("SELECT a FROM Actividad a " +
           "WHERE a.profesor.id = :profesorId " +
           "ORDER BY a.tramo.diaSemana ASC, a.tramo.horaDia ASC")
    List<Actividad> findByProfesorId(@Param("profesorId") Long profesorId);
    
    /**
     * Busca actividades de un profesor en un día específico.
     * OPTIMIZADO: Evita cargar actividades innecesarias
     *
     * @param idProfesor ID del profesor
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @return lista de actividades del día ordenadas por hora
     */
    @Query("SELECT a FROM Actividad a " +
           "WHERE a.profesor.id = :idProfesor AND a.tramo.diaSemana = :diaSemana " +
           "ORDER BY a.tramo.horaDia ASC")
    List<Actividad> findByProfesorIdAndTramoDiaSemana(@Param("idProfesor") Long idProfesor, 
                                                      @Param("diaSemana") int diaSemana);
    
    /**
     * Busca actividades por tipo, día de la semana y hora del día.
     * OPTIMIZADO: Para búsquedas específicas de guardias
     * 
     * @param tipo Tipo de actividad
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @param horaDia Hora del día
     * @return Lista de actividades
     */
    @Query("SELECT a FROM Actividad a " +
           "WHERE a.tipo = :tipo AND a.tramo.diaSemana = :diaSemana AND a.tramo.horaDia = :horaDia " +
           "ORDER BY a.profesor.nombre ASC")
    List<Actividad> findByTipoAndTramoDiaSemanaAndTramoHoraDia(@Param("tipo") TipoActividad tipo, 
                                                               @Param("diaSemana") int diaSemana, 
                                                               @Param("horaDia") int horaDia);
    
    /**
     * Busca actividades por tipo y día de la semana.
     * OPTIMIZADO: Para obtener todas las guardias de un día
     * 
     * @param tipo Tipo de actividad
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @return Lista de actividades ordenadas por hora
     */
    @Query("SELECT a FROM Actividad a " +
           "WHERE a.tipo = :tipo AND a.tramo.diaSemana = :diaSemana " +
           "ORDER BY a.tramo.horaDia ASC, a.profesor.nombre ASC")
    List<Actividad> findByTipoAndTramoDiaSemana(@Param("tipo") TipoActividad tipo, 
                                                @Param("diaSemana") int diaSemana);

    /**
     * Cuenta las actividades de un profesor por tipo.
     * ÚTIL: Para estadísticas y validaciones
     * 
     * @param profesorId ID del profesor
     * @param tipo Tipo de actividad
     * @return Número de actividades del tipo especificado
     */
    @Query("SELECT COUNT(a) FROM Actividad a " +
           "WHERE a.profesor.id = :profesorId AND a.tipo = :tipo")
    Long countByProfesorIdAndTipo(@Param("profesorId") Long profesorId, 
                                  @Param("tipo") TipoActividad tipo);

    /**
     * Busca actividades excluyendo un tipo específico.
     * OPTIMIZADO: Para horarios sin actividades tipo OTRA
     * 
     * @param profesorId ID del profesor
     * @param tipoExcluido Tipo de actividad a excluir
     * @return Lista de actividades ordenadas
     */
    @Query("SELECT a FROM Actividad a " +
           "WHERE a.profesor.id = :profesorId AND a.tipo != :tipoExcluido " +
           "ORDER BY a.tramo.diaSemana ASC, a.tramo.horaDia ASC")
    List<Actividad> findByProfesorIdExcludingTipo(@Param("profesorId") Long profesorId, 
                                                  @Param("tipoExcluido") TipoActividad tipoExcluido);
}
