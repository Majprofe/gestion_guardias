package com.FranGarcia.NuevaVersionGuardias.services;

import com.FranGarcia.NuevaVersionGuardias.dto.CoberturaDTO;
import com.FranGarcia.NuevaVersionGuardias.models.Ausencia;
import com.FranGarcia.NuevaVersionGuardias.models.Cobertura;
import com.FranGarcia.NuevaVersionGuardias.repositories.AusenciaRepository;
import com.FranGarcia.NuevaVersionGuardias.repositories.CoberturaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CoberturaService {

    private static final Logger logger = LoggerFactory.getLogger(CoberturaService.class);

    @Autowired
    private CoberturaRepository coberturaRepository;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    /**
     * Guarda una nueva cobertura basada en la información proporcionada por un DTO.
     *
     * @param dto El objeto CoberturaDTO que contiene la información de la cobertura a guardar.
     * @return La entidad Cobertura guardada.
     * @throws RuntimeException si la ausencia no existe o ya tiene una cobertura asignada.
     */
    public Cobertura guardarDesdeDto(CoberturaDTO dto) {
        logger.debug("Intentando guardar cobertura desde DTO: {}", dto);

        Ausencia ausencia = ausenciaRepository.findById(dto.getAusenciaId())
                .orElseThrow(() -> {
                    logger.error("Ausencia con ID {} no encontrada", dto.getAusenciaId());
                    return new RuntimeException("Ausencia no encontrada");
                });

        if (ausencia.getCobertura() != null) {
            logger.warn("La ausencia con ID {} ya tiene una cobertura asignada", ausencia.getId());
            throw new RuntimeException("La ausencia ya tiene una cobertura asignada");
        }

        Cobertura cobertura = new Cobertura();
        cobertura.setAusencia(ausencia);
        cobertura.setProfesorCubreEmail(dto.getProfesorCubreEmail());
        cobertura.setGrupo(dto.getGrupo());
        cobertura.setAula(dto.getAula());

        Cobertura coberturaGuardada = coberturaRepository.save(cobertura);
        logger.info("Cobertura guardada con ID {}", coberturaGuardada.getId());

        return coberturaGuardada;
    }

    /**
     * Lista todas las coberturas registradas en el sistema.
     *
     * @return Una lista de todas las coberturas.
     */
    public List<Cobertura> listar() {
        logger.debug("Listando todas las coberturas");
        List<Cobertura> coberturas = coberturaRepository.findAll();
        logger.info("Total de coberturas encontradas: {}", coberturas.size());
        return coberturas;
    }

    /**
     * Busca una cobertura asociada a una ausencia específica.
     *
     * @param ausenciaId El ID de la ausencia.
     * @return Un Optional con la cobertura encontrada o vacío si no existe.
     */
    public Optional<Cobertura> findByAusenciaId(Long ausenciaId) {
        logger.debug("Buscando cobertura para ausencia con ID {}", ausenciaId);
        Optional<Cobertura> cobertura = coberturaRepository.findByAusenciaId(ausenciaId);
        if (cobertura.isPresent()) {
            logger.info("Cobertura encontrada para la ausencia con ID {}", ausenciaId);
        } else {
            logger.warn("No se encontró cobertura para la ausencia con ID {}", ausenciaId);
        }
        return cobertura;
    }

    /**
     * Elimina la cobertura asociada a una ausencia específica.
     *
     * @param ausenciaId El ID de la ausencia cuya cobertura se desea eliminar.
     * @return true si la cobertura fue eliminada, false si no se encontró cobertura.
     */
    public boolean eliminarCoberturaPorAusencia(Long ausenciaId) {
        logger.debug("Intentando eliminar cobertura para ausencia con ID {}", ausenciaId);
        return coberturaRepository.findByAusenciaId(ausenciaId)
                .map(cobertura -> {
                    coberturaRepository.deleteById(cobertura.getId());
                    logger.info("Cobertura con ID {} eliminada", cobertura.getId());
                    return true;
                })
                .orElseGet(() -> {
                    logger.warn("No se encontró cobertura para eliminar para la ausencia con ID {}", ausenciaId);
                    return false;
                });
    }

    /**
     * Obtiene todas las coberturas asignadas a un profesor específico.
     *
     * @param emailProfesor El correo electrónico del profesor.
     * @return Una lista de coberturas en las que el profesor está asignado.
     */
    public List<Cobertura> obtenerCoberturasPorProfesor(String emailProfesor) {
        logger.debug("Obteniendo coberturas para el profesor {}", emailProfesor);
        List<Cobertura> coberturas = coberturaRepository.findAllByProfesorCubreEmail(emailProfesor);
        logger.info("Total de coberturas encontradas para el profesor {}: {}", emailProfesor, coberturas.size());
        return coberturas;
    }

    public List<CoberturaDTO> obtenerCoberturasDTOPorProfesor(String email) {
        return coberturaRepository.findAllByProfesorCubreEmail(email).stream()
                .map(cobertura -> {
                    CoberturaDTO dto = new CoberturaDTO();
                    dto.setAusenciaId(cobertura.getAusencia().getId());
                    dto.setProfesorCubreEmail(cobertura.getProfesorCubreEmail());
                    dto.setGrupo(cobertura.getAusencia().getGrupo());
                    dto.setAula(cobertura.getAusencia().getAula());
                    dto.setHora(cobertura.getAusencia().getHora());
                    dto.setFecha(cobertura.getAusencia().getFecha());
                    dto.setProfesorAusenteEmail(cobertura.getAusencia().getProfesorAusenteEmail());
                    dto.setTarea(cobertura.getAusencia().getTarea());
                    return dto;
                })
                .collect(Collectors.toList());
    }
    @Transactional
    public Cobertura asignarCoberturaManual(Long ausenciaId, String emailProfesor) {
        logger.debug("Reasignando cobertura para ausencia {}", ausenciaId);

        coberturaRepository.deleteByAusenciaId(ausenciaId);
        coberturaRepository.flush(); // 🔨 ¡esto sí fuerza la ejecución!

        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .orElseThrow(() -> new RuntimeException("Ausencia no encontrada"));

        Cobertura nueva = new Cobertura();
        nueva.setAusencia(ausencia);
        nueva.setProfesorCubreEmail(emailProfesor);
        nueva.setGrupo(ausencia.getGrupo());
        nueva.setAula(ausencia.getAula());

        Cobertura guardada = coberturaRepository.save(nueva);
        logger.info("Cobertura asignada manualmente para ausencia {}", ausenciaId);
        return guardada;
    }


}