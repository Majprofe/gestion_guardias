package es.iesjandula.guardias.models;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entidad que representa un archivo asociado a una hora específica de ausencia.
 * Permite que cada hora de ausencia tenga material didáctico específico (PDFs).
 */
@Entity
@Table(name = "archivo_hora_ausencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArchivoHoraAusencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Relación Many-to-One con HoraAusencia
     * Varios archivos pueden estar asociados a una misma hora de ausencia
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hora_ausencia_id", nullable = false)
    @JsonBackReference
    @NotNull(message = "La hora de ausencia es obligatoria")
    private HoraAusencia horaAusencia;

    /**
     * Nombre original del archivo subido por el usuario
     */
    @Column(nullable = false, length = 255)
    @NotNull(message = "El nombre del archivo es obligatorio")
    private String nombreArchivo;

    /**
     * Nombre único del archivo almacenado en el sistema de archivos
     * (incluye timestamp para evitar colisiones)
     */
    @Column(nullable = false, length = 255)
    @NotNull(message = "La ruta del archivo es obligatoria")
    private String nombreAlmacenado;

    /**
     * Ruta relativa del archivo en el sistema de archivos
     * Ejemplo: "2025/10/15/ausencia_123_hora_1_1696512345678_documento.pdf"
     */
    @Column(nullable = false, length = 500)
    @NotNull(message = "La ruta del archivo es obligatoria")
    private String rutaArchivo;

    /**
     * Tipo MIME del archivo (debe ser application/pdf)
     */
    @Column(nullable = false, length = 100)
    @NotNull(message = "El tipo de archivo es obligatorio")
    private String tipoMime;

    /**
     * Tamaño del archivo en bytes
     */
    @Column(nullable = false)
    @NotNull(message = "El tamaño del archivo es obligatorio")
    private Long tamanio;

    /**
     * Fecha y hora de subida del archivo
     */
    @Column(nullable = false)
    private LocalDateTime fechaSubida;

    @PrePersist
    protected void onCreate() {
        fechaSubida = LocalDateTime.now();
    }

    /**
     * Constructor de conveniencia para crear un nuevo archivo
     */
    public ArchivoHoraAusencia(HoraAusencia horaAusencia, String nombreArchivo, 
                               String nombreAlmacenado, String rutaArchivo, 
                               String tipoMime, Long tamanio) {
        this.horaAusencia = horaAusencia;
        this.nombreArchivo = nombreArchivo;
        this.nombreAlmacenado = nombreAlmacenado;
        this.rutaArchivo = rutaArchivo;
        this.tipoMime = tipoMime;
        this.tamanio = tamanio;
        this.fechaSubida = LocalDateTime.now();
    }
}
