package es.iesjandula.guardias.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesjandula.guardias.dto.AusenciaConGuardiasDTO;
import es.iesjandula.guardias.dto.AusenciaResponseDTO;
import es.iesjandula.guardias.dto.CrearAusenciaDTO;
import es.iesjandula.guardias.dto.CrearAusenciaMultipleDTO;
import es.iesjandula.guardias.models.ArchivoHoraAusencia;
import es.iesjandula.guardias.repositories.ArchivoHoraAusenciaRepository;
import es.iesjandula.guardias.services.AusenciaService;
import es.iesjandula.guardias.services.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.format.annotation.DateTimeFormat;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestionar ausencias
 */
@RestController
@RequestMapping("/api/ausencias")
@Tag(name = "Ausencias", description = "Gestión de ausencias de profesores")
public class AusenciaController {
    
    @Autowired
    private AusenciaService ausenciaService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ArchivoHoraAusenciaRepository archivoRepository;
    
    /**
     * Crea una nueva ausencia con múltiples horas, archivos opcionales y asigna automáticamente las guardias
     * 
     * Este endpoint acepta multipart/form-data con:
     * - ausenciaData: JSON con los datos de la ausencia (CrearAusenciaDTO)
     * - archivos_hora_0, archivos_hora_1, etc: Arrays de archivos PDF (máx 3 por hora, máx 30MB cada uno)
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Crear ausencia con archivos", 
               description = "Crea una ausencia de un día completo con múltiples horas, archivos opcionales y asigna automáticamente las guardias")
    public ResponseEntity<AusenciaResponseDTO> crearAusencia(
            @RequestPart("ausenciaData") String ausenciaDataJson,
            @RequestPart(value = "archivos_hora_0", required = false) MultipartFile[] archivosHora0,
            @RequestPart(value = "archivos_hora_1", required = false) MultipartFile[] archivosHora1,
            @RequestPart(value = "archivos_hora_2", required = false) MultipartFile[] archivosHora2,
            @RequestPart(value = "archivos_hora_3", required = false) MultipartFile[] archivosHora3,
            @RequestPart(value = "archivos_hora_4", required = false) MultipartFile[] archivosHora4,
            @RequestPart(value = "archivos_hora_5", required = false) MultipartFile[] archivosHora5) throws IOException {
        
        // Parse JSON data
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Para LocalDate
        CrearAusenciaDTO crearAusenciaDTO = objectMapper.readValue(ausenciaDataJson, CrearAusenciaDTO.class);
        
        // Construir mapa de archivos por hora
        Map<Integer, MultipartFile[]> archivos = new HashMap<>();
        if (archivosHora0 != null && archivosHora0.length > 0) archivos.put(0, archivosHora0);
        if (archivosHora1 != null && archivosHora1.length > 0) archivos.put(1, archivosHora1);
        if (archivosHora2 != null && archivosHora2.length > 0) archivos.put(2, archivosHora2);
        if (archivosHora3 != null && archivosHora3.length > 0) archivos.put(3, archivosHora3);
        if (archivosHora4 != null && archivosHora4.length > 0) archivos.put(4, archivosHora4);
        if (archivosHora5 != null && archivosHora5.length > 0) archivos.put(5, archivosHora5);
        
        // Crear ausencia con archivos si hay, sin archivos si no
        AusenciaResponseDTO ausencia;
        if (archivos.isEmpty()) {
            ausencia = ausenciaService.crearAusenciaConCoberturas(crearAusenciaDTO);
        } else {
            ausencia = ausenciaService.crearAusenciaConArchivos(crearAusenciaDTO, archivos);
        }
        
        return ResponseEntity.ok(ausencia);
    }
    
    /**
     * Obtiene todas las ausencias de una fecha específica, agrupadas por hora
     */
    @GetMapping("/fecha/{fecha}")
    @Operation(summary = "Obtener ausencias por fecha", 
               description = "Obtiene todas las ausencias de una fecha específica, agrupadas por hora, con sus coberturas y archivos")
    public ResponseEntity<Map<String, List<AusenciaResponseDTO>>> obtenerAusenciasPorFecha(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        Map<String, List<AusenciaResponseDTO>> ausencias = ausenciaService.obtenerAusenciasPorFecha(fecha);
        return ResponseEntity.ok(ausencias);
    }
    
