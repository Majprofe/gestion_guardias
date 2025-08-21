package com.FranGarcia.NuevaVersionGuardias.repositories;


import com.FranGarcia.NuevaVersionGuardias.models.Cobertura;
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

}
