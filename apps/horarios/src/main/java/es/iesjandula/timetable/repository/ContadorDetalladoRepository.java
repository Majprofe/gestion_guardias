package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.ContadorDetallado;
import es.iesjandula.timetable.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContadorDetalladoRepository extends JpaRepository<ContadorDetallado, Long> {

    /**
     * Busca contador específico por profesor, día y hora
     */
    Optional<ContadorDetallado> findByProfesorAndDiaAndHora(Profesor profesor, Integer dia, Integer hora);

    /**
     * Busca contador específico por email del profesor, día y hora
     */
    @Query("SELECT cd FROM ContadorDetallado cd WHERE cd.profesor.email = :email AND cd.dia = :dia AND cd.hora = :hora")
    Optional<ContadorDetallado> findByProfesorEmailAndDiaAndHora(@Param("email") String email, 
                                                                  @Param("dia") Integer dia, 
                                                                  @Param("hora") Integer hora);

    /**
     * Obtiene todos los contadores de un profesor
     */
    List<ContadorDetallado> findByProfesor(Profesor profesor);

    /**
     * Obtiene todos los contadores de un profesor por email
     */
    @Query("SELECT cd FROM ContadorDetallado cd WHERE cd.profesor.email = :email")
    List<ContadorDetallado> findByProfesorEmail(@Param("email") String email);

    /**
     * Obtiene contadores de un día específico para todos los profesores
     */
    List<ContadorDetallado> findByDia(Integer dia);

    /**
     * Obtiene contadores de una hora específica para todos los profesores
     */
    List<ContadorDetallado> findByHora(Integer hora);

    /**
     * Obtiene contadores de un día y hora específicos
     */
    List<ContadorDetallado> findByDiaAndHora(Integer dia, Integer hora);

    /**
     * Calcula el total de guardias normales de un profesor
     */
    @Query("SELECT COALESCE(SUM(cd.guardiasNormales), 0) FROM ContadorDetallado cd WHERE cd.profesor.email = :email")
    Integer sumGuardiasNormalesByProfesorEmail(@Param("email") String email);

    /**
     * Calcula el total de guardias problemáticas de un profesor
     */
    @Query("SELECT COALESCE(SUM(cd.guardiasProblematicas), 0) FROM ContadorDetallado cd WHERE cd.profesor.email = :email")
    Integer sumGuardiasProblematicasByProfesorEmail(@Param("email") String email);

    /**
     * Calcula el total de guardias de convivencia de un profesor
     */
    @Query("SELECT COALESCE(SUM(cd.guardiasConvivencia), 0) FROM ContadorDetallado cd WHERE cd.profesor.email = :email")
    Integer sumGuardiasConvivenciaByProfesorEmail(@Param("email") String email);

    /**
     * Elimina todos los contadores de un profesor
     */
    void deleteByProfesor(Profesor profesor);

    /**
     * Elimina todos los contadores de un profesor por email
     */
    @Query("DELETE FROM ContadorDetallado cd WHERE cd.profesor.email = :email")
    void deleteByProfesorEmail(@Param("email") String email);
}