    // TEMPORALMENTE DESHABILITADOS - Usan modelo antiguo
    // Serán migrados al nuevo modelo más adelante
    /*
    @GetMapping("/profesor/{profesorEmail}")
    public ResponseEntity<List<AusenciaConGuardiasDTO>> obtenerAusenciasProfesor(@PathVariable String profesorEmail) {
        List<AusenciaConGuardiasDTO> ausencias = ausenciaService.obtenerAusenciasPorProfesor(profesorEmail);
        return ResponseEntity.ok(ausencias);
    }
    
    @GetMapping("/historico")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>>> obtenerHistoricoFaltas() {
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = ausenciaService.historicoFaltas();
        return ResponseEntity.ok(historico);
    }
    
    @GetMapping("/historico/profesor/{profesorEmail}")
    public ResponseEntity<Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>>> obtenerHistoricoFaltasProfesor(@PathVariable String profesorEmail) {
        Map<LocalDate, Map<String, List<AusenciaConGuardiasDTO>>> historico = ausenciaService.historicoFaltasPorProfesor(profesorEmail);
        return ResponseEntity.ok(historico);
    }
    */
    
    /**
     * Elimina una ausencia
     */
    @DeleteMapping("/{ausenciaId}")
    @Operation(summary = "Eliminar ausencia", description = "Elimina una ausencia y su cobertura asociada")
    public ResponseEntity<Void> eliminarAusencia(@PathVariable Long ausenciaId) {
        ausenciaService.eliminarPorId(ausenciaId);
        return ResponseEntity.noContent().build();
    }
    
    // TEMPORALMENTE DESHABILITADO - Usa modelo antiguo
    /*
    @GetMapping("/hoy")
    public ResponseEntity<Map<String, List<AusenciaConGuardiasDTO>>> obtenerAusenciasHoy() {
        Map<String, List<AusenciaConGuardiasDTO>> ausencias = ausenciaService.listarAusenciasPorFechaAgrupadasPorHora(LocalDate.now());
        return ResponseEntity.ok(ausencias);
    }
    */
    
    /**
     * Descarga un archivo asociado a una hora de ausencia
     */
    @GetMapping("/archivos/{archivoId}/download")
    @Operation(summary = "Descargar archivo", description = "Descarga un archivo PDF asociado a una hora de ausencia")
    public ResponseEntity<Resource> descargarArchivo(
            @PathVariable Long archivoId,
            HttpServletRequest request) {
        
        // Buscar archivo en BD
        ArchivoHoraAusencia archivo = archivoRepository.findById(archivoId)
            .orElseThrow(() -> new es.iesjandula.guardias.exception.ResourceNotFoundException(
                "Archivo", "id", archivoId));
        
        // Cargar archivo como Resource
        Resource resource = fileStorageService.loadFileAsResource(archivo.getRutaArchivo());
        
        // Determinar content type
        String contentType = archivo.getTipoMime();
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + archivo.getNombreArchivo() + "\"")
                .body(resource);
    }
    
    /**
     * Test simple para verificar conectividad con la base de datos
     */
    @GetMapping("/test")
    @Operation(summary = "Test de conectividad", description = "Endpoint simple para verificar que el servicio funciona")
    public ResponseEntity<Map<String, Object>> test() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("timestamp", LocalDate.now());
        response.put("message", "Servicio de ausencias funcionando correctamente");
        
        try {
            // Test simple de base de datos
            long count = ausenciaService.contarTotalAusencias();
            response.put("totalAusenciasEnBD", count);
        } catch (Exception e) {
            response.put("errorBD", e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
