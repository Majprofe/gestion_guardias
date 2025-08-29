package es.iesjandula.timetable.service;

import es.iesjandula.timetable.dto.ProfesorGuardiaDto;
import es.iesjandula.timetable.dto.ActualizarContadorDto;
import es.iesjandula.timetable.model.Profesor;
import es.iesjandula.timetable.model.ContadorDetallado;
import es.iesjandula.timetable.repository.ProfesorRepository;
import es.iesjandula.timetable.repository.ContadorDetalladoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProfesorService {

    private final ProfesorRepository repository;
    private final ContadorDetalladoRepository contadorDetalladoRepository;

    public ProfesorService(ProfesorRepository repository, ContadorDetalladoRepository contadorDetalladoRepository) {
        this.repository = repository;
        this.contadorDetalladoRepository = contadorDetalladoRepository;
    }

    public List<Profesor> findAll() {
        return repository.findAll();
    }

    public Profesor findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Profesor findByEmail(String email) {
        return repository.findByEmail(email).orElse(null);
    }

    public Profesor save(Profesor profesor) {
        return repository.save(profesor);
    }

    public void deleteAll() {
        repository.deleteAll();
    }
    
    @Transactional
    public Profesor incrementarGuardiaRealizada(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() + 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor incrementarGuardiaProblematica(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() + 1);
            profesor.setGuardiasProblematicas(profesor.getGuardiasProblematicas() + 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor decrementarGuardiaRealizada(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null && profesor.getGuardiasRealizadas() > 0) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() - 1);
            return repository.save(profesor);
        }
        return null;
    }
    
    @Transactional
    public Profesor decrementarGuardiaProblematica(Long id) {
        Profesor profesor = findById(id);
        if (profesor != null && profesor.getGuardiasRealizadas() > 0 && profesor.getGuardiasProblematicas() > 0) {
            profesor.setGuardiasRealizadas(profesor.getGuardiasRealizadas() - 1);
            profesor.setGuardiasProblematicas(profesor.getGuardiasProblematicas() - 1);
            return repository.save(profesor);
        }
        return null;
    }

    public List<String> buscarNombresParciales(String nombreParcial) {
        return repository.findByNombreContainingIgnoreCase(nombreParcial)
                .stream()
                .map(Profesor::getNombre)
                .collect(Collectors.toList());
    }
    
    public Optional<String> obtenerEmailPorNombre(String nombre) {
        return repository.findByNombreIgnoreCase(nombre)
                .map(Profesor::getEmail);
    }

    // ==================== NUEVOS MÉTODOS PARA INTEGRACIÓN ====================

    /**
     * Obtiene todos los profesores con información de guardias para la integración
     */
    public List<ProfesorGuardiaDto> getProfesoresConGuardias() {
        List<Profesor> profesores = repository.findAll();
        return profesores.stream()
                .map(this::convertirAProfesorGuardiaDto)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene información detallada de guardias de un profesor específico
     */
    public ProfesorGuardiaDto getProfesorConGuardias(String email) {
        Profesor profesor = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado: " + email));
        
        return convertirAProfesorGuardiaDto(profesor);
    }

    /**
     * Actualiza contadores de guardias de un profesor
     */
    @Transactional
    public ProfesorGuardiaDto actualizarContadores(ActualizarContadorDto contadorDto) {
        Profesor profesor = repository.findByEmail(contadorDto.getEmail())
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado: " + contadorDto.getEmail()));

        // Buscar o crear contador detallado
        ContadorDetallado contador = contadorDetalladoRepository
                .findByProfesorAndDiaAndHora(profesor, contadorDto.getDia(), contadorDto.getHora())
                .orElse(new ContadorDetallado(profesor, contadorDto.getDia(), contadorDto.getHora()));

        // Aplicar cambios según el tipo de operación
        if ("INCREMENT".equals(contadorDto.getOperacion())) {
            if (contadorDto.getGuardiasNormales() != null) {
                contador.incrementarContador("normales", contadorDto.getGuardiasNormales());
            }
            if (contadorDto.getGuardiasProblematicas() != null) {
                contador.incrementarContador("problematicas", contadorDto.getGuardiasProblematicas());
            }
            if (contadorDto.getGuardiasConvivencia() != null) {
                contador.incrementarContador("convivencia", contadorDto.getGuardiasConvivencia());
            }
        } else { // SET por defecto
            if (contadorDto.getGuardiasNormales() != null) {
                contador.setGuardiasNormales(contadorDto.getGuardiasNormales());
            }
            if (contadorDto.getGuardiasProblematicas() != null) {
                contador.setGuardiasProblematicas(contadorDto.getGuardiasProblematicas());
            }
            if (contadorDto.getGuardiasConvivencia() != null) {
                contador.setGuardiasConvivencia(contadorDto.getGuardiasConvivencia());
            }
        }

        contadorDetalladoRepository.save(contador);

        // Actualizar también los contadores globales del profesor
        actualizarContadoresGlobales(profesor);

        return convertirAProfesorGuardiaDto(profesor);
    }

    /**
     * Actualiza múltiples contadores en lote
     */
    @Transactional
    public List<ProfesorGuardiaDto> actualizarContadoresLote(List<ActualizarContadorDto> contadoresDto) {
        return contadoresDto.stream()
                .map(this::actualizarContadores)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene contador específico para un día y hora
     */
    public ProfesorGuardiaDto.ContadorDetalle getContadorEspecifico(String email, Integer dia, Integer hora) {
        Optional<ContadorDetallado> contador = contadorDetalladoRepository
                .findByProfesorEmailAndDiaAndHora(email, dia, hora);

        if (contador.isPresent()) {
            ContadorDetallado c = contador.get();
            return new ProfesorGuardiaDto.ContadorDetalle(
                    c.getGuardiasNormales(),
                    c.getGuardiasProblematicas(),
                    c.getGuardiasConvivencia()
            );
        } else {
            return new ProfesorGuardiaDto.ContadorDetalle(0, 0, 0);
        }
    }

    /**
     * Resetea todos los contadores de un profesor
     */
    @Transactional
    public void resetearContadores(String email) {
        Profesor profesor = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Profesor no encontrado: " + email));

        // Resetear contadores detallados
        contadorDetalladoRepository.deleteByProfesor(profesor);

        // Resetear contadores globales
        profesor.setGuardiasRealizadas(0);
        profesor.setGuardiasProblematicas(0);
        repository.save(profesor);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Convierte un Profesor a ProfesorGuardiaDto con contadores detallados
     */
    private ProfesorGuardiaDto convertirAProfesorGuardiaDto(Profesor profesor) {
        // Obtener contadores detallados
        List<ContadorDetallado> contadores = contadorDetalladoRepository.findByProfesor(profesor);
        Map<String, ProfesorGuardiaDto.ContadorDetalle> contadoresPorDiaHora = new HashMap<>();

        for (ContadorDetallado contador : contadores) {
            String clave = contador.getDiaSemana() + "-" + contador.getHoraDia();
            contadoresPorDiaHora.put(clave, new ProfesorGuardiaDto.ContadorDetalle(
                    contador.getGuardiasNormales(),
                    contador.getGuardiasProblematicas(),
                    contador.getGuardiasConvivencia()
            ));
        }

        // Calcular totales de guardias de convivencia
        Integer guardiasConvivencia = contadorDetalladoRepository.sumGuardiasConvivenciaByProfesorEmail(profesor.getEmail());

        return new ProfesorGuardiaDto(
                profesor.getId(),
                profesor.getNombre(),
                profesor.getEmail(),
                profesor.getAbreviatura(),
                profesor.getDepartamento(), // Ya es String directamente
                profesor.getGuardiasRealizadas(),
                profesor.getGuardiasProblematicas(),
                guardiasConvivencia != null ? guardiasConvivencia : 0,
                contadoresPorDiaHora
        );
    }

    /**
     * Actualiza los contadores globales del profesor basándose en los contadores detallados
     */
    @Transactional
    private void actualizarContadoresGlobales(Profesor profesor) {
        Integer totalNormales = contadorDetalladoRepository.sumGuardiasNormalesByProfesorEmail(profesor.getEmail());
        Integer totalProblematicas = contadorDetalladoRepository.sumGuardiasProblematicasByProfesorEmail(profesor.getEmail());

        profesor.setGuardiasRealizadas(totalNormales != null ? totalNormales : 0);
        profesor.setGuardiasProblematicas(totalProblematicas != null ? totalProblematicas : 0);
        
        repository.save(profesor);
    }
}
