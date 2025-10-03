package es.iesjandula.guardias.services;

import es.iesjandula.guardias.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

/**
 * Servicio para gestionar el almacenamiento de archivos en el sistema de archivos local
 */
@Service
public class FileStorageService {

    // Tamaño máximo: 30MB en bytes
    private static final long MAX_FILE_SIZE = 30 * 1024 * 1024;
    
    // Tipo MIME permitido
    private static final String ALLOWED_MIME_TYPE = "application/pdf";
    
    // Máximo de archivos por hora
    public static final int MAX_FILES_PER_HORA = 3;

    private final Path fileStorageLocation;

    /**
     * Constructor que inicializa la ubicación de almacenamiento
     */
    public FileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        // Construir ruta absoluta desde la raíz del proyecto
        this.fileStorageLocation = Paths.get(uploadDir, "ausencias").toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new BusinessException("No se pudo crear el directorio de almacenamiento de archivos", ex);
        }
    }

    /**
     * Valida que el archivo cumple con las restricciones
     */
    public void validateFile(MultipartFile file) {
        // Validar que no esté vacío
        if (file.isEmpty()) {
            throw new BusinessException("El archivo está vacío");
        }

        // Validar tamaño
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(
                String.format("El archivo excede el tamaño máximo permitido de 30MB. Tamaño: %.2f MB", 
                    file.getSize() / (1024.0 * 1024.0))
            );
        }

        // Validar tipo MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(ALLOWED_MIME_TYPE)) {
            throw new BusinessException(
                String.format("Tipo de archivo no permitido. Solo se permiten archivos PDF. Tipo recibido: %s", 
                    contentType)
            );
        }

        // Validar extensión del nombre de archivo
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("El archivo debe tener extensión .pdf");
        }
    }

    /**
     * Guarda un archivo en el sistema de archivos
     * 
     * @param file Archivo a guardar
     * @param ausenciaId ID de la ausencia
     * @param horaAusenciaId ID de la hora de ausencia
     * @return Información del archivo guardado [rutaRelativa, nombreAlmacenado]
     */
    public String[] storeFile(MultipartFile file, Long ausenciaId, Long horaAusenciaId) {
        validateFile(file);

        String originalFileName = file.getOriginalFilename();
        
        // Generar nombre único para evitar colisiones
        String timestamp = String.valueOf(System.currentTimeMillis());
        String storedFileName = String.format("ausencia_%d_hora_%d_%s_%s", 
            ausenciaId, horaAusenciaId, timestamp, originalFileName);

        try {
            // Crear estructura de directorios por fecha (año/mes/día)
            LocalDate today = LocalDate.now();
            Path dateDirectory = this.fileStorageLocation
                .resolve(String.valueOf(today.getYear()))
                .resolve(String.format("%02d", today.getMonthValue()))
                .resolve(String.format("%02d", today.getDayOfMonth()));
            
            Files.createDirectories(dateDirectory);

            // Ruta completa del archivo
            Path targetLocation = dateDirectory.resolve(storedFileName);

            // Copiar archivo
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Ruta relativa desde fileStorageLocation
            String relativePath = this.fileStorageLocation.relativize(targetLocation).toString()
                .replace("\\", "/"); // Normalizar separadores para todas las plataformas

            return new String[]{relativePath, storedFileName};

        } catch (IOException ex) {
            throw new BusinessException("Error al almacenar el archivo " + originalFileName, ex);
        }
    }

    /**
     * Carga un archivo como Resource para descarga
     */
    public Resource loadFileAsResource(String rutaArchivo) {
        try {
            Path filePath = this.fileStorageLocation.resolve(rutaArchivo).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new BusinessException("Archivo no encontrado: " + rutaArchivo);
            }
        } catch (MalformedURLException ex) {
            throw new BusinessException("Archivo no encontrado: " + rutaArchivo, ex);
        }
    }

    /**
     * Elimina un archivo del sistema de archivos
     */
    public void deleteFile(String rutaArchivo) {
        try {
            Path filePath = this.fileStorageLocation.resolve(rutaArchivo).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            // Log del error pero no lanzar excepción crítica
            System.err.println("Error al eliminar archivo: " + rutaArchivo + " - " + ex.getMessage());
        }
    }
}
