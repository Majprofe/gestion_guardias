package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que representa una ausencia de un profesor en un día específico.
 * Una ausencia puede contener múltiples horas con sus respectivas tareas y coberturas.
 */
@Entity
@Table(name = "ausencias", 
       indexes = {
           @Index(name = "idx_ausencia_profesor_fecha", columnList = "profesorAusenteEmail, fecha")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_ausencia_profesor_fecha", 
                           columnNames = {"profesorAusenteEmail", "fecha"})
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa una ausencia de un profesor en un día específico")
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
    @Schema(description = "Fecha de la ausencia", example = "2025-10-09")
    private LocalDate fecha;

    @OneToMany(mappedBy = "ausencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference
    @Schema(description = "Horas específicas de la ausencia con sus tareas y coberturas")
    private List<HoraAusencia> horas = new ArrayList<>();

    @OneToMany(mappedBy = "ausencia", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Schema(description = "Archivos adjuntos a esta ausencia (comunes para todas las horas)")
    private List<ArchivoAusencia> archivos = new ArrayList<>();

    /**
     * Método helper para añadir una hora de ausencia
     */
    public void addHoraAusencia(HoraAusencia horaAusencia) {
        horas.add(horaAusencia);
        horaAusencia.setAusencia(this);
    }

    /**
     * Método helper para añadir un archivo
     */
    public void addArchivo(ArchivoAusencia archivo) {
        archivos.add(archivo);
        archivo.setAusencia(this);
    }
}
