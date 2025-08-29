package es.iesjandula.guardias.repositories;

import es.iesjandula.guardias.models.ArchivoAusencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArchivoAusenciaRepository extends JpaRepository<ArchivoAusencia, Long> {

    /**
     * Obtiene todos los archivos de una ausencia específica
     */
    List<ArchivoAusencia> findByAusenciaIdOrderByFechaSubidaDesc(Long ausenciaId);

    /**
     * Obtiene archivos por tipo de archivo
     */
    List<ArchivoAusencia> findByTipoArchivoContainingIgnoreCaseOrderByFechaSubidaDesc(String tipoArchivo);

    /**
     * Obtiene archivos subidos en un rango de fechas
     */
    List<ArchivoAusencia> findByFechaSubidaBetweenOrderByFechaSubidaDesc(
            LocalDateTime fechaInicio, LocalDateTime fechaFin);

    /**
     * Cuenta el número de archivos por ausencia
     */
    @Query("SELECT COUNT(a) FROM ArchivoAusencia a WHERE a.ausencia.id = :ausenciaId")
    Long countByAusenciaId(@Param("ausenciaId") Long ausenciaId);

    /**
     * Calcula el tamaño total de archivos por ausencia
     */
    @Query("SELECT COALESCE(SUM(a.tamanoArchivo), 0) FROM ArchivoAusencia a WHERE a.ausencia.id = :ausenciaId")
    Long getTamanoTotalByAusenciaId(@Param("ausenciaId") Long ausenciaId);

    /**
     * Obtiene archivos por profesor (a través de la ausencia)
     */
    @Query("SELECT a FROM ArchivoAusencia a WHERE a.ausencia.profesorAusenteEmail = :profesorEmail " +
           "ORDER BY a.fechaSubida DESC")
    List<ArchivoAusencia> findByProfesorEmail(@Param("profesorEmail") String profesorEmail);

    /**
     * Elimina archivos antiguos (más de X días)
     */
    @Query("DELETE FROM ArchivoAusencia a WHERE a.fechaSubida < :fechaLimite")
    void deleteArchivosAntiguos(@Param("fechaLimite") LocalDateTime fechaLimite);

    /**
     * Obtiene la suma total del tamaño de todos los archivos
     */
    @Query("SELECT SUM(a.tamanoArchivo) FROM ArchivoAusencia a")
    Optional<Long> sumTamañoArchivos();
}
