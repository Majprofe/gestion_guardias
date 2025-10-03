package es.iesjandula.guardias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para crear una ausencia de un día completo con múltiples horas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "DTO para crear una ausencia con múltiples horas")
public class CrearAusenciaDTO {

    @NotBlank(message = "El email del profesor ausente es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Schema(description = "Email del profesor que falta", example = "profesor@instituto.edu", required = true)
    private String profesorAusenteEmail;

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o en el futuro")
    @Schema(description = "Fecha de la ausencia", example = "2025-10-09", required = true)
    private LocalDate fecha;

    @NotNull(message = "Debe especificar al menos una hora de ausencia")
    @Size(min = 1, message = "Debe haber al menos una hora de ausencia")
    @Valid
    @Schema(description = "Lista de horas con sus respectivas tareas", required = true)
    private List<HoraAusenciaDTO> horas;
}
