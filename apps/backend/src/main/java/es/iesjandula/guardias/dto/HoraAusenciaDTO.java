package es.iesjandula.guardias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una hora específica dentro de una ausencia
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Información de una hora específica de ausencia")
public class HoraAusenciaDTO {

    @NotNull(message = "La hora es obligatoria")
    @Min(value = 1, message = "La hora debe ser mayor a 0")
    @Max(value = 8, message = "La hora debe ser menor o igual a 8")
    @Schema(description = "Número de hora (1-8)", example = "3", required = true)
    private Integer hora;

    @NotBlank(message = "El grupo es obligatorio")
    @Size(max = 50, message = "El grupo no puede tener más de 50 caracteres")
    @Schema(description = "Grupo afectado", example = "1DAW", required = true)
    private String grupo;

    @NotBlank(message = "El aula es obligatoria")
    @Size(max = 20, message = "El aula no puede tener más de 20 caracteres")
    @Schema(description = "Aula donde se imparte", example = "2.2", required = true)
    private String aula;

    @Size(max = 1000, message = "La tarea no puede tener más de 1000 caracteres")
    @Schema(description = "Tarea para los alumnos durante esta hora", example = "Realizar ejercicios 1-10 de la página 45")
    private String tarea;
}
