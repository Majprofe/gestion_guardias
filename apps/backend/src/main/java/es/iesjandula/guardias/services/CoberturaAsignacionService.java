package es.iesjandula.guardias.services;

import es.iesjandula.guardias.dto.GrupoDto;
import es.iesjandula.guardias.dto.ProfesorGuardiaSimpleDto;
import es.iesjandula.guardias.integration.HorarioIntegrationService;
import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.repositories.CoberturaRepository;
import es.iesjandula.guardias.repositories.ContadorGuardiasRepository;
import es.iesjandula.guardias.repositories.HoraAusenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para asignar coberturas de forma automática y equitativa.
 * 
 * Lógica de asignación:
 * 1. Asigna 1 profesor al aula de convivencia (el que tiene menos guardiasConvivencia)
 * 2. Asigna profesores a grupos problemáticos (los que tienen menos guardiasProblematicas)
 * 3. Asigna profesores a guardias normales (los que tienen menos guardiasNormales)
 */
@Service
public class CoberturaAsignacionService {

    private static final Logger logger = LoggerFactory.getLogger(CoberturaAsignacionService.class);

    private final HorarioIntegrationService horarioIntegration;
    private final ContadorGuardiasRepository contadorRepository;
    private final CoberturaRepository coberturaRepository;
    private final HoraAusenciaRepository horaAusenciaRepository;
    private final RestTemplate restTemplate;

    @Value("${app.horarios.api.url:http://localhost:8082}")
    private String horariosApiUrl;

