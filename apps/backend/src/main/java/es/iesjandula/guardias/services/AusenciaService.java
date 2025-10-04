package es.iesjandula.guardias.services;

import es.iesjandula.guardias.dto.AusenciaConGuardiasDTO;
import es.iesjandula.guardias.dto.AusenciaResponseDTO;
import es.iesjandula.guardias.dto.ArchivoDTO;
import es.iesjandula.guardias.dto.CoberturaDTO;
import es.iesjandula.guardias.dto.CrearAusenciaDTO;
import es.iesjandula.guardias.dto.CrearAusenciaMultipleDTO;
import es.iesjandula.guardias.dto.HoraAusenciaDTO;
import es.iesjandula.guardias.dto.HoraAusenciaConCoberturaDTO;
import es.iesjandula.guardias.exception.BusinessException;
import es.iesjandula.guardias.exception.ResourceNotFoundException;
import es.iesjandula.guardias.models.*;
import es.iesjandula.guardias.repositories.AusenciaRepository;
import es.iesjandula.guardias.repositories.CoberturaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
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

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private es.iesjandula.guardias.repositories.ArchivoHoraAusenciaRepository archivoRepository;

    @Autowired
    private CoberturaAsignacionService coberturaAsignacionService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.horarios.api.url:http://localhost:8082}")
    private String horariosApiUrl;

    // Métodos del modelo viejo comentados temporalmente - serán migrados después
    // public AusenciaConGuardiasDTO guardarYAsignarCobertura(CrearAusenciaDTO crearDto) {...}
    // public List<AusenciaConGuardiasDTO> guardarYAsignarCoberturaMultiple(CrearAusenciaMultipleDTO crearDto) {...}
    // public Optional<List<Ausencia>> listarPorFechaHora(LocalDate fecha, Integer hora) {...}
    // public Map<String, List<AusenciaConGuardiasDTO>> listarAusenciasPorFechaAgrupadasPorHora(LocalDate fecha) {...}
    // public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoFaltas() {...}
    // public Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historicoFaltasPorProfesor(String emailProfesor) {...}
    // public List<AusenciaConGuardiasDTO> obtenerAusenciasPorProfesor(String email) {...}

    public void eliminarPorId(Long id) {
        if (!ausenciaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Ausencia", "id", id);
        }
        ausenciaRepository.deleteById(id);
        logger.info("Ausencia con ID {} eliminada correctamente", id);
    }

    /**
     * Crea una ausencia con múltiples horas.
     * Este método maneja el nuevo formato donde una ausencia representa un día completo
     * con múltiples horas afectadas.
     *
     * @param crearDto DTO con los datos de la ausencia y las horas afectadas.
     * @return AusenciaResponseDTO con la ausencia creada y sus horas.
     * @throws BusinessException Si ya existe una ausencia para el profesor en la fecha especificada.
     */
    public AusenciaResponseDTO crearAusenciaConCoberturas(CrearAusenciaDTO crearDto) {
        logger.debug("Creando ausencia: {}", crearDto);

        // Verificar que no exista ya una ausencia para este profesor en esta fecha
        if (ausenciaRepository.existsByProfesorAusenteEmailAndFecha(
                crearDto.getProfesorAusenteEmail(), crearDto.getFecha())) {
            logger.warn("Ya existe una ausencia para el profesor {} en la fecha {}",
                    crearDto.getProfesorAusenteEmail(), crearDto.getFecha());
            throw new BusinessException(
                "Ya existe una ausencia registrada para el profesor " + crearDto.getProfesorAusenteEmail() + 
                " en la fecha " + crearDto.getFecha(),
                HttpStatus.CONFLICT
            );
        }

        // Crear la ausencia principal (nivel día)
        Ausencia ausencia = new Ausencia();
        ausencia.setProfesorAusenteEmail(crearDto.getProfesorAusenteEmail());
        ausencia.setFecha(crearDto.getFecha());

        // Crear las horas de ausencia
        List<HoraAusenciaConCoberturaDTO> horasConCobertura = new ArrayList<>();
        
        for (es.iesjandula.guardias.dto.HoraAusenciaDTO horaDto : crearDto.getHoras()) {
            // Crear HoraAusencia
            HoraAusencia horaAusencia = new HoraAusencia();
            horaAusencia.setHora(horaDto.getHora());
            horaAusencia.setGrupo(horaDto.getGrupo());
            horaAusencia.setAula(horaDto.getAula());
            horaAusencia.setTarea(horaDto.getTarea());

            // Agregar a la ausencia usando el helper method
            ausencia.addHoraAusencia(horaAusencia);

            // Crear DTO de respuesta para esta hora
            HoraAusenciaConCoberturaDTO horaConCoberturaDto = new HoraAusenciaConCoberturaDTO();
            horaConCoberturaDto.setHora(horaAusencia.getHora());
            horaConCoberturaDto.setGrupo(horaAusencia.getGrupo());
            horaConCoberturaDto.setAula(horaAusencia.getAula());
            horaConCoberturaDto.setTarea(horaAusencia.getTarea());
            horaConCoberturaDto.setCobertura(null); // Por ahora sin cobertura automática

            horasConCobertura.add(horaConCoberturaDto);
        }

        // Guardar la ausencia con todas sus horas (cascade ALL)
        ausencia = ausenciaRepository.save(ausencia);
        logger.info("Ausencia creada con ID: {} y {} horas", ausencia.getId(), ausencia.getHoras().size());

        // ASIGNAR COBERTURAS AUTOMÁTICAMENTE
        // Primero, verificar si alguna de las horas ya tiene coberturas asignadas
        List<Integer> horasConCoberturaExistente = new ArrayList<>();
        for (HoraAusencia horaAusencia : ausencia.getHoras()) {
            List<Cobertura> coberturasExistentes = coberturaRepository.findByHoraAusencia_Ausencia_FechaAndHoraAusencia_NumeroHora(
                crearDto.getFecha(), horaAusencia.getHora()
            );
            if (!coberturasExistentes.isEmpty()) {
                horasConCoberturaExistente.add(horaAusencia.getHora());
            }
        }
        
        try {
            if (!horasConCoberturaExistente.isEmpty()) {
                // Si hay horas con coberturas existentes, reasignar TODAS las coberturas de esas horas
                logger.info("Detectadas coberturas existentes en horas {}. Reasignando para distribución equitativa", 
                    horasConCoberturaExistente);
                coberturaAsignacionService.reasignarCoberturasDelDia(crearDto.getFecha(), horasConCoberturaExistente);
            } else {
                // Si no hay coberturas previas, asignar normalmente
                coberturaAsignacionService.asignarCoberturas(ausencia, crearDto.getFecha());
                logger.info("Coberturas asignadas automáticamente para ausencia ID: {}", ausencia.getId());
            }
        } catch (Exception e) {
            logger.error("Error asignando coberturas para ausencia {}: {}", ausencia.getId(), e.getMessage(), e);
        }

        // Actualizar los IDs en los DTOs
        for (int i = 0; i < ausencia.getHoras().size(); i++) {
            horasConCobertura.get(i).setId(ausencia.getHoras().get(i).getId());
        }

        // Crear DTO de respuesta
        AusenciaResponseDTO responseDto = new AusenciaResponseDTO();
        responseDto.setId(ausencia.getId());
        responseDto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
        responseDto.setFecha(ausencia.getFecha());
        responseDto.setHoras(horasConCobertura);

        logger.info("Ausencia creada exitosamente con {} horas", horasConCobertura.size());
        return responseDto;
    }

    /**
     * Crea una ausencia con múltiples horas Y archivos asociados
     * 
     * @param crearDto DTO con los datos de la ausencia
     * @param archivos Mapa con archivos por hora: key=índice_hora (0,1,2...), value=array de archivos
     * @return AusenciaResponseDTO con la ausencia creada, sus horas y archivos
     */
    public AusenciaResponseDTO crearAusenciaConArchivos(CrearAusenciaDTO crearDto, 
                                                         Map<Integer, MultipartFile[]> archivos) {
        logger.debug("Creando ausencia con archivos: {}", crearDto);

        // Validar límite de archivos por hora
        for (Map.Entry<Integer, MultipartFile[]> entry : archivos.entrySet()) {
            if (entry.getValue() != null && entry.getValue().length > FileStorageService.MAX_FILES_PER_HORA) {
                throw new BusinessException(
                    String.format("La hora %d excede el límite de %d archivos por hora", 
                        entry.getKey() + 1, FileStorageService.MAX_FILES_PER_HORA),
                    HttpStatus.BAD_REQUEST
                );
            }
        }

        // Verificar que no exista ya una ausencia para este profesor en esta fecha
        if (ausenciaRepository.existsByProfesorAusenteEmailAndFecha(
                crearDto.getProfesorAusenteEmail(), crearDto.getFecha())) {
            throw new BusinessException(
                "Ya existe una ausencia registrada para el profesor " + crearDto.getProfesorAusenteEmail() + 
                " en la fecha " + crearDto.getFecha(),
                HttpStatus.CONFLICT
            );
        }

        // Crear la ausencia principal
        Ausencia ausencia = new Ausencia();
        ausencia.setProfesorAusenteEmail(crearDto.getProfesorAusenteEmail());
        ausencia.setFecha(crearDto.getFecha());

        // Guardar primero la ausencia para obtener su ID
        ausencia = ausenciaRepository.save(ausencia);
        logger.info("Ausencia creada con ID: {}", ausencia.getId());

        List<HoraAusenciaConCoberturaDTO> horasConCobertura = new ArrayList<>();
        
        // Crear las horas de ausencia y procesar archivos
        for (int i = 0; i < crearDto.getHoras().size(); i++) {
            es.iesjandula.guardias.dto.HoraAusenciaDTO horaDto = crearDto.getHoras().get(i);
            
            // Crear HoraAusencia
            HoraAusencia horaAusencia = new HoraAusencia();
            horaAusencia.setHora(horaDto.getHora());
            horaAusencia.setGrupo(horaDto.getGrupo());
            horaAusencia.setAula(horaDto.getAula());
            horaAusencia.setTarea(horaDto.getTarea());
            
            // Agregar a la ausencia
            ausencia.addHoraAusencia(horaAusencia);
        }
        
        // Guardar la ausencia con todas sus horas
        ausencia = ausenciaRepository.save(ausencia);
        
        // ASIGNAR COBERTURAS AUTOMÁTICAMENTE
        // Primero, verificar si alguna de las horas ya tiene coberturas asignadas
        List<Integer> horasConCoberturaExistente = new ArrayList<>();
        for (HoraAusencia horaAusencia : ausencia.getHoras()) {
            List<Cobertura> coberturasExistentes = coberturaRepository.findByHoraAusencia_Ausencia_FechaAndHoraAusencia_NumeroHora(
                crearDto.getFecha(), horaAusencia.getHora()
            );
            if (!coberturasExistentes.isEmpty()) {
                horasConCoberturaExistente.add(horaAusencia.getHora());
            }
        }
        
        try {
            if (!horasConCoberturaExistente.isEmpty()) {
                // Si hay horas con coberturas existentes, reasignar TODAS las coberturas de esas horas
                logger.info("Detectadas coberturas existentes en horas {}. Reasignando para distribución equitativa", 
                    horasConCoberturaExistente);
                coberturaAsignacionService.reasignarCoberturasDelDia(crearDto.getFecha(), horasConCoberturaExistente);
            } else {
                // Si no hay coberturas previas, asignar normalmente
                coberturaAsignacionService.asignarCoberturas(ausencia, crearDto.getFecha());
                logger.info("Coberturas asignadas automáticamente para ausencia ID: {}", ausencia.getId());
            }
        } catch (Exception e) {
            logger.error("Error asignando coberturas para ausencia {}: {}", ausencia.getId(), e.getMessage(), e);
        }
        
        // Ahora procesar archivos para cada hora
        for (int i = 0; i < ausencia.getHoras().size(); i++) {
            HoraAusencia horaAusencia = ausencia.getHoras().get(i);
            List<ArchivoDTO> archivosDto = new ArrayList<>();
            
            // Si hay archivos para esta hora
            MultipartFile[] archivosHora = archivos.get(i);
            if (archivosHora != null && archivosHora.length > 0) {
                for (MultipartFile archivo : archivosHora) {
                    if (archivo != null && !archivo.isEmpty()) {
                        // Guardar archivo físicamente
                        String[] archivoInfo = fileStorageService.storeFile(
                            archivo, ausencia.getId(), horaAusencia.getId()
                        );
                        
                        // Crear registro en BD
                        ArchivoHoraAusencia archivoEntity = new ArchivoHoraAusencia(
                            horaAusencia,
                            archivo.getOriginalFilename(),
                            archivoInfo[1], // nombreAlmacenado
                            archivoInfo[0], // rutaArchivo
                            archivo.getContentType(),
                            archivo.getSize()
                        );
                        
                        horaAusencia.addArchivo(archivoEntity);
                        archivoRepository.save(archivoEntity);
                        
                        // Crear DTO para respuesta
                        ArchivoDTO archivoDto = new ArchivoDTO();
                        archivoDto.setId(archivoEntity.getId());
                        archivoDto.setNombreArchivo(archivoEntity.getNombreArchivo());
                        archivoDto.setTamanio(archivoEntity.getTamanio());
                        archivoDto.setTamanioFormateado(ArchivoDTO.formatFileSize(archivoEntity.getTamanio()));
                        archivoDto.setTipoMime(archivoEntity.getTipoMime());
                        archivoDto.setFechaSubida(archivoEntity.getFechaSubida());
                        archivoDto.setUrlDescarga("/api/ausencias/archivos/" + archivoEntity.getId() + "/download");
                        
                        archivosDto.add(archivoDto);
                    }
                }
            }
            
            // Crear DTO de respuesta para esta hora
            HoraAusenciaConCoberturaDTO horaConCoberturaDto = new HoraAusenciaConCoberturaDTO();
            horaConCoberturaDto.setId(horaAusencia.getId());
            horaConCoberturaDto.setHora(horaAusencia.getHora());
            horaConCoberturaDto.setGrupo(horaAusencia.getGrupo());
            horaConCoberturaDto.setAula(horaAusencia.getAula());
            horaConCoberturaDto.setTarea(horaAusencia.getTarea());
            horaConCoberturaDto.setCobertura(null);
            horaConCoberturaDto.setArchivos(archivosDto);
            
            horasConCobertura.add(horaConCoberturaDto);
        }

        // Crear DTO de respuesta
        AusenciaResponseDTO responseDto = new AusenciaResponseDTO();
        responseDto.setId(ausencia.getId());
        responseDto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
        responseDto.setFecha(ausencia.getFecha());
        responseDto.setHoras(horasConCobertura);

        logger.info("Ausencia creada exitosamente con {} horas y archivos", horasConCobertura.size());
        return responseDto;
    }

    /**
     * Obtiene todas las ausencias de una fecha específica, agrupadas por hora
     * 
     * @param fecha Fecha de las ausencias a buscar
     * @return Map con las ausencias agrupadas por hora (key=hora, value=lista de ausencias)
     */
    public Map<String, List<AusenciaResponseDTO>> obtenerAusenciasPorFecha(LocalDate fecha) {
        logger.debug("Obteniendo ausencias para fecha: {}", fecha);
        
        List<Ausencia> ausencias = ausenciaRepository.findByFecha(fecha);
        Map<String, List<AusenciaResponseDTO>> ausenciasPorHora = new HashMap<>();
        
        for (Ausencia ausencia : ausencias) {
            // Convertir cada Ausencia a AusenciaResponseDTO
            for (HoraAusencia horaAusencia : ausencia.getHoras()) {
                String horaKey = String.valueOf(horaAusencia.getHora());
                
                // Crear DTO para esta hora específica
                AusenciaResponseDTO ausenciaDto = new AusenciaResponseDTO();
                ausenciaDto.setId(ausencia.getId());
                ausenciaDto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
                // Obtener nombre del profesor ausente desde backend de horarios
                ausenciaDto.setProfesorAusenteNombre(obtenerNombreProfesor(ausencia.getProfesorAusenteEmail()));
                ausenciaDto.setFecha(ausencia.getFecha());
                
                // Crear HoraAusenciaConCoberturaDTO con archivos
                HoraAusenciaConCoberturaDTO horaDto = new HoraAusenciaConCoberturaDTO();
                horaDto.setId(horaAusencia.getId());
                horaDto.setHora(horaAusencia.getHora());
                horaDto.setGrupo(horaAusencia.getGrupo());
                horaDto.setAula(horaAusencia.getAula());
                horaDto.setTarea(horaAusencia.getTarea());
                
                // Obtener cobertura si existe
                Optional<Cobertura> coberturaOpt = coberturaRepository.findByHoraAusenciaId(horaAusencia.getId());
                if (coberturaOpt.isPresent()) {
                    Cobertura cobertura = coberturaOpt.get();
                    CoberturaDTO coberturaDto = new CoberturaDTO();
                    coberturaDto.setProfesorCubreEmail(cobertura.getProfesorCubreEmail());
                    // Obtener nombre del profesor que cubre desde backend de horarios
                    coberturaDto.setProfesorCubreNombre(obtenerNombreProfesor(cobertura.getProfesorCubreEmail()));
                    coberturaDto.setProfesorAusenteEmail(ausencia.getProfesorAusenteEmail());
                    coberturaDto.setGrupo(horaAusencia.getGrupo());
                    coberturaDto.setAula(horaAusencia.getAula());
                    coberturaDto.setHora(horaAusencia.getHora());
                    coberturaDto.setFecha(ausencia.getFecha());
                    coberturaDto.setTarea(horaAusencia.getTarea());
                    horaDto.setCobertura(coberturaDto);
                }
                
                // Obtener archivos asociados
                List<ArchivoDTO> archivosDto = new ArrayList<>();
                for (ArchivoHoraAusencia archivo : horaAusencia.getArchivos()) {
                    ArchivoDTO archivoDto = new ArchivoDTO();
                    archivoDto.setId(archivo.getId());
                    archivoDto.setNombreArchivo(archivo.getNombreArchivo());
                    archivoDto.setTamanio(archivo.getTamanio());
                    archivoDto.setTamanioFormateado(ArchivoDTO.formatFileSize(archivo.getTamanio()));
                    archivoDto.setTipoMime(archivo.getTipoMime());
                    archivoDto.setFechaSubida(archivo.getFechaSubida());
                    archivoDto.setUrlDescarga("/api/ausencias/archivos/" + archivo.getId() + "/download");
                    archivosDto.add(archivoDto);
                }
                horaDto.setArchivos(archivosDto);
                
                ausenciaDto.setHoras(Collections.singletonList(horaDto));
                
                // Agregar al mapa agrupado por hora
                ausenciasPorHora.computeIfAbsent(horaKey, k -> new ArrayList<>()).add(ausenciaDto);
            }
        }
        
        logger.info("Encontradas {} ausencias en {} horas diferentes para fecha {}", 
                    ausencias.size(), ausenciasPorHora.size(), fecha);
        return ausenciasPorHora;
    }

    /**
     * Método de test para verificar conectividad con la base de datos
     */
    public long contarTotalAusencias() {
        return ausenciaRepository.count();
    }

    /**
     * Obtiene el nombre completo de un profesor desde el backend de horarios
     * 
     * @param email Email del profesor
     * @return Nombre completo del profesor o null si no se encuentra
     */
    @SuppressWarnings("unchecked")
    private String obtenerNombreProfesor(String email) {
        if (email == null || email.isEmpty()) {
            return null;
        }
        
        try {
            String url = horariosApiUrl + "/profesores/email/" + email;
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            
            if (response != null && response.containsKey("nombre")) {
                return (String) response.get("nombre");
            }
        } catch (Exception e) {
            logger.warn("No se pudo obtener el nombre del profesor {}: {}", email, e.getMessage());
        }
        
        return null;
    }

}
