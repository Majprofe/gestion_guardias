package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Profesor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio optimizado para operaciones con Profesores.
 * Incluye queries optimizadas para búsquedas frecuentes.
 */
@Repository
public interface ProfesorRepository extends JpaRepository<Profesor, Long> {
    
    /**
     * Busca un profesor por email (case-insensitive).
     * OPTIMIZADO: Índice en email para búsquedas rápidas
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.email) = LOWER(:email)")
    Optional<Profesor> findByEmail(@Param("email") String email);
    
    /**
     * Busca profesores por nombre parcial (case-insensitive).
     * OPTIMIZADO: Para autocompletado y búsquedas flexibles
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY p.nombre ASC")
    List<Profesor> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);
    
    /**
     * Busca un profesor por nombre exacto (case-insensitive).
     * OPTIMIZADO: Para búsquedas precisas de nombres
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.nombre) = LOWER(:nombre)")
    Optional<Profesor> findByNombreIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca profesores por departamento.
     * ÚTIL: Para agrupaciones y estadísticas por departamento
     */
    @Query("SELECT p FROM Profesor p WHERE LOWER(p.departamento) = LOWER(:departamento) ORDER BY p.nombre ASC")
    List<Profesor> findByDepartamento(@Param("departamento") String departamento);

    /**
     * Busca profesores con guardias disponibles.
     * ÚTIL: Para asignación automática de coberturas
     */
    @Query("SELECT p FROM Profesor p WHERE p.guardiasRealizadas < p.guardiasProblematicas ORDER BY p.guardiasRealizadas ASC")
    List<Profesor> findProfesoresConGuardiasDisponibles();

    /**
     * Busca profesores con más guardias problemáticas.
     * ÚTIL: Para reportes y balanceo de cargas
     */
    @Query("SELECT p FROM Profesor p ORDER BY p.guardiasProblematicas DESC, p.guardiasRealizadas DESC")
    List<Profesor> findProfesoresOrdenadosPorGuardias();

    /**
     * Cuenta profesores por departamento.
     * ÚTIL: Para estadísticas y reportes
     */
    @Query("SELECT p.departamento, COUNT(p) FROM Profesor p GROUP BY p.departamento ORDER BY COUNT(p) DESC")
    List<Object[]> countProfesoresPorDepartamento();

    /**
     * Busca profesores que contienen el texto en nombre o email.
     * OPTIMIZADO: Para búsquedas globales
     */
    @Query("SELECT p FROM Profesor p WHERE " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%')) OR " +
           "LOWER(p.email) LIKE LOWER(CONCAT('%', :texto, '%')) " +
           "ORDER BY p.nombre ASC")
    List<Profesor> findByNombreOrEmailContaining(@Param("texto") String texto);
}
