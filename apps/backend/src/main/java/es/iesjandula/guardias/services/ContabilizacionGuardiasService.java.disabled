package es.iesjandula.guardias.services;

import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.repositories.*;
import es.iesjandula.guardias.dto.EstadisticasContabilizacionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que maneja la contabilización automática de guardias
 * Se ejecuta todos los días a las 23:30h para contabilizar las guardias validadas del día
 */
@Component
public class ContabilizacionGuardiasService {
    
    private static final Logger logger = LoggerFactory.getLogger(ContabilizacionGuardiasService.class);
    
    @Autowired
    private CoberturaRepository coberturaRepository;
    
    @Autowired
    private ContadorGuardiasRepository contadorRepository;
    
    /**
     * Cron job que se ejecuta todos los días a las 23:30h
     * Solo contabiliza coberturas con estado VALIDADA
     */
    @Scheduled(cron = "0 30 23 * * *")
    @Transactional
    public void contabilizarGuardiasDelDia() {
        LocalDate hoy = LocalDate.now();
        logger.info("🕚 Iniciando contabilización de guardias del día {}", hoy);
        
        try {
            // Obtener coberturas validadas del día (solo estas se cuentan)
            List<Cobertura> coberturasValidadas = coberturaRepository
                .findByAusenciaFechaAndEstado(hoy, EstadoCobertura.VALIDADA);
            
            logger.info("📊 Encontradas {} coberturas validadas para contabilizar", coberturasValidadas.size());
            
            // Contabilizar cada cobertura validada
            int contabilizadas = 0;
            for (Cobertura cobertura : coberturasValidadas) {
                if (contabilizarCobertura(cobertura)) {
                    contabilizadas++;
                }
            }
            
            logger.info("✅ Contabilización completada: {}/{} guardias procesadas", 
                       contabilizadas, coberturasValidadas.size());
                       
        } catch (Exception e) {
            logger.error("❌ Error durante la contabilización de guardias: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Contabiliza una cobertura específica
     */
    private boolean contabilizarCobertura(Cobertura cobertura) {
        try {
            DiaSemana diaSemana = DiaSemana.fromLocalDate(cobertura.getAusencia().getFecha());
            
            // Buscar o crear contador para el profesor
            Optional<ContadorGuardias> contadorOpt = contadorRepository
                .findByProfesorEmailAndDiaSemanaAndHora(
                    cobertura.getProfesorCubreEmail(),
                    diaSemana,
                    cobertura.getAusencia().getHora()
                );
            
            ContadorGuardias contador;
            if (contadorOpt.isPresent()) {
                contador = contadorOpt.get();
            } else {
                contador = new ContadorGuardias();
                contador.setProfesorEmail(cobertura.getProfesorCubreEmail());
                contador.setDiaSemana(diaSemana);
                contador.setHora(cobertura.getAusencia().getHora());
            }
            
            // Incrementar contador según el tipo de guardia
            contador.incrementarContador(cobertura.getTipoGuardia());
            
            contadorRepository.save(contador);
            
            logger.debug("Contabilizada guardia {} para {}", 
                        cobertura.getTipoGuardia(), cobertura.getProfesorCubreEmail());
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error al contabilizar cobertura ID {}: {}", 
                        cobertura.getId(), e.getMessage());
            return false;
        }
    }
    
    /**
     * Obtiene estadísticas de contabilización para un día específico
     */
    public EstadisticasContabilizacionDTO obtenerEstadisticasDia(LocalDate fecha) {
        List<Cobertura> todasLasCoberturas = coberturaRepository.findByAusenciaFecha(fecha);
        long validadas = todasLasCoberturas.stream()
            .filter(c -> c.getEstado() == EstadoCobertura.VALIDADA)
            .count();
        long asignadas = todasLasCoberturas.stream()
            .filter(c -> c.getEstado() == EstadoCobertura.ASIGNADA)
            .count();
        long canceladas = todasLasCoberturas.stream()
            .filter(c -> c.getEstado() == EstadoCobertura.CANCELADA)
            .count();
        
        return new EstadisticasContabilizacionDTO(fecha, validadas, asignadas, canceladas);
    }
}
