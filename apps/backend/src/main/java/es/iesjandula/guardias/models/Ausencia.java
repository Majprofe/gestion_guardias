package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * Entidad que representa una ausencia de un profesor.
 * Una ausencia corresponde a una hora específica con su correspondiente cobertura.
 */
@Entity
@Table(name = "ausencias", 
       indexes = {
           @Index(name = "idx_ausencia_profesor_fecha", columnList = "profesorAusenteEmail, fecha"),
           @Index(name = "idx_ausencia_fecha_hora", columnList = "fecha, hora")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_ausencia_profesor_fecha_hora", 
                           columnNames = {"profesorAusenteEmail", "fecha", "hora"})
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa una ausencia de un profesor")
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la ausencia", example = "1")
    private Long id;

    @NotBlank(message = "El email del profesor ausente es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(name = "profesorAusenteEmail", nullable = false, length = 100)
    @Schema(description = "Email del profesor ausente", example = "profesor@instituto.edu")
    private String profesorAusenteEmail;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    @Schema(description = "Fecha de la ausencia", example = "2025-08-21")
    private LocalDate fecha;

    @NotBlank(message = "El grupo es obligatorio")
    @Size(max = 10, message = "El grupo no puede tener más de 10 caracteres")
    @Column(length = 10)
    @Schema(description = "Grupo afectado por la ausencia", example = "1DAW")
    private String grupo;
    
    @NotBlank(message = "El aula es obligatoria")
    @Size(max = 20, message = "El aula no puede tener más de 20 caracteres")
    @Column(length = 20)
    @Schema(description = "Aula donde se produce la ausencia", example = "A101")
    private String aula;

    @NotNull(message = "La hora es obligatoria")
    @Min(value = 1, message = "La hora debe ser mayor a 0")
    @Max(value = 8, message = "La hora debe ser menor o igual a 8")
    @Column(nullable = false)
    @Schema(description = "Hora de la ausencia (1-8)", example = "3")
    private Integer hora;

    @Size(max = 500, message = "La tarea no puede tener más de 500 caracteres")
    @Column(length = 500)
    @Schema(description = "Tarea asignada durante la ausencia", example = "Ejercicios página 25-30")
    private String tarea;

    @Column(columnDefinition = "TEXT")
    @Schema(description = "Tareas obligatorias detalladas para los alumnos durante la ausencia")
    private String tareasObligatorias;

    @OneToOne(mappedBy = "ausencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Cobertura asociada a esta ausencia")
    private Cobertura cobertura;

    @OneToMany(mappedBy = "ausencia", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Schema(description = "Archivos adjuntos a esta ausencia")
    private List<ArchivoAusencia> archivos;
}
