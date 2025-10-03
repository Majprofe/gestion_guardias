package es.iesjandula.guardias.repositories;

import es.iesjandula.guardias.models.ArchivoHoraAusencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestionar archivos asociados a horas de ausencia
 */
@Repository
public interface ArchivoHoraAusenciaRepository extends JpaRepository<ArchivoHoraAusencia, Long> {

    /**
     * Encuentra todos los archivos asociados a una hora de ausencia específica
     */
    @Query("SELECT a FROM ArchivoHoraAusencia a WHERE a.horaAusencia.id = :horaAusenciaId")
    List<ArchivoHoraAusencia> findByHoraAusenciaId(@Param("horaAusenciaId") Long horaAusenciaId);

    /**
     * Cuenta cuántos archivos tiene una hora de ausencia específica
     */
    @Query("SELECT COUNT(a) FROM ArchivoHoraAusencia a WHERE a.horaAusencia.id = :horaAusenciaId")
    long countByHoraAusenciaId(@Param("horaAusenciaId") Long horaAusenciaId);

    /**
     * Elimina todos los archivos asociados a una hora de ausencia
     */
    @Modifying
    @Query("DELETE FROM ArchivoHoraAusencia a WHERE a.horaAusencia.id = :horaAusenciaId")
    void deleteByHoraAusenciaId(@Param("horaAusenciaId") Long horaAusenciaId);
}
