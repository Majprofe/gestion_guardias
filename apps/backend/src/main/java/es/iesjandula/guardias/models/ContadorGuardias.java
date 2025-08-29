package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entidad que mantiene el contador de guardias realizadas por cada profesor
 * desglosado por día de la semana y hora
 */
@Entity
@Table(name = "contador_guardias",
       indexes = {
           @Index(name = "idx_contador_profesor", columnList = "profesorEmail"),
           @Index(name = "idx_contador_dia_hora", columnList = "diaSemana, hora")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_contador_profesor_dia_hora", 
                           columnNames = {"profesorEmail", "diaSemana", "hora"})
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Contador de guardias realizadas por profesor, día y hora")
public class ContadorGuardias {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del contador")
    private Long id;

    @NotBlank(message = "El email del profesor es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Column(name = "profesorEmail", nullable = false, length = 100)
    @Schema(description = "Email del profesor", example = "profesor@instituto.edu")
    private String profesorEmail;

    @NotNull(message = "El día de la semana es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "diaSemana", nullable = false)
    @Schema(description = "Día de la semana", example = "LUNES")
    private DiaSemana diaSemana;

    @NotNull(message = "La hora es obligatoria")
    @Min(value = 1, message = "La hora debe ser mayor a 0")
    @Max(value = 8, message = "La hora debe ser menor o igual a 8")
    @Column(nullable = false)
    @Schema(description = "Hora del día (1-8)", example = "3")
    private Integer hora;

    @Min(value = 0, message = "Las guardias normales no pueden ser negativas")
    @Column(name = "guardias_normales", nullable = false)
    @Schema(description = "Número de guardias normales realizadas", example = "5")
    private Integer guardiasNormales = 0;

    @Min(value = 0, message = "Las guardias problemáticas no pueden ser negativas")
    @Column(name = "guardias_problematicas", nullable = false)
    @Schema(description = "Número de guardias problemáticas realizadas", example = "2")
    private Integer guardiasProblematicas = 0;

    @Min(value = 0, message = "Las guardias de convivencia no pueden ser negativas")
    @Column(name = "guardias_convivencia", nullable = false)
    @Schema(description = "Número de guardias de convivencia realizadas", example = "1")
    private Integer guardiasConvivencia = 0;

    /**
     * Calcula el total de guardias realizadas
     */
    @Schema(description = "Total de guardias realizadas", example = "8")
    public Integer getTotalGuardias() {
        return guardiasNormales + guardiasProblematicas + guardiasConvivencia;
    }

    /**
     * Incrementa el contador según el tipo de guardia
     */
    public void incrementarContador(TipoGuardia tipoGuardia) {
        switch (tipoGuardia) {
            case NORMAL -> guardiasNormales++;
            case PROBLEMATICA -> guardiasProblematicas++;
            case CONVIVENCIA -> guardiasConvivencia++;
        }
    }
}
