package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Entidad que representa un archivo adjunto a una ausencia
 */
@Entity
@Table(name = "archivos_ausencia",
       indexes = {
           @Index(name = "idx_archivo_ausencia", columnList = "ausencia_id"),
           @Index(name = "idx_archivo_fecha", columnList = "fechaSubida")
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Archivo adjunto a una ausencia")
public class ArchivoAusencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del archivo")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ausencia_id", nullable = false)
    @Schema(description = "Ausencia a la que pertenece el archivo")
    private Ausencia ausencia;

    @NotBlank(message = "El nombre del archivo es obligatorio")
    @Size(max = 255, message = "El nombre del archivo no puede tener más de 255 caracteres")
    @Column(name = "nombre_archivo", nullable = false)
    @Schema(description = "Nombre original del archivo", example = "ejercicios_matematicas.pdf")
    private String nombreArchivo;

    @NotBlank(message = "La ruta del archivo es obligatoria")
    @Size(max = 500, message = "La ruta del archivo no puede tener más de 500 caracteres")
    @Column(name = "ruta_archivo", nullable = false, length = 500)
    @Schema(description = "Ruta donde se almacena el archivo", example = "/uploads/ausencias/2025/08/archivo123.pdf")
    private String rutaArchivo;

    @Size(max = 100, message = "El tipo de archivo no puede tener más de 100 caracteres")
    @Column(name = "tipo_archivo", length = 100)
    @Schema(description = "Tipo MIME del archivo", example = "application/pdf")
    private String tipoArchivo;

    @Min(value = 0, message = "El tamaño del archivo no puede ser negativo")
    @Column(name = "tamano_archivo")
    @Schema(description = "Tamaño del archivo en bytes", example = "1048576")
    private Long tamanoArchivo;

    @Column(name = "fecha_subida", nullable = false)
    @Schema(description = "Fecha y hora de subida del archivo")
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        if (fechaSubida == null) {
            fechaSubida = LocalDateTime.now();
        }
    }

    /**
     * Obtiene el tamaño del archivo en formato legible
     */
    public String getTamanoFormateado() {
        if (tamanoArchivo == null) return "0 B";
        
        double bytes = tamanoArchivo.doubleValue();
        String[] unidades = {"B", "KB", "MB", "GB"};
        int unidadIndex = 0;
        
        while (bytes >= 1024 && unidadIndex < unidades.length - 1) {
            bytes /= 1024;
            unidadIndex++;
        }
        
        return String.format("%.1f %s", bytes, unidades[unidadIndex]);
    }
}
