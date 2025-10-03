package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Email;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO para actualizar contadores de guardias específicos")
public class ActualizarContadorDto {
    
    @NotNull(message = "El email del profesor es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Schema(description = "Email del profesor", example = "juan.perez@instituto.edu", required = true)
    private String email;
    
    @NotNull(message = "El día es obligatorio")
    @Min(value = 1, message = "El día debe ser entre 1 y 7")
    @Schema(description = "Día de la semana (1=Lunes, 7=Domingo)", example = "1", required = true)
    private Integer dia;
    
    @NotNull(message = "La hora es obligatoria")
    @Min(value = 1, message = "La hora debe ser mayor a 0")
    @Schema(description = "Hora del día (1-12 para horario escolar)", example = "3", required = true)
    private Integer hora;
    
    @Schema(description = "Cantidad de guardias normales", example = "2")
    private Integer guardiasNormales;
    
    @Schema(description = "Cantidad de guardias problemáticas", example = "1")
    private Integer guardiasProblematicas;
    
    @Schema(description = "Cantidad de guardias de convivencia", example = "0")
    private Integer guardiasConvivencia;
    
    @Schema(description = "Tipo de operación: 'SET' para establecer, 'INCREMENT' para incrementar", 
            example = "INCREMENT", allowableValues = {"SET", "INCREMENT"})
    private String operacion = "SET";
    
    /**
     * Valida que al menos un tipo de guardia esté especificado
     */
    public boolean tieneAlMenosUnContador() {
        return (guardiasNormales != null && guardiasNormales > 0) ||
               (guardiasProblematicas != null && guardiasProblematicas > 0) ||
               (guardiasConvivencia != null && guardiasConvivencia > 0);
    }
    
    /**
     * Obtiene el total de guardias especificadas
     */
    public int getTotalGuardias() {
        return (guardiasNormales != null ? guardiasNormales : 0) +
               (guardiasProblematicas != null ? guardiasProblematicas : 0) +
               (guardiasConvivencia != null ? guardiasConvivencia : 0);
    }
}
