package es.iesjandula.guardias.repositories;

import es.iesjandula.guardias.models.ContadorGuardias;
import es.iesjandula.guardias.models.DiaSemana;
import es.iesjandula.guardias.models.TipoGuardia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContadorGuardiasRepository extends JpaRepository<ContadorGuardias, Long> {

    /**
     * Busca un contador específico por profesor, día y hora
     */
    Optional<ContadorGuardias> findByProfesorEmailAndDiaSemanaAndHora(
            String profesorEmail, DiaSemana diaSemana, Integer hora);

    /**
     * Obtiene todos los contadores de un profesor
     */
    List<ContadorGuardias> findByProfesorEmailOrderByDiaSemanaAscHoraAsc(String profesorEmail);

    /**
     * Obtiene contadores por día y hora específicos
     */
    List<ContadorGuardias> findByDiaSemanaAndHoraOrderByProfesorEmailAsc(DiaSemana diaSemana, Integer hora);

    /**
     * Encuentra el profesor con menos guardias normales para un día y hora específicos
     */
    @Query("SELECT c FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasNormales ASC, c.profesorEmail ASC")
    List<ContadorGuardias> findProfesoresConMenosGuardiasNormales(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);

    /**
     * Encuentra el profesor con menos guardias problemáticas para un día y hora específicos
     */
    @Query("SELECT c FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasProblematicas ASC, c.profesorEmail ASC")
    List<ContadorGuardias> findProfesoresConMenosGuardiasProblematicas(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);

    /**
     * Encuentra el profesor con menos guardias de convivencia para un día y hora específicos
     */
    @Query("SELECT c FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasConvivencia ASC, c.profesorEmail ASC")
    List<ContadorGuardias> findProfesoresConMenosGuardiasConvivencia(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);

    /**
     * Obtiene estadísticas generales de guardias por día
     */
    @Query("SELECT c.diaSemana, SUM(c.guardiasNormales + c.guardiasProblematicas + c.guardiasConvivencia) " +
           "FROM ContadorGuardias c GROUP BY c.diaSemana ORDER BY c.diaSemana")
    List<Object[]> getEstadisticasPorDia();

    /**
     * Obtiene estadísticas generales de guardias por hora
     */
    @Query("SELECT c.hora, SUM(c.guardiasNormales + c.guardiasProblematicas + c.guardiasConvivencia) " +
           "FROM ContadorGuardias c GROUP BY c.hora ORDER BY c.hora")
    List<Object[]> getEstadisticasPorHora();

    /**
     * Obtiene emails de profesores ordenados por menor cantidad de guardias normales
     */
    @Query("SELECT c.profesorEmail FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasNormales ASC, c.profesorEmail ASC")
    List<String> findProfesoresConMenosGuardiasNormalesEmails(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);

    /**
     * Obtiene emails de profesores ordenados por menor cantidad de guardias problemáticas
     */
    @Query("SELECT c.profesorEmail FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasProblematicas ASC, c.profesorEmail ASC")
    List<String> findProfesoresConMenosGuardiasProblematicasEmails(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);

    /**
     * Obtiene emails de profesores ordenados por menor cantidad de guardias de convivencia
     */
    @Query("SELECT c.profesorEmail FROM ContadorGuardias c WHERE c.diaSemana = :dia AND c.hora = :hora " +
           "ORDER BY c.guardiasConvivencia ASC, c.profesorEmail ASC")
    List<String> findProfesoresConMenosGuardiasConvivenciaEmails(
            @Param("dia") DiaSemana dia, @Param("hora") Integer hora);
}
