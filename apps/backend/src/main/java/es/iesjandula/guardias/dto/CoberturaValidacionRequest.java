package es.iesjandula.guardias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para la validación de una cobertura por el administrador
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request para validar una cobertura")
public class CoberturaValidacionRequest {
    
    @Schema(description = "Email del administrador que valida", example = "admin@instituto.edu", required = true)
    private String adminEmail;
    
    @Schema(description = "Observaciones opcionales", example = "Cobertura realizada correctamente")
    private String observaciones;
}
