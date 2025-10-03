package es.iesjandula.guardias.repositories;


import es.iesjandula.guardias.models.Cobertura;
import es.iesjandula.guardias.models.EstadoCobertura;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CoberturaRepository extends JpaRepository<Cobertura, Long> {
    
    // Métodos actualizados para usar horaAusencia
    Optional<Cobertura> findByHoraAusenciaId(Long horaAusenciaId);

    @Query("SELECT c FROM Cobertura c WHERE LOWER(c.profesorCubreEmail) = LOWER(:email)")
    List<Cobertura> findAllByProfesorCubreEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cobertura c " +
           "JOIN c.horaAusencia ha JOIN ha.ausencia a " +
           "WHERE LOWER(c.profesorCubreEmail) = LOWER(:email) AND a.fecha = :fecha AND ha.hora = :hora")
    boolean existsByProfesorCubreEmailAndFechaAndHora(
            @Param("email") String email, 
            @Param("fecha") LocalDate fecha, 
            @Param("hora") Integer hora
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Cobertura c WHERE c.horaAusencia.id = :horaAusenciaId")
    void deleteByHoraAusenciaId(@Param("horaAusenciaId") Long horaAusenciaId);

    /**
     * Busca coberturas por fecha y estado
     */
    @Query("SELECT c FROM Cobertura c JOIN c.horaAusencia ha JOIN ha.ausencia a " +
           "WHERE a.fecha = :fecha AND c.estado = :estado")
    List<Cobertura> findByFechaAndEstado(@Param("fecha") LocalDate fecha, 
                                         @Param("estado") EstadoCobertura estado);

    /**
     * Busca coberturas de un profesor específico y estado
     */
    @Query("SELECT c FROM Cobertura c WHERE LOWER(c.profesorCubreEmail) = LOWER(:email) AND c.estado = :estado")
    List<Cobertura> findByProfesorCubreEmailAndEstado(@Param("email") String email, 
                                                     @Param("estado") EstadoCobertura estado);

    /**
     * Busca coberturas por fecha (sin filtro de estado)
     */
    @Query("SELECT c FROM Cobertura c JOIN c.horaAusencia ha JOIN ha.ausencia a WHERE a.fecha = :fecha")
    List<Cobertura> findByFecha(@Param("fecha") LocalDate fecha);

    /**
     * Busca coberturas por fecha y hora específicas
     */
    @Query("SELECT c FROM Cobertura c JOIN c.horaAusencia ha JOIN ha.ausencia a " +
           "WHERE a.fecha = :fecha AND ha.hora = :hora")
    List<Cobertura> findByHoraAusencia_Ausencia_FechaAndHoraAusencia_NumeroHora(
            @Param("fecha") LocalDate fecha, 
            @Param("hora") Integer hora);

}
