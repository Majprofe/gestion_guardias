package es.iesjandula.guardias.services;

import es.iesjandula.guardias.dto.CoberturaDTO;
import es.iesjandula.guardias.exception.BusinessException;
import es.iesjandula.guardias.exception.ResourceNotFoundException;
import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.repositories.AusenciaRepository;
import es.iesjandula.guardias.repositories.CoberturaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar las coberturas de guardias
 */
@Service
public class CoberturaService {
    
    private static final Logger logger = LoggerFactory.getLogger(CoberturaService.class);
    
    @Autowired
    private CoberturaRepository coberturaRepository;
    
    @Autowired
    private AusenciaRepository ausenciaRepository;
    
    @Autowired
    private AsignacionGuardiasService asignacionService;
    
    /**
     * Obtiene todas las coberturas de un día específico
     */
    public List<CoberturaDTO> obtenerCoberturasPorFecha(LocalDate fecha) {
        List<Cobertura> coberturas = coberturaRepository.findByAusenciaFecha(fecha);
        return coberturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene coberturas por estado y fecha
     */
    public List<CoberturaDTO> obtenerCoberturasPorEstadoYFecha(EstadoCobertura estado, LocalDate fecha) {
        List<Cobertura> coberturas = coberturaRepository.findByAusenciaFechaAndEstado(fecha, estado);
        return coberturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtiene las coberturas asignadas a un profesor específico
     */
    public List<CoberturaDTO> obtenerCoberturasProfesor(String profesorEmail) {
        List<Cobertura> todasLasCoberturas = coberturaRepository.findAll();
        List<Cobertura> coberturas = todasLasCoberturas.stream()
                .filter(c -> profesorEmail.equals(c.getProfesorCubreEmail()))
                .collect(Collectors.toList());
        
        return coberturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Valida una cobertura (solo para admins)
     */
    @Transactional
    public CoberturaDTO validarCobertura(Long coberturaId, String adminEmail) {
        Cobertura cobertura = coberturaRepository.findById(coberturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobertura no encontrada"));
        
        if (cobertura.getEstado() != EstadoCobertura.ASIGNADA) {
            throw new BusinessException("Solo se pueden validar coberturas asignadas");
        }
        
        cobertura.setEstado(EstadoCobertura.VALIDADA);
        cobertura.setValidadaPorAdmin(true);
        cobertura.setFechaValidacion(LocalDateTime.now());
        cobertura.setAdminValidadorEmail(adminEmail);
        
        Cobertura coberturaGuardada = coberturaRepository.save(cobertura);
        
        logger.info("Cobertura {} validada por admin {}", coberturaId, adminEmail);
        
        return convertirADTO(coberturaGuardada);
    }
    
    /**
     * Cancela una cobertura y reasigna automáticamente
     */
    @Transactional
    public void cancelarCobertura(Long coberturaId, String motivoCancelacion) {
        Cobertura cobertura = coberturaRepository.findById(coberturaId)
                .orElseThrow(() -> new ResourceNotFoundException("Cobertura no encontrada"));
        
        if (cobertura.getEstado() == EstadoCobertura.VALIDADA) {
            throw new BusinessException("No se puede cancelar una cobertura ya validada");
        }
        
        // Marcar como cancelada
        cobertura.setEstado(EstadoCobertura.CANCELADA);
        coberturaRepository.save(cobertura);
        
        // Intentar reasignar automáticamente
        try {
            asignacionService.redistribuirCoberturasPorNuevaAusencia(cobertura.getAusencia());
            logger.info("Cobertura {} cancelada y reasignada automáticamente", coberturaId);
        } catch (Exception e) {
            logger.warn("No se pudo reasignar automáticamente la cobertura {}: {}", 
                       coberturaId, e.getMessage());
        }
    }
    
    /**
     * Convierte una entidad Cobertura a DTO
     */
    private CoberturaDTO convertirADTO(Cobertura cobertura) {
        CoberturaDTO dto = new CoberturaDTO();
        dto.setAusenciaId(cobertura.getAusencia().getId());
        dto.setProfesorCubreEmail(cobertura.getProfesorCubreEmail());
        dto.setGrupo(cobertura.getGrupo());
        dto.setAula(cobertura.getAula());
        
        // Datos de la ausencia
        Ausencia ausencia = cobertura.getAusencia();
        dto.setFecha(ausencia.getFecha());
        dto.setHora(ausencia.getHora());
        dto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
        dto.setTarea(ausencia.getTarea());
        
        return dto;
    }
    
    /**
     * Obtiene estadísticas de coberturas para un rango de fechas
     */
    public long contarCoberturasPorEstado(EstadoCobertura estado, LocalDate fechaInicio, LocalDate fechaFin) {
        return coberturaRepository.findAll().stream()
                .filter(c -> c.getEstado() == estado &&
                           !c.getAusencia().getFecha().isBefore(fechaInicio) && 
                           !c.getAusencia().getFecha().isAfter(fechaFin))
                .count();
    }
    
    /**
     * Obtiene las coberturas pendientes de validación (para admins)
     */
    public List<CoberturaDTO> obtenerCoberturasPendientesValidacion() {
        LocalDate hoy = LocalDate.now();
        List<Cobertura> coberturas = coberturaRepository
                .findByAusenciaFechaAndEstado(hoy, EstadoCobertura.ASIGNADA);
        
        return coberturas.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}
