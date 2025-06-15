package com.FranGarcia.NuevaVersionGuardias.services;

import com.FranGarcia.NuevaVersionGuardias.dto.AusenciaConGuardiasDTO;
import com.FranGarcia.NuevaVersionGuardias.models.*;
import com.FranGarcia.NuevaVersionGuardias.repositories.AusenciaRepository;
import com.FranGarcia.NuevaVersionGuardias.repositories.CoberturaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

import java.util.stream.Collectors;
@Service
public class AusenciaService {

    private static final Logger logger = LoggerFactory.getLogger(AusenciaService.class);

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @Autowired
    private CoberturaRepository coberturaRepository;

    /**
     * Guarda una nueva ausencia y asigna una cobertura con el profesor en guardia proporcionado.
     *
     * @param dto Objeto que contiene los detalles de la ausencia y el profesor en guardia.
     * @return La ausencia guardada.
     * @throws RuntimeException Si la ausencia ya existe para el profesor en la misma fecha y hora.
     */
    public Ausencia guardarYAsignarCobertura(AusenciaConGuardiasDTO dto) {
        logger.debug("Intentando guardar ausencia: {}", dto);

        if (ausenciaRepository.existsByProfesorAusenteEmailAndFechaAndHora(
                dto.getProfesorAusenteEmail(), dto.getFecha(), dto.getHora())) {
            logger.warn("La ausencia ya existe para el profesor {} en fecha {} y hora {}",
                    dto.getProfesorAusenteEmail(), dto.getFecha(), dto.getHora());
            throw new RuntimeException("La ausencia ya existe");
        }

        // Crear ausencia
        Ausencia ausencia = new Ausencia();
        ausencia.setProfesorAusenteEmail(dto.getProfesorAusenteEmail());
        ausencia.setFecha(dto.getFecha());
        ausencia.setHora(dto.getHora());
        ausencia.setTarea(dto.getTarea());
        ausencia.setGrupo(dto.getGrupo());
        ausencia.setAula(dto.getAula());

        ausencia = ausenciaRepository.save(ausencia);
        logger.info("Ausencia guardada con ID {}", ausencia.getId());

        // Crear cobertura directamente con el profesor proporcionado
        Cobertura cobertura = new Cobertura();
        cobertura.setAusencia(ausencia);
        cobertura.setProfesorCubreEmail(dto.getProfesorEnGuardiaEmail());
        cobertura.setGrupo(dto.getGrupo());
        cobertura.setAula(dto.getAula());

        coberturaRepository.save(cobertura);
        logger.info("Cobertura creada para la ausencia con ID {} por el profesor {}", ausencia.getId(), dto.getProfesorEnGuardiaEmail());

        return ausencia;
    }


    /**
     * Lista las ausencias para una fecha y hora específicas.
     *
     * @param fecha La fecha de las ausencias a buscar.
     * @param hora La hora de las ausencias a buscar.
     * @return Una lista de ausencias que coinciden con la fecha y hora proporcionadas.
     */
    public Optional<List<Ausencia>> listarPorFechaHora(LocalDate fecha, Integer hora) {
        logger.debug("Buscando ausencias para fecha {} y hora {}", fecha, hora);
        Optional<List<Ausencia>> faltasDia = ausenciaRepository.findAusenciaByFecha(fecha);
        if (faltasDia.isPresent()) {
            List<Ausencia> coincidencias = faltasDia.get().stream()
                    .filter(ausencia -> ausencia.getHora().equals(hora))
                    .collect(Collectors.toList());

            logger.info("Encontradas {} ausencias para la hora {}", coincidencias.size(), hora);
            return coincidencias.isEmpty() ? Optional.empty() : Optional.of(coincidencias);
        } else {
            logger.info("No se encontraron ausencias para la fecha {}", fecha);
            return Optional.empty();
        }
    }

    /**
     * Lista las ausencias para una fecha específica, agrupadas por hora.
     *
     * @param fecha La fecha de las ausencias a buscar.
     * @return Un mapa que agrupa las ausencias por hora.
     */
    public Map<String, List<AusenciaConGuardiasDTO>> listarAusenciasPorFechaAgrupadasPorHora(LocalDate fecha) {
        logger.debug("Listando ausencias agrupadas por hora para la fecha {}", fecha);
        List<Ausencia> ausencias = ausenciaRepository.findByFecha(fecha);

        Map<String, List<AusenciaConGuardiasDTO>> ausenciasPorHora = new TreeMap<>(Comparator.comparingInt(Integer::parseInt));

        for (Ausencia ausencia : ausencias) {
            String hora = String.valueOf(ausencia.getHora());

            String profesorCubreEmail = ausencia.getCobertura() != null
                    ? ausencia.getCobertura().getProfesorCubreEmail()
                    : null;

            AusenciaConGuardiasDTO dto = new AusenciaConGuardiasDTO();
            dto.setId(ausencia.getId());
            dto.setFecha(ausencia.getFecha());
            dto.setHora(ausencia.getHora());
            dto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
            dto.setTarea(ausencia.getTarea());
            dto.setProfesorEnGuardiaEmail(profesorCubreEmail);
            dto.setGrupo(ausencia.getGrupo());
            dto.setAula(ausencia.getAula());

            ausenciasPorHora.computeIfAbsent(hora, k -> new ArrayList<>()).add(dto);
        }

        logger.info("Total de horas con ausencias encontradas: {}", ausenciasPorHora.size());
        return ausenciasPorHora;
    }

    /**
     * Genera un histórico completo de todas las ausencias registradas.
     *
     * @return Un mapa que agrupa las ausencias por fecha y hora.
     */
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoFaltas() {
        logger.debug("Generando histórico completo de faltas");
        List<Ausencia> ausenciasTodas = ausenciaRepository.findAll();
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = new TreeMap<>();

        for (Ausencia ausencia : ausenciasTodas) {
            LocalDate fecha = ausencia.getFecha();
            String hora = String.valueOf(ausencia.getHora());

            String profesorCubreEmail = ausencia.getCobertura() != null
                    ? ausencia.getCobertura().getProfesorCubreEmail()
                    : null;

            AusenciaConGuardiasDTO dto = new AusenciaConGuardiasDTO();
            dto.setId(ausencia.getId());
            dto.setFecha(fecha);
            dto.setHora(ausencia.getHora());
            dto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
            dto.setTarea(ausencia.getTarea());
            dto.setProfesorEnGuardiaEmail(profesorCubreEmail);
            dto.setAula(ausencia.getAula());
            dto.setGrupo(ausencia.getGrupo());
            historico
                    .computeIfAbsent(fecha, k -> new TreeMap<>(Comparator.comparingInt(Integer::parseInt)))
                    .computeIfAbsent(hora, k -> new ArrayList<>())
                    .add(dto);
        }

        logger.info("Histórico generado con {} fechas distintas", historico.size());
        return historico;
    }


    /**
     * Genera un histórico de las ausencias de un profesor específico.
     *
     * @param emailProfesor El email del profesor para el que se busca el histórico.
     * @return Un mapa que agrupa las ausencias del profesor por fecha y hora.
     */
    public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoFaltasPorProfesor(String emailProfesor) {
        logger.debug("Generando histórico de faltas para el profesor {}", emailProfesor);
        List<Ausencia> ausenciasProfesor = ausenciaRepository.findAllByProfesorAusenteEmail(emailProfesor);
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = new TreeMap<>();

        for (Ausencia ausencia : ausenciasProfesor) {
            LocalDate fecha = ausencia.getFecha();
            String hora = String.valueOf(ausencia.getHora());

            String profesorCubreEmail = ausencia.getCobertura() != null
                    ? ausencia.getCobertura().getProfesorCubreEmail()
                    : null;

            AusenciaConGuardiasDTO dto = new AusenciaConGuardiasDTO();
            dto.setId(ausencia.getId());
            dto.setFecha(fecha);
            dto.setHora(ausencia.getHora());
            dto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
            dto.setTarea(ausencia.getTarea());
            dto.setProfesorEnGuardiaEmail(profesorCubreEmail);

            historico
                    .computeIfAbsent(fecha, k -> new TreeMap<>(Comparator.comparingInt(Integer::parseInt)))
                    .computeIfAbsent(hora, k -> new ArrayList<>())
                    .add(dto);
        }

        logger.info("Histórico para profesor {} generado con {} fechas distintas", emailProfesor, historico.size());
        return historico;
    }

    /**
     * Obtiene las ausencias de un profesor específico.
     *
     * @param email El email del profesor cuyas ausencias se desean obtener.
     * @return Una lista de objetos {@link AusenciaConGuardiasDTO} que contienen los detalles de las ausencias del profesor.
     */
    public List<AusenciaConGuardiasDTO> obtenerAusenciasPorProfesor(String email) {
        System.out.println("🟡 Buscando ausencias para: " + email);
        logger.debug("Obteniendo ausencias para el profesor {}", email);
        List<Ausencia> ausencias = ausenciaRepository.findAllByProfesorAusenteEmail(email);

        List<AusenciaConGuardiasDTO> resultado = ausencias.stream()
                .map(a -> {
                    AusenciaConGuardiasDTO dto = new AusenciaConGuardiasDTO();
                    dto.setId(a.getId());
                    dto.setFecha(a.getFecha());
                    dto.setHora(a.getHora());
                    dto.setProfesorAusenteEmail(a.getProfesorAusenteEmail());
                    dto.setTarea(a.getTarea());
                    dto.setAula(a.getAula());
                    dto.setGrupo(a.getGrupo());
                    if (a.getCobertura() != null) {
                        dto.setProfesorEnGuardiaEmail(a.getCobertura().getProfesorCubreEmail());
                    }
                    return dto;
                })
                .collect(Collectors.toList());

        logger.info("Total de ausencias encontradas para el profesor {}: {}", email, resultado.size());
        return resultado;
    }

    public void eliminarPorId(Long id) {
        ausenciaRepository.deleteById(id);
    }

}