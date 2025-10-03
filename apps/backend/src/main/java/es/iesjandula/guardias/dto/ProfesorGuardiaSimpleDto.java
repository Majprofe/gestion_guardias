package es.iesjandula.guardias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO simplificado para profesores de guardia
 * Usado en la asignación de coberturas
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información básica de un profesor de guardia")
public class ProfesorGuardiaSimpleDto {
    
    @Schema(description = "Email del profesor", example = "profesor@instituto.edu")
    private String email;
    
    @Schema(description = "Nombre completo del profesor", example = "Juan Pérez García")
    private String nombre;
    
    @Schema(description = "Abreviatura del profesor", example = "JPG")
    private String abreviatura;
}
