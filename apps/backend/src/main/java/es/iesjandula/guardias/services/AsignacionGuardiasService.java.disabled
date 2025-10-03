package es.iesjandula.guardias.services;

import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * Servicio que maneja la asignación automática de coberturas
 * Implementa el sistema de redistribución completa por día con algoritmo de equidad
 */
@Service
public class AsignacionGuardiasService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsignacionGuardiasService.class);
    
    @Autowired
    private AusenciaRepository ausenciaRepository;
    
    @Autowired
    private CoberturaRepository coberturaRepository;
    
    @Autowired
    private ContadorGuardiasRepository contadorRepository;
    
    /**
     * Se ejecuta cada vez que se crea una nueva ausencia.
     * Redistribuye TODAS las coberturas del día para optimizar la distribución equitativa.
     */
    @Transactional
    public void redistribuirCoberturasPorNuevaAusencia(Ausencia nuevaAusencia) {
        logger.info("🔄 Redistribuyendo coberturas completas por nueva ausencia ID: {}", nuevaAusencia.getId());
        
        LocalDate fechaAusencia = nuevaAusencia.getFecha();
        
        // Eliminar solo coberturas automáticas (ASIGNADA) del día para reasignar
        eliminarCoberturasReasignables(fechaAusencia);
        
        // Redistribuir por cada hora del día que tenga ausencias
        Map<Integer, List<Ausencia>> ausenciasPorHora = agruparAusenciasPorHora(fechaAusencia);
        
        for (Map.Entry<Integer, List<Ausencia>> entry : ausenciasPorHora.entrySet()) {
            Integer hora = entry.getKey();
            List<Ausencia> ausenciasHora = entry.getValue();
            
            logger.info("📋 Procesando hora {} con {} ausencias", hora, ausenciasHora.size());
            asignarCoberturasPorHora(fechaAusencia, hora, ausenciasHora);
        }
        
        logger.info("✅ Redistribución completada para el día {}", fechaAusencia);
    }
    
    /**
     * Elimina las coberturas que pueden ser reasignadas (estado ASIGNADA)
     * Mantiene las coberturas VALIDADAS que ya han sido confirmadas
     */
    private void eliminarCoberturasReasignables(LocalDate fecha) {
        List<Cobertura> coberturasReasignables = coberturaRepository
            .findByAusenciaFechaAndEstado(fecha, EstadoCobertura.ASIGNADA);
        
        logger.debug("🗑️ Eliminando {} coberturas reasignables del {}", 
                    coberturasReasignables.size(), fecha);
        
        for (Cobertura cobertura : coberturasReasignables) {
            coberturaRepository.delete(cobertura);
        }
        
        // Asegurar que los cambios se reflejen inmediatamente
        coberturaRepository.flush();
    }
    
    /**
     * Agrupa las ausencias sin cobertura por hora
     */
    private Map<Integer, List<Ausencia>> agruparAusenciasPorHora(LocalDate fecha) {
        List<Ausencia> ausenciasSinCobertura = ausenciaRepository.findSinCoberturaByFecha(fecha);
        
        return ausenciasSinCobertura.stream()
            .collect(Collectors.groupingBy(Ausencia::getHora));
    }
    
    /**
     * Asigna coberturas para una hora específica siguiendo el algoritmo de prioridades
     */
    private void asignarCoberturasPorHora(LocalDate fecha, Integer hora, List<Ausencia> ausencias) {
        DiaSemana diaSemana = DiaSemana.fromLocalDate(fecha);
        
        // Obtener profesores disponibles de guardia en este tramo horario
        List<String> profesoresDisponibles = obtenerProfesoresDeGuardia(diaSemana, hora);
        List<String> profesoresLibres = new ArrayList<>(profesoresDisponibles);
        
        logger.debug("👥 Profesores de guardia disponibles en {}:{}: {}", 
                    diaSemana, hora, profesoresDisponibles.size());
        
        // 1️⃣ PASO 1: Asignar AULA DE CONVIVENCIA (OBLIGATORIO)
        String profesorConvivencia = asignarAulaConvivencia(diaSemana, hora, profesoresLibres);
        if (profesorConvivencia != null) {
            profesoresLibres.remove(profesorConvivencia);
            logger.debug("🏫 Convivencia asignada a: {}", profesorConvivencia);
        }
        
        // Separar ausencias por tipo de grupo
        List<Ausencia> ausenciasProblematicas = ausencias.stream()
            .filter(this::esGrupoProblematico)
            .collect(Collectors.toList());
            
        List<Ausencia> ausenciasNormales = ausencias.stream()
            .filter(a -> !esGrupoProblematico(a))
            .collect(Collectors.toList());
        
        // 2️⃣ PASO 2: Asignar GRUPOS PROBLEMÁTICOS
        asignarGruposProblematicos(diaSemana, hora, ausenciasProblematicas, profesoresLibres);
        
        // 3️⃣ PASO 3: Asignar GRUPOS NORMALES  
        asignarGruposNormales(diaSemana, hora, ausenciasNormales, profesoresLibres);
        
        // 🚨 PASO 4: Alertar si faltan profesores
        int totalAusencias = ausencias.size();
        int profesoresAsignados = totalAusencias - profesoresLibres.size() + (profesorConvivencia != null ? 1 : 0);
        
        if (totalAusencias > profesoresDisponibles.size()) {
            logger.warn("⚠️ ALERTA: {} ausencias vs {} profesores disponibles en {}:{}", 
                       totalAusencias, profesoresDisponibles.size(), diaSemana, hora);
        }
    }
    
    /**
     * Asigna el aula de convivencia al profesor con menos guardias de convivencia
     */
    private String asignarAulaConvivencia(DiaSemana dia, Integer hora, List<String> profesoresDisponibles) {
        if (profesoresDisponibles.isEmpty()) {
            logger.warn("⚠️ No hay profesores disponibles para aula de convivencia en {}:{}", dia, hora);
            return null;
        }
        
        // Buscar profesor con menos guardias de convivencia
        List<String> profesoresOrdenados = contadorRepository
            .findProfesoresConMenosGuardiasConvivenciaEmails(dia, hora);
        
        // Filtrar solo los que están disponibles de guardia
        String profesorSeleccionado = profesoresOrdenados.stream()
            .filter(profesoresDisponibles::contains)
            .findFirst()
            .orElse(profesoresDisponibles.get(0)); // Fallback al primero disponible
        
        // Crear cobertura especial de convivencia (sin ausencia específica)
        crearCoberturaConvivencia(profesorSeleccionado, dia, hora);
        
        return profesorSeleccionado;
    }
    
    /**
     * Asigna grupos problemáticos a profesores con menos guardias problemáticas
     */
    private void asignarGruposProblematicos(DiaSemana dia, Integer hora, List<Ausencia> ausenciasProblematicas, List<String> profesoresLibres) {
        if (ausenciasProblematicas.isEmpty()) return;
        
        logger.debug("⚡ Asignando {} grupos problemáticos", ausenciasProblematicas.size());
        
        // Obtener profesores ordenados por menos guardias problemáticas
        List<String> profesoresOrdenados = contadorRepository
            .findProfesoresConMenosGuardiasProblematicasEmails(dia, hora);
        
        // Filtrar solo los que están libres
        List<String> profesoresDisponibles = profesoresOrdenados.stream()
            .filter(profesoresLibres::contains)
            .collect(Collectors.toList());
        
        // Asignar uno a uno
        int index = 0;
        for (Ausencia ausencia : ausenciasProblematicas) {
            if (index < profesoresDisponibles.size()) {
                String profesor = profesoresDisponibles.get(index);
                crearCobertura(ausencia, profesor, TipoGuardia.PROBLEMATICA);
                profesoresLibres.remove(profesor);
                index++;
                
                logger.debug("⚡ Grupo problemático {} asignado a {}", ausencia.getGrupo(), profesor);
            } else {
                logger.warn("⚠️ No hay suficientes profesores para grupo problemático: {}", ausencia.getGrupo());
            }
        }
    }
    
    /**
     * Asigna grupos normales a profesores con menos guardias normales
     */
    private void asignarGruposNormales(DiaSemana dia, Integer hora, List<Ausencia> ausenciasNormales, List<String> profesoresLibres) {
        if (ausenciasNormales.isEmpty()) return;
        
        logger.debug("📚 Asignando {} grupos normales", ausenciasNormales.size());
        
        // Obtener profesores ordenados por menos guardias normales
        List<String> profesoresOrdenados = contadorRepository
            .findProfesoresConMenosGuardiasNormalesEmails(dia, hora);
        
        // Filtrar solo los que están libres
        List<String> profesoresDisponibles = profesoresOrdenados.stream()
            .filter(profesoresLibres::contains)
            .collect(Collectors.toList());
        
        // Asignar uno a uno
        int index = 0;
        for (Ausencia ausencia : ausenciasNormales) {
            if (index < profesoresDisponibles.size()) {
                String profesor = profesoresDisponibles.get(index);
                crearCobertura(ausencia, profesor, TipoGuardia.NORMAL);
                profesoresLibres.remove(profesor);
                index++;
                
                logger.debug("📚 Grupo normal {} asignado a {}", ausencia.getGrupo(), profesor);
            } else {
                logger.warn("⚠️ No hay suficientes profesores para grupo normal: {}", ausencia.getGrupo());
            }
        }
    }
    
    /**
     * Determina si un grupo es problemático basado en reglas de negocio
     */
    private boolean esGrupoProblematico(Ausencia ausencia) {
        String grupo = ausencia.getGrupo().toLowerCase();
        String tarea = ausencia.getTarea();
        
        // Reglas para identificar grupos problemáticos
        // TODO: Personalizar según las reglas específicas del instituto
        
        // Ejemplos de reglas:
        // - Grupos específicos marcados como problemáticos
        if (grupo.contains("problematico") || grupo.contains("conflictivo")) {
            return true;
        }
        
        // - Exámenes o actividades especiales
        if (tarea != null && (tarea.toLowerCase().contains("examen") || 
                              tarea.toLowerCase().contains("evaluacion"))) {
            return true;
        }
        
        // - Horarios específicos (primera/última hora con ciertos cursos)
        // if ((ausencia.getHora() == 1 || ausencia.getHora() == 8) && grupo.contains("1º")) {
        //     return true;
        // }
        
        return false;
    }

    /**
     * Obtiene la lista de profesores que están de guardia en un día y hora específicos
     * TODO: Integrar con el microservicio de horarios para obtener esta información
     */
    private List<String> obtenerProfesoresDeGuardia(DiaSemana dia, Integer hora) {
        // Por ahora devolvemos los profesores que tienen contadores en ese tramo horario
        // En el futuro, esto debería consultar el microservicio de horarios
        List<ContadorGuardias> contadores = contadorRepository.findByDiaSemanaAndHoraOrderByProfesorEmailAsc(dia, hora);
        
        return contadores.stream()
            .map(ContadorGuardias::getProfesorEmail)
            .distinct()
            .collect(Collectors.toList());
    }
    
    /**
     * Crea una cobertura de convivencia (sin ausencia específica)
     */
    private void crearCoberturaConvivencia(String profesorEmail, DiaSemana dia, Integer hora) {
        // TODO: Implementar lógica para crear cobertura de convivencia
        // Esto requeriría una estructura especial ya que no hay ausencia específica
        logger.debug("🏫 Cobertura de convivencia asignada a {} en {}:{}", profesorEmail, dia, hora);
    }
    
    /**
     * Crea una cobertura para una ausencia específica
     */
    private void crearCobertura(Ausencia ausencia, String profesorEmail, TipoGuardia tipoGuardia) {
        Cobertura cobertura = new Cobertura();
        cobertura.setAusencia(ausencia);
        cobertura.setProfesorCubreEmail(profesorEmail);
        cobertura.setGrupo(ausencia.getGrupo());
        cobertura.setAula(ausencia.getAula());
        cobertura.setTipoGuardia(tipoGuardia);
        cobertura.setEstado(EstadoCobertura.ASIGNADA); // Automática, pendiente de validación
        
        coberturaRepository.save(cobertura);
        
        logger.debug("✅ Cobertura creada: {} para ausencia {} ({})", 
                    profesorEmail, ausencia.getId(), tipoGuardia);
    }

    /**
     * Obtiene profesores con menos guardias según el tipo específico
     */
    private List<String> obtenerProfesoresConMenosGuardias(TipoGuardia tipo, DiaSemana dia, Integer hora) {
        return switch (tipo) {
            case NORMAL -> contadorRepository.findProfesoresConMenosGuardiasNormalesEmails(dia, hora);
            case PROBLEMATICA -> contadorRepository.findProfesoresConMenosGuardiasProblematicasEmails(dia, hora);
            case CONVIVENCIA -> contadorRepository.findProfesoresConMenosGuardiasConvivenciaEmails(dia, hora);
        };
    }
    
    /**
     * Valida una cobertura manualmente (uso del administrador)
     */
    @Transactional
    public void validarCobertura(Long coberturaId, String adminEmail) {
        Optional<Cobertura> coberturaOpt = coberturaRepository.findById(coberturaId);
        
        if (coberturaOpt.isPresent()) {
            Cobertura cobertura = coberturaOpt.get();
            cobertura.setEstado(EstadoCobertura.VALIDADA);
            cobertura.setValidadaPorAdmin(true);
            cobertura.setAdminValidadorEmail(adminEmail);
            cobertura.setFechaValidacion(LocalDateTime.now());
            
            coberturaRepository.save(cobertura);
            
            logger.info("Cobertura {} validada por admin {}", coberturaId, adminEmail);
        } else {
            throw new RuntimeException("Cobertura no encontrada: " + coberturaId);
        }
    }
    
    /**
     * Cancela una cobertura (no se realizó finalmente)
     */
    @Transactional
    public void cancelarCobertura(Long coberturaId, String adminEmail) {
        Optional<Cobertura> coberturaOpt = coberturaRepository.findById(coberturaId);
        
        if (coberturaOpt.isPresent()) {
            Cobertura cobertura = coberturaOpt.get();
            cobertura.setEstado(EstadoCobertura.CANCELADA);
            cobertura.setAdminValidadorEmail(adminEmail);
            cobertura.setFechaValidacion(LocalDateTime.now());
            
            coberturaRepository.save(cobertura);
            
            logger.info("Cobertura {} cancelada por admin {}", coberturaId, adminEmail);
        } else {
            throw new RuntimeException("Cobertura no encontrada: " + coberturaId);
        }
    }
    
    /**
     * Obtiene las coberturas pendientes de validación para un día específico
     */
    public List<Cobertura> obtenerCoberturasPendientes(LocalDate fecha) {
        return coberturaRepository.findByAusenciaFechaAndEstado(fecha, EstadoCobertura.ASIGNADA);
    }
}
