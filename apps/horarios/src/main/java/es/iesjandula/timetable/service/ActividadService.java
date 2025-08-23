package es.iesjandula.timetable.service;

import es.iesjandula.timetable.model.Actividad;
import es.iesjandula.timetable.repository.ActividadRepository;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Comparator;
import java.util.List;

@Service
public class ActividadService {

    private static final Logger logger = LoggerFactory.getLogger(ActividadService.class);
    private final ActividadRepository repository;

    public ActividadService(ActividadRepository repository) {
        this.repository = repository;
    }

    public List<Actividad> findAll() {
        return repository.findAll();
    }

    public Actividad findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Actividad save(Actividad actividad) {
        return repository.save(actividad);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    /**
     * Encuentra todas las clases de un profesor por su ID, ordenadas por hora.
     * OPTIMIZADO: Ya no carga toda la tabla en memoria.
     */
    public List<Actividad> findClasesByProfesorId(Long profesorId) {
        logger.debug("Buscando clases para profesor ID: {}", profesorId);
        
        // ✅ OPTIMIZACIÓN: Usar query específica del repository
        List<Actividad> actividades = repository.findByProfesorId(profesorId);
        
        // Filtrar y ordenar solo las actividades ya obtenidas
        List<Actividad> resultado = actividades.stream()
                .filter(a -> a.getTramo() != null)
                .sorted(Comparator
                        .comparingInt((Actividad a) -> a.getTramo().getDiaSemana())
                        .thenComparingInt(a -> a.getTramo().getHoraDia())
                )
                .toList();
                
        logger.info("Encontradas {} actividades para profesor ID {}", resultado.size(), profesorId);
        return resultado;
    }
}
