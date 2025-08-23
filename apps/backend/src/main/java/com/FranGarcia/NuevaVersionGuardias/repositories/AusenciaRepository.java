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

    /**
     * Verifica si existe una ausencia para un profesor en una fecha y hora específica
     */
    boolean existsByProfesorAusenteEmailAndFechaAndHora(String email, LocalDate fecha, Integer hora);
    
    /**
     * Busca ausencias por fecha (devuelve Optional para manejar casos vacíos)
     */
    Optional<List<Ausencia>> findAusenciaByFecha(LocalDate fecha);
    
    /**
     * Busca ausencias por fecha (devuelve lista directa)
     */
    List<Ausencia> findByFecha(LocalDate fecha);
    
    /**
     * Busca ausencias específicas por fecha, hora y profesor
     */
    List<Ausencia> findByFechaAndHoraAndProfesorAusenteEmail(LocalDate fecha, Integer hora, String email);
    
    /**
     * Busca todas las ausencias de un profesor (case-insensitive)
     * Optimizada para usar índice de base de datos con LOWER
     */
    @Query("SELECT a FROM Ausencia a WHERE LOWER(a.profesorAusenteEmail) = LOWER(:email) ORDER BY a.fecha DESC, a.hora ASC")
    List<Ausencia> findAllByProfesorAusenteEmail(@Param("email") String email);
    
    /**
     * Busca ausencias en un rango de fechas para estadísticas
     */
    @Query("SELECT a FROM Ausencia a WHERE a.fecha BETWEEN :fechaInicio AND :fechaFin ORDER BY a.fecha ASC, a.hora ASC")
    List<Ausencia> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, 
                                     @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Cuenta ausencias por profesor en un rango de fechas
     */
    @Query("SELECT COUNT(a) FROM Ausencia a WHERE LOWER(a.profesorAusenteEmail) = LOWER(:email) AND a.fecha BETWEEN :fechaInicio AND :fechaFin")
    Long countByProfesorAndFechaBetween(@Param("email") String email, 
                                       @Param("fechaInicio") LocalDate fechaInicio, 
                                       @Param("fechaFin") LocalDate fechaFin);
}
