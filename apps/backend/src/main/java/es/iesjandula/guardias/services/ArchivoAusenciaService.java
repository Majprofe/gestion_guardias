package es.iesjandula.guardias.services;

import es.iesjandula.guardias.models.ArchivoAusencia;
import es.iesjandula.guardias.models.Ausencia;
import es.iesjandula.guardias.repositories.ArchivoAusenciaRepository;
import es.iesjandula.guardias.repositories.AusenciaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servicio para gestionar archivos adjuntos a ausencias.
 */
@Service
public class ArchivoAusenciaService {

    private static final Logger logger = LoggerFactory.getLogger(ArchivoAusenciaService.class);

    @Autowired
    private ArchivoAusenciaRepository archivoRepository;

    @Autowired
    private AusenciaRepository ausenciaRepository;

    @Value("${app.archivos.directorio:uploads/ausencias}")
    private String directorioArchivos;

    @Value("${app.archivos.tamaño-maximo:10485760}") // 10MB por defecto
    private long tamañoMaximoArchivo;

    /**
     * Guarda uno o varios archivos para una ausencia específica.
     */
    @Transactional
    public List<ArchivoAusencia> guardarArchivos(Long ausenciaId, MultipartFile[] archivos) {
        // Verificar que la ausencia existe
        Ausencia ausencia = ausenciaRepository.findById(ausenciaId)
                .orElseThrow(() -> new RuntimeException("Ausencia no encontrada: " + ausenciaId));

        List<ArchivoAusencia> archivosGuardados = new ArrayList<>();

        for (MultipartFile archivo : archivos) {
            if (!archivo.isEmpty()) {
                try {
                    ArchivoAusencia archivoGuardado = guardarArchivo(ausencia, archivo);
                    archivosGuardados.add(archivoGuardado);
                } catch (Exception e) {
                    logger.error("Error al guardar archivo {}: {}", archivo.getOriginalFilename(), e.getMessage());
                    throw new RuntimeException("Error al guardar archivo: " + archivo.getOriginalFilename(), e);
                }
            }
        }

        return archivosGuardados;
    }

    /**
     * Guarda un archivo individual.
     */
    private ArchivoAusencia guardarArchivo(Ausencia ausencia, MultipartFile archivo) throws IOException {
        // Validar tamaño del archivo
        if (archivo.getSize() > tamañoMaximoArchivo) {
            throw new RuntimeException("El archivo " + archivo.getOriginalFilename() + 
                    " excede el tamaño máximo permitido (" + (tamañoMaximoArchivo / 1024 / 1024) + " MB)");
        }

        // Crear directorio si no existe
        Path directorio = Paths.get(directorioArchivos);
        if (!Files.exists(directorio)) {
            Files.createDirectories(directorio);
        }

        // Generar nombre único para el archivo
        String nombreOriginal = archivo.getOriginalFilename();
        String extension = "";
        if (nombreOriginal != null && nombreOriginal.contains(".")) {
            extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        }
        String nombreUnico = UUID.randomUUID().toString() + extension;

        // Guardar archivo físicamente
        Path rutaArchivo = directorio.resolve(nombreUnico);
        Files.copy(archivo.getInputStream(), rutaArchivo, StandardCopyOption.REPLACE_EXISTING);

        // Crear entidad ArchivoAusencia
        ArchivoAusencia archivoAusencia = new ArchivoAusencia();
        archivoAusencia.setAusencia(ausencia);
        archivoAusencia.setNombreArchivo(nombreOriginal);
        archivoAusencia.setRutaArchivo(rutaArchivo.toString());
        archivoAusencia.setTipoArchivo(archivo.getContentType());
        archivoAusencia.setTamanoArchivo(archivo.getSize());
        archivoAusencia.setFechaSubida(LocalDateTime.now());

        // Guardar en base de datos
        ArchivoAusencia archivoGuardado = archivoRepository.save(archivoAusencia);

        logger.info("Archivo guardado: {} para ausencia {}", nombreOriginal, ausencia.getId());
        return archivoGuardado;
    }

    /**
     * Obtiene todos los archivos de una ausencia específica.
     */
    public List<ArchivoAusencia> obtenerArchivosPorAusencia(Long ausenciaId) {
        return archivoRepository.findByAusenciaIdOrderByFechaSubidaDesc(ausenciaId);
    }

    /**
     * Carga un archivo para descarga.
     */
    public Resource cargarArchivo(Long archivoId) {
        ArchivoAusencia archivo = archivoRepository.findById(archivoId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado: " + archivoId));

        try {
            Path rutaArchivo = Paths.get(archivo.getRutaArchivo());
            Resource resource = new UrlResource(rutaArchivo.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado o no legible: " + archivo.getNombreArchivo());
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error al cargar archivo: " + archivo.getNombreArchivo(), e);
        }
    }

    /**
     * Obtiene los metadatos de un archivo.
     */
    public ArchivoAusencia obtenerMetadatosArchivo(Long archivoId) {
        return archivoRepository.findById(archivoId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado: " + archivoId));
    }

    /**
     * Elimina un archivo (tanto el registro como el archivo físico).
     */
    @Transactional
    public void eliminarArchivo(Long archivoId) {
        ArchivoAusencia archivo = archivoRepository.findById(archivoId)
                .orElseThrow(() -> new RuntimeException("Archivo no encontrado: " + archivoId));

        try {
            // Eliminar archivo físico
            Path rutaArchivo = Paths.get(archivo.getRutaArchivo());
            if (Files.exists(rutaArchivo)) {
                Files.delete(rutaArchivo);
            }

            // Eliminar registro de base de datos
            archivoRepository.delete(archivo);

            logger.info("Archivo eliminado: {} (ID: {})", archivo.getNombreArchivo(), archivoId);
        } catch (IOException e) {
            logger.error("Error al eliminar archivo físico: {}", e.getMessage());
            throw new RuntimeException("Error al eliminar archivo: " + archivo.getNombreArchivo(), e);
        }
    }

    /**
     * Obtiene estadísticas de archivos.
     */
    public ArchivoEstadisticas obtenerEstadisticas() {
        long totalArchivos = archivoRepository.count();
        long tamañoTotal = archivoRepository.sumTamañoArchivos().orElse(0L);
        
        return new ArchivoEstadisticas(totalArchivos, tamañoTotal);
    }

    /**
     * Clase interna para estadísticas de archivos.
     */
    public static class ArchivoEstadisticas {
        private final long totalArchivos;
        private final long tamañoTotalBytes;

        public ArchivoEstadisticas(long totalArchivos, long tamañoTotalBytes) {
            this.totalArchivos = totalArchivos;
            this.tamañoTotalBytes = tamañoTotalBytes;
        }

        public long getTotalArchivos() {
            return totalArchivos;
        }

        public long getTamañoTotalBytes() {
            return tamañoTotalBytes;
        }

        public String getTamañoTotalFormateado() {
            if (tamañoTotalBytes == 0) return "0 B";
            
            double bytes = tamañoTotalBytes;
            String[] unidades = {"B", "KB", "MB", "GB"};
            int unidadIndex = 0;
            
            while (bytes >= 1024 && unidadIndex < unidades.length - 1) {
                bytes /= 1024;
                unidadIndex++;
            }
            
            return String.format("%.1f %s", bytes, unidades[unidadIndex]);
        }
    }
}
