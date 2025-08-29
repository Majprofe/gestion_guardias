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
    Optional<Cobertura> findByAusenciaId(Long ausenciaId);

    @Query("SELECT c FROM Cobertura c WHERE LOWER(c.profesorCubreEmail) = LOWER(:email)")
    List<Cobertura> findAllByProfesorCubreEmail(@Param("email") String email);

    boolean existsByProfesorCubreEmailAndAusenciaFechaAndAusenciaHora(
            String email, LocalDate fecha, Integer hora
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM Cobertura c WHERE c.ausencia.id = :ausenciaId")
    void deleteByAusenciaId(@Param("ausenciaId") Long ausenciaId);

    /**
     * Busca coberturas por fecha de ausencia y estado
     */
    @Query("SELECT c FROM Cobertura c WHERE c.ausencia.fecha = :fecha AND c.estado = :estado")
    List<Cobertura> findByAusenciaFechaAndEstado(@Param("fecha") LocalDate fecha, 
                                                @Param("estado") EstadoCobertura estado);

    /**
     * Busca coberturas de un profesor específico y estado
     */
    @Query("SELECT c FROM Cobertura c WHERE LOWER(c.profesorCubreEmail) = LOWER(:email) AND c.estado = :estado")
    List<Cobertura> findByProfesorCubreEmailAndEstado(@Param("email") String email, 
                                                     @Param("estado") EstadoCobertura estado);

    /**
     * Busca coberturas por fecha de ausencia (sin filtro de estado)
     */
    @Query("SELECT c FROM Cobertura c WHERE c.ausencia.fecha = :fecha")
    List<Cobertura> findByAusenciaFecha(@Param("fecha") LocalDate fecha);

}
