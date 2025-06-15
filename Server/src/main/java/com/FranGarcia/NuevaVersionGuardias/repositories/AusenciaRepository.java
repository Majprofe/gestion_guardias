package com.FranGarcia.NuevaVersionGuardias.repositories;

import com.FranGarcia.NuevaVersionGuardias.models.Ausencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AusenciaRepository extends JpaRepository<Ausencia, Long> {

    boolean existsByProfesorAusenteEmailAndFechaAndHora(String email, LocalDate fecha, Integer hora);
    Optional<List<Ausencia>> findAusenciaByFecha(LocalDate fecha);
    List<Ausencia> findByFecha(LocalDate fecha);
    List<Ausencia> findByFechaAndHoraAndProfesorAusenteEmail(LocalDate fecha, Integer hora, String email);
    @Query("SELECT a FROM Ausencia a WHERE LOWER(a.profesorAusenteEmail) = LOWER(:email)")
    List<Ausencia> findAllByProfesorAusenteEmail(@Param("email") String email);



    /* CODIGO ANTIGUO
    List<Ausencia> findByFecha(LocalDate fecha);
    List<Ausencia> findAllByProfesorAusenteEmail(String email);
    List<Ausencia> findByFechaAndHoraAndProfesorAusenteEmail(LocalDate fecha, Integer hora, String emailProfesor);
    List<Ausencia> findByProfesorAusenteEmail(String email);
    */
}
