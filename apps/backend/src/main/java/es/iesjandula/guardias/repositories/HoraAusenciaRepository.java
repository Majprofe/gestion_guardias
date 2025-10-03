package es.iesjandula.guardias.repositories;

import es.iesjandula.guardias.models.HoraAusencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HoraAusenciaRepository extends JpaRepository<HoraAusencia, Long> {

    /**
     * Busca horas de ausencia por fecha y hora específica
     */
    @Query("SELECT ha FROM HoraAusencia ha JOIN ha.ausencia a WHERE a.fecha = :fecha AND ha.hora = :hora")
    List<HoraAusencia> findByFechaAndHora(@Param("fecha") LocalDate fecha, @Param("hora") Integer hora);
    
    /**
     * Busca todas las horas de ausencia de una fecha específica
     */
    @Query("SELECT ha FROM HoraAusencia ha JOIN ha.ausencia a WHERE a.fecha = :fecha ORDER BY ha.hora ASC")
    List<HoraAusencia> findByFecha(@Param("fecha") LocalDate fecha);
}
