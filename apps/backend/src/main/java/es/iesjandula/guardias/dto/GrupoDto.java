package es.iesjandula.guardias.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO para información de un grupo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Información de un grupo de alumnos")
public class GrupoDto {
    
    @Schema(description = "ID del grupo")
    private Long id;
    
    @Schema(description = "Abreviatura del grupo", example = "1DAW")
    private String abreviatura;
    
    @Schema(description = "Nombre completo del grupo", example = "1º Desarrollo de Aplicaciones Web")
    private String nombre;
    
    @Schema(description = "Indica si el grupo es problemático", example = "false")
    private boolean esProblematico;
}
