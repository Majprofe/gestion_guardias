package es.iesjandula.guardias.services;

import es.iesjandula.guardias.models.Cobertura;
import es.iesjandula.guardias.models.ContadorGuardias;
import es.iesjandula.guardias.models.DiaSemana;
import es.iesjandula.guardias.models.EstadoCobertura;
import es.iesjandula.guardias.repositories.CoberturaRepository;
import es.iesjandula.guardias.repositories.ContadorGuardiasRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Servicio para validar y cancelar coberturas.
 * Solo el administrador puede validar coberturas, lo que incrementa los contadores.
 */
@Service
public class CoberturaValidacionService {

    private static final Logger logger = LoggerFactory.getLogger(CoberturaValidacionService.class);

    private final CoberturaRepository coberturaRepository;
    private final ContadorGuardiasRepository contadorRepository;

    public CoberturaValidacionService(CoberturaRepository coberturaRepository,
                                     ContadorGuardiasRepository contadorRepository) {
        this.coberturaRepository = coberturaRepository;
        this.contadorRepository = contadorRepository;
    }

    /**
     * Valida una cobertura e incrementa el contador correspondiente
     * Solo puede hacerlo un administrador
     */
    @Transactional
    public Cobertura validarCobertura(Long coberturaId, String adminEmail) {
        logger.info("Validando cobertura ID: {} por admin: {}", coberturaId, adminEmail);

        Cobertura cobertura = coberturaRepository.findById(coberturaId)
                .orElseThrow(() -> new RuntimeException("Cobertura no encontrada: " + coberturaId));

        // Verificar que no esté ya validada
        if (cobertura.getEstado() == EstadoCobertura.VALIDADA) {
            logger.warn("Cobertura {} ya está validada", coberturaId);
            throw new RuntimeException("La cobertura ya está validada");
        }

        // Verificar que no esté cancelada
        if (cobertura.getEstado() == EstadoCobertura.CANCELADA) {
            logger.warn("No se puede validar cobertura {} porque está cancelada", coberturaId);
            throw new RuntimeException("No se puede validar una cobertura cancelada");
        }

        // Cambiar estado a VALIDADA
        cobertura.setEstado(EstadoCobertura.VALIDADA);
        cobertura.setValidadaPorAdmin(true);
        cobertura.setFechaValidacion(LocalDateTime.now());
        cobertura.setAdminValidadorEmail(adminEmail);

        // Incrementar contador
        incrementarContador(cobertura);

        coberturaRepository.save(cobertura);
        logger.info("Cobertura {} validada exitosamente", coberturaId);

        return cobertura;
    }

    /**
     * Cancela una cobertura (no se incrementa el contador)
     */
    @Transactional
    public Cobertura cancelarCobertura(Long coberturaId, String adminEmail, String motivo) {
        logger.info("Cancelando cobertura ID: {} por admin: {}. Motivo: {}", 
                   coberturaId, adminEmail, motivo);

        Cobertura cobertura = coberturaRepository.findById(coberturaId)
                .orElseThrow(() -> new RuntimeException("Cobertura no encontrada: " + coberturaId));

        // No se puede cancelar si ya está validada
        if (cobertura.getEstado() == EstadoCobertura.VALIDADA) {
            logger.warn("No se puede cancelar cobertura {} porque ya está validada", coberturaId);
            throw new RuntimeException("No se puede cancelar una cobertura ya validada");
        }

        cobertura.setEstado(EstadoCobertura.CANCELADA);
        cobertura.setAdminValidadorEmail(adminEmail);
        cobertura.setFechaValidacion(LocalDateTime.now());

        coberturaRepository.save(cobertura);
        logger.info("Cobertura {} cancelada exitosamente", coberturaId);

        return cobertura;
    }

    /**
     * Valida todas las coberturas de una fecha específica
     * Útil para validaciones masivas al final del día
     */
    @Transactional
    public int validarCoberturasDelDia(java.time.LocalDate fecha, String adminEmail) {
        logger.info("Validando todas las coberturas del día {} por admin: {}", fecha, adminEmail);

        // Obtener todas las coberturas asignadas de ese día
        var coberturas = coberturaRepository.findByFechaAndEstado(fecha, EstadoCobertura.ASIGNADA);

        int validadas = 0;
        for (Cobertura cobertura : coberturas) {
            try {
                validarCobertura(cobertura.getId(), adminEmail);
                validadas++;
            } catch (Exception e) {
                logger.error("Error validando cobertura {}: {}", cobertura.getId(), e.getMessage());
            }
        }

        logger.info("Validadas {} coberturas del día {}", validadas, fecha);
        return validadas;
    }

    /**
     * Incrementa el contador del profesor según el tipo de guardia
     */
    private void incrementarContador(Cobertura cobertura) {
        String profesorEmail = cobertura.getProfesorCubreEmail();
        DiaSemana diaSemana = convertirDiaSemana(
                cobertura.getHoraAusencia().getAusencia().getFecha().getDayOfWeek());
        Integer hora = cobertura.getHoraAusencia().getHora();

        // Obtener o crear contador
        ContadorGuardias contador = contadorRepository
                .findByProfesorEmailAndDiaSemanaAndHora(profesorEmail, diaSemana, hora)
                .orElseGet(() -> {
                    ContadorGuardias nuevo = new ContadorGuardias();
                    nuevo.setProfesorEmail(profesorEmail);
                    nuevo.setDiaSemana(diaSemana);
                    nuevo.setHora(hora);
                    nuevo.setGuardiasNormales(0);
                    nuevo.setGuardiasProblematicas(0);
                    nuevo.setGuardiasConvivencia(0);
                    return nuevo;
                });

        // Incrementar según tipo de guardia
        contador.incrementarContador(cobertura.getTipoGuardia());
        contadorRepository.save(contador);

        logger.info("Contador incrementado para profesor {} - {} {} ({}): normales={}, problemáticas={}, convivencia={}",
                   profesorEmail, diaSemana, hora, cobertura.getTipoGuardia(),
                   contador.getGuardiasNormales(), contador.getGuardiasProblematicas(), 
                   contador.getGuardiasConvivencia());
    }

    /**
     * Convierte DayOfWeek a DiaSemana
     */
    private DiaSemana convertirDiaSemana(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> DiaSemana.LUNES;
            case TUESDAY -> DiaSemana.MARTES;
            case WEDNESDAY -> DiaSemana.MIERCOLES;
            case THURSDAY -> DiaSemana.JUEVES;
            case FRIDAY -> DiaSemana.VIERNES;
            default -> throw new IllegalArgumentException("Día no válido: " + dayOfWeek);
        };
    }
}
