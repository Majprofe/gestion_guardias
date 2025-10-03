package es.iesjandula.timetable.repository;

import es.iesjandula.timetable.model.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Long> {
    
    /**
     * Busca un grupo por su abreviatura
     */
    Optional<Grupo> findByAbreviatura(String abreviatura);
}
