package es.iesjandula.guardias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "DTO para crear múltiples ausencias en un día")
public class CrearAusenciaMultipleDTO {
    
    @NotBlank(message = "El email del profesor ausente es obligatorio")
    @Email(message = "El email del profesor ausente debe tener un formato válido")
    @Schema(description = "Email del profesor ausente", example = "profesor@instituto.edu", required = true)
    private String profesorAusenteEmail;
    
    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o en el futuro")
    @Schema(description = "Fecha de la ausencia", example = "2025-08-21", required = true)
    private LocalDate fecha;
    
    @NotEmpty(message = "Debe especificar al menos una hora")
    @Schema(description = "Lista de horas y tareas afectadas", required = true)
    private List<HoraAusenciaDTO> horas;
    
    @Data
    @Schema(description = "Información de cada hora de ausencia")
    public static class HoraAusenciaDTO {
        
        @NotNull(message = "La hora es obligatoria")
        @Min(value = 1, message = "La hora debe ser mayor a 0")
        @Max(value = 8, message = "La hora debe ser menor o igual a 8")
        @Schema(description = "Hora de la ausencia (1-8)", example = "3", required = true)
        private Integer hora;
        
        @NotBlank(message = "El grupo es obligatorio")
        @Size(max = 10, message = "El grupo no puede tener más de 10 caracteres")
        @Schema(description = "Grupo al que afecta la ausencia", example = "1DAW", required = true)
        private String grupo;
        
        @NotBlank(message = "El aula es obligatoria")
        @Size(max = 20, message = "El aula no puede tener más de 20 caracteres")
        @Schema(description = "Aula donde se produce la ausencia", example = "A101", required = true)
        private String aula;
        
        @Size(max = 500, message = "La tarea no puede tener más de 500 caracteres")
        @Schema(description = "Tarea para el grupo durante la ausencia", example = "Ejercicios página 25-30")
        private String tarea;
    }
}
