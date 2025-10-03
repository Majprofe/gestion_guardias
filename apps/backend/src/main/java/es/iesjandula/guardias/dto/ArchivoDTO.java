package es.iesjandula.guardias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para transferir información de archivos al frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoDTO {
    
    private Long id;
    
    /**
     * Nombre original del archivo
     */
    private String nombreArchivo;
    
    /**
     * Tamaño del archivo en bytes
     */
    private Long tamanio;
    
    /**
     * Tamaño formateado (KB, MB)
     */
    private String tamanioFormateado;
    
    /**
     * Tipo MIME del archivo
     */
    private String tipoMime;
    
    /**
     * Fecha de subida
     */
    private LocalDateTime fechaSubida;
    
    /**
     * URL para descargar el archivo
     */
    private String urlDescarga;

    /**
     * Formatea el tamaño de bytes a una representación legible
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
