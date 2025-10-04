package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonBackReference;

/**
 * Entidad que representa una hora específica dentro de una ausencia.
 * Una ausencia puede tener múltiples horas.
 */
@Entity
@Table(name = "horas_ausencia",
       indexes = {
           @Index(name = "idx_hora_ausencia", columnList = "ausencia_id, hora")
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Hora específica dentro de una ausencia")
public class HoraAusencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la hora de ausencia", example = "1")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ausencia_id", nullable = false)
    @JsonBackReference
    @Schema(description = "Ausencia a la que pertenece esta hora")
    private Ausencia ausencia;

    @NotNull(message = "La hora es obligatoria")
    @Min(value = 1, message = "La hora debe ser mayor a 0")
    @Max(value = 8, message = "La hora debe ser menor o igual a 8")
    @Column(nullable = false)
    @Schema(description = "Hora de la ausencia (1-6, sin recreo)", example = "3")
    private Integer hora;

    @NotBlank(message = "El grupo es obligatorio")
    @Size(max = 50, message = "El grupo no puede tener más de 50 caracteres")
    @Column(length = 50, nullable = false)
    @Schema(description = "Grupo afectado por la ausencia", example = "1DAW")
    private String grupo;
    
    @NotBlank(message = "El aula es obligatoria")
    @Size(max = 20, message = "El aula no puede tener más de 20 caracteres")
    @Column(length = 20, nullable = false)
    @Schema(description = "Aula donde se produce la ausencia", example = "A101")
    private String aula;

    @Size(max = 1000, message = "La tarea no puede tener más de 1000 caracteres")
    @Column(length = 1000)
    @Schema(description = "Tarea asignada durante esta hora de ausencia", example = "Ejercicios página 25-30")
    private String tarea;

    @OneToOne(mappedBy = "horaAusencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Cobertura asignada para esta hora")
    private Cobertura cobertura;

    @OneToMany(mappedBy = "horaAusencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Archivos asociados a esta hora de ausencia (material didáctico)")
    private java.util.List<ArchivoHoraAusencia> archivos = new java.util.ArrayList<>();

    /**
     * Helper method para añadir un archivo a esta hora de ausencia
     */
    public void addArchivo(ArchivoHoraAusencia archivo) {
        archivos.add(archivo);
        archivo.setHoraAusencia(this);
    }

    /**
     * Helper method para eliminar un archivo de esta hora de ausencia
     */
    public void removeArchivo(ArchivoHoraAusencia archivo) {
        archivos.remove(archivo);
        archivo.setHoraAusencia(null);
    }
}