    public CoberturaAsignacionService(
            HorarioIntegrationService horarioIntegration,
            ContadorGuardiasRepository contadorRepository,
            CoberturaRepository coberturaRepository,
            HoraAusenciaRepository horaAusenciaRepository,
            RestTemplate restTemplate) {
        this.horarioIntegration = horarioIntegration;
        this.contadorRepository = contadorRepository;
        this.coberturaRepository = coberturaRepository;
        this.horaAusenciaRepository = horaAusenciaRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Asigna coberturas para todas las horas de una ausencia específica
     */
    @Transactional
    public void asignarCoberturas(Ausencia ausencia, LocalDate fecha) {
        logger.info("Iniciando asignación de coberturas para ausencia ID: {} en fecha: {}", 
                   ausencia.getId(), fecha);

        DiaSemana diaSemana = convertirDiaSemana(fecha.getDayOfWeek());

        for (HoraAusencia horaAusencia : ausencia.getHoras()) {
            try {
                asignarCoberturaParaHora(horaAusencia, diaSemana, horaAusencia.getHora(), fecha);
            } catch (Exception e) {
                logger.error("Error asignando cobertura para hora {}: {}", 
                           horaAusencia.getHora(), e.getMessage(), e);
            }
        }
    }

    /**
     * Reasigna todas las coberturas de un día específico y horas específicas
     * Se usa cuando hay nuevas ausencias que requieren redistribuir profesores
     */
    @Transactional
    public void reasignarCoberturasDelDia(LocalDate fecha, List<Integer> horas) {
        logger.info("Reasignando coberturas para fecha: {} y horas: {}", fecha, horas);

        DiaSemana diaSemana = convertirDiaSemana(fecha.getDayOfWeek());

        for (Integer hora : horas) {
            // Eliminar coberturas existentes que puedan ser reasignadas
            List<Cobertura> coberturasExistentes = coberturaRepository
                    .findByHoraAusencia_Ausencia_FechaAndHoraAusencia_NumeroHora(fecha, hora);

            List<Cobertura> coberturasReasignables = coberturasExistentes.stream()
                    .filter(c -> c.getEstado().puedeReasignarse())
                    .collect(Collectors.toList());

            logger.info("Eliminando {} coberturas reasignables para hora {}", 
                       coberturasReasignables.size(), hora);
            coberturaRepository.deleteAll(coberturasReasignables);

            // Obtener todas las horas de ausencia para esta fecha y hora
            List<HoraAusencia> horasAusencia = horaAusenciaRepository
                    .findByAusencia_FechaAndNumeroHora(fecha, hora);

            // Reasignar coberturas
            asignarCoberturasParaHoras(horasAusencia, diaSemana, hora, fecha);
        }
    }

    /**
     * Asigna cobertura para una hora de ausencia específica
     */
    private void asignarCoberturaParaHora(HoraAusencia horaAusencia, DiaSemana diaSemana, 
                                          Integer hora, LocalDate fecha) {
        logger.debug("Asignando cobertura para HoraAusencia ID: {}, día: {}, hora: {}", 
                    horaAusencia.getId(), diaSemana, hora);

        // Obtener profesores de guardia desde el backend de horarios
        List<ProfesorGuardiaSimpleDto> profesoresGuardia = obtenerProfesoresGuardia(
                diaSemana.ordinal() + 1, horaAusencia.getHora());

        if (profesoresGuardia.isEmpty()) {
            logger.warn("No hay profesores de guardia disponibles para día {} hora {}", diaSemana, hora);
            return;
        }

        // Verificar si el grupo es problemático
        boolean esGrupoProblematico = verificarSiGrupoEsProblematico(horaAusencia.getGrupo());
        TipoGuardia tipoGuardia = esGrupoProblematico ? TipoGuardia.PROBLEMATICA : TipoGuardia.NORMAL;

        // Asignar profesor según el tipo de guardia
        String profesorAsignado = asignarProfesor(profesoresGuardia, diaSemana, hora, tipoGuardia);

        if (profesorAsignado != null) {
            // Crear cobertura
            Cobertura cobertura = new Cobertura();
            cobertura.setHoraAusencia(horaAusencia);
            cobertura.setProfesorCubreEmail(profesorAsignado);
            cobertura.setGrupo(horaAusencia.getGrupo());
            cobertura.setAula(horaAusencia.getAula());
            cobertura.setTipoGuardia(tipoGuardia);
            cobertura.setEstado(EstadoCobertura.ASIGNADA);
            cobertura.setValidadaPorAdmin(false);

            coberturaRepository.save(cobertura);
            logger.info("Cobertura creada: Profesor {} cubre hora {} ({})", 
                       profesorAsignado, hora, tipoGuardia);
        }
    }

    /**
     * Asigna coberturas para múltiples horas de ausencia, incluyendo aula de convivencia
     */
    private void asignarCoberturasParaHoras(List<HoraAusencia> horasAusencia, DiaSemana diaSemana, 
                                            Integer hora, LocalDate fecha) {
        if (horasAusencia.isEmpty()) {
            return;
        }

        // Obtener profesores de guardia
        List<ProfesorGuardiaSimpleDto> profesoresGuardia = obtenerProfesoresGuardia(
                diaSemana.ordinal() + 1, hora);

        if (profesoresGuardia.isEmpty()) {
            logger.warn("No hay profesores de guardia para día {} hora {}", diaSemana, hora);
            return;
        }

        // Set para trackear profesores ya asignados
        Set<String> profesoresAsignados = new HashSet<>();

        // 1. ASIGNAR AULA DE CONVIVENCIA (siempre primero)
        String profesorConvivencia = asignarProfesorConvivencia(profesoresGuardia, diaSemana, hora);
        if (profesorConvivencia != null) {
            profesoresAsignados.add(profesorConvivencia);
            // Crear cobertura especial para convivencia
            crearCoberturaConvivencia(horasAusencia.get(0), profesorConvivencia, fecha);
            logger.info("Aula de convivencia asignada a: {}", profesorConvivencia);
        }

        // Profesores disponibles para cubrir ausencias (sin el de convivencia)
        List<ProfesorGuardiaSimpleDto> profesoresDisponibles = profesoresGuardia.stream()
                .filter(p -> !profesoresAsignados.contains(p.getEmail()))
                .collect(Collectors.toList());

        // 2. ASIGNAR GRUPOS PROBLEMÁTICOS
        List<HoraAusencia> horasProblematicas = horasAusencia.stream()
                .filter(h -> verificarSiGrupoEsProblematico(h.getGrupo()))
                .collect(Collectors.toList());

        for (HoraAusencia horaProblematica : horasProblematicas) {
            String profesor = asignarProfesor(profesoresDisponibles, diaSemana, hora, 
                                            TipoGuardia.PROBLEMATICA, profesoresAsignados);
            if (profesor != null) {
                crearCobertura(horaProblematica, profesor, TipoGuardia.PROBLEMATICA);
                profesoresAsignados.add(profesor);
            }
        }

        // 3. ASIGNAR GUARDIAS NORMALES
        List<HoraAusencia> horasNormales = horasAusencia.stream()
                .filter(h -> !verificarSiGrupoEsProblematico(h.getGrupo()))
                .collect(Collectors.toList());

        for (HoraAusencia horaNormal : horasNormales) {
            String profesor = asignarProfesor(profesoresDisponibles, diaSemana, hora, 
                                            TipoGuardia.NORMAL, profesoresAsignados);
            if (profesor != null) {
                crearCobertura(horaNormal, profesor, TipoGuardia.NORMAL);
                profesoresAsignados.add(profesor);
            }
        }

        logger.info("Asignadas {} coberturas para día {} hora {}", 
                   profesoresAsignados.size(), diaSemana, hora);
    }

    /**
     * Asigna el profesor al aula de convivencia (el que tiene menos guardiasConvivencia)
     */
    private String asignarProfesorConvivencia(List<ProfesorGuardiaSimpleDto> profesores, 
                                             DiaSemana dia, Integer hora) {
        // Obtener o crear contadores
        Map<String, ContadorGuardias> contadores = obtenerOCrearContadores(profesores, dia, hora);

        // Ordenar por guardiasConvivencia ascendente
        return contadores.values().stream()
                .sorted(Comparator.comparingInt(ContadorGuardias::getGuardiasConvivencia)
                        .thenComparing(ContadorGuardias::getProfesorEmail))
                .findFirst()
                .map(ContadorGuardias::getProfesorEmail)
                .orElse(null);
    }

    /**
     * Asigna un profesor para cubrir una ausencia según el tipo de guardia
     */
    private String asignarProfesor(List<ProfesorGuardiaSimpleDto> profesores, DiaSemana dia, 
                                   Integer hora, TipoGuardia tipoGuardia) {
        return asignarProfesor(profesores, dia, hora, tipoGuardia, new HashSet<>());
    }

    /**
     * Asigna un profesor evitando los ya asignados
     */
    private String asignarProfesor(List<ProfesorGuardiaSimpleDto> profesores, DiaSemana dia, 
                                   Integer hora, TipoGuardia tipoGuardia, 
                                   Set<String> profesoresYaAsignados) {
        // Filtrar profesores no asignados aún
        List<ProfesorGuardiaSimpleDto> profesoresDisponibles = profesores.stream()
                .filter(p -> !profesoresYaAsignados.contains(p.getEmail()))
                .collect(Collectors.toList());

        if (profesoresDisponibles.isEmpty()) {
            logger.warn("No hay profesores disponibles para tipo {} en día {} hora {}", 
                       tipoGuardia, dia, hora);
            return null;
        }

        Map<String, ContadorGuardias> contadores = obtenerOCrearContadores(profesoresDisponibles, dia, hora);

        // Ordenar según el tipo de guardia
        Comparator<ContadorGuardias> comparador;
        switch (tipoGuardia) {
            case PROBLEMATICA:
                comparador = Comparator.comparingInt(ContadorGuardias::getGuardiasProblematicas);
                break;
            case CONVIVENCIA:
                comparador = Comparator.comparingInt(ContadorGuardias::getGuardiasConvivencia);
                break;
            default:
                comparador = Comparator.comparingInt(ContadorGuardias::getGuardiasNormales);
        }

        return contadores.values().stream()
                .sorted(comparador.thenComparing(ContadorGuardias::getProfesorEmail))
                .findFirst()
                .map(ContadorGuardias::getProfesorEmail)
                .orElse(null);
    }

    /**
     * Crea una cobertura estándar
     */
    private void crearCobertura(HoraAusencia horaAusencia, String profesorEmail, TipoGuardia tipoGuardia) {
        Cobertura cobertura = new Cobertura();
        cobertura.setHoraAusencia(horaAusencia);
        cobertura.setProfesorCubreEmail(profesorEmail);
        cobertura.setGrupo(horaAusencia.getGrupo());
        cobertura.setAula(horaAusencia.getAula());
        cobertura.setTipoGuardia(tipoGuardia);
        cobertura.setEstado(EstadoCobertura.ASIGNADA);
        cobertura.setValidadaPorAdmin(false);

        coberturaRepository.save(cobertura);
    }

    /**
     * Crea una cobertura especial para el aula de convivencia
     */
    private void crearCoberturaConvivencia(HoraAusencia horaAusencia, String profesorEmail, LocalDate fecha) {
        Cobertura cobertura = new Cobertura();
        cobertura.setHoraAusencia(horaAusencia);
        cobertura.setProfesorCubreEmail(profesorEmail);
        cobertura.setGrupo("CONVIVENCIA");
        cobertura.setAula("Aula de Convivencia");
        cobertura.setTipoGuardia(TipoGuardia.CONVIVENCIA);
        cobertura.setEstado(EstadoCobertura.ASIGNADA);
        cobertura.setValidadaPorAdmin(false);

        coberturaRepository.save(cobertura);
    }

    /**
     * Obtiene o crea contadores para los profesores
     */
    private Map<String, ContadorGuardias> obtenerOCrearContadores(
            List<ProfesorGuardiaSimpleDto> profesores, DiaSemana dia, Integer hora) {
        
        Map<String, ContadorGuardias> contadores = new HashMap<>();

        for (ProfesorGuardiaSimpleDto profesor : profesores) {
            ContadorGuardias contador = contadorRepository
                    .findByProfesorEmailAndDiaSemanaAndHora(profesor.getEmail(), dia, hora)
                    .orElseGet(() -> {
                        ContadorGuardias nuevo = new ContadorGuardias();
                        nuevo.setProfesorEmail(profesor.getEmail());
                        nuevo.setDiaSemana(dia);
                        nuevo.setHora(hora);
                        nuevo.setGuardiasNormales(0);
                        nuevo.setGuardiasProblematicas(0);
                        nuevo.setGuardiasConvivencia(0);
                        return contadorRepository.save(nuevo);
                    });

            contadores.put(profesor.getEmail(), contador);
        }

        return contadores;
    }

    /**
     * Obtiene los profesores de guardia desde el backend de horarios
     */
    @SuppressWarnings("unchecked")
    private List<ProfesorGuardiaSimpleDto> obtenerProfesoresGuardia(int dia, int hora) {
        try {
            String url = horariosApiUrl + "/horario/guardia/dia/" + dia + "/hora/" + hora;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.containsKey("profesores")) {
                List<Map<String, Object>> profesores = (List<Map<String, Object>>) response.get("profesores");
                
                return profesores.stream()
                        .map(p -> new ProfesorGuardiaSimpleDto(
                                (String) p.get("email"),
                                (String) p.get("nombre"),
                                (String) p.get("abreviatura")
                        ))
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            logger.error("Error obteniendo profesores de guardia: {}", e.getMessage(), e);
        }

        return List.of();
    }

    /**
     * Verifica si un grupo es problemático consultando el backend de horarios
     */
    @SuppressWarnings("unchecked")
    private boolean verificarSiGrupoEsProblematico(String abreviaturaGrupo) {
        try {
            String url = horariosApiUrl + "/grupos/abreviatura/" + abreviaturaGrupo;
            Map<String, Object> grupo = restTemplate.getForObject(url, Map.class);

            if (grupo != null && grupo.containsKey("esProblematico")) {
                return (Boolean) grupo.get("esProblematico");
            }
        } catch (Exception e) {
            logger.warn("No se pudo verificar si grupo {} es problemático: {}", 
                       abreviaturaGrupo, e.getMessage());
        }

        return false; // Por defecto no es problemático
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
