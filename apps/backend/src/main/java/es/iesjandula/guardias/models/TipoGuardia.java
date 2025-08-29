package es.iesjandula.guardias.models;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que representa los diferentes tipos de guardias que puede realizar un profesor
 */
@Schema(description = "Tipos de guardia disponibles")
public enum TipoGuardia {
    
    @Schema(description = "Guardia normal - cobertura estándar de ausencia")
    NORMAL("Guardia Normal"),
    
    @Schema(description = "Guardia problemática - cobertura de grupo problemático")
    PROBLEMATICA("Guardia Problemática"),
    
    @Schema(description = "Guardia de convivencia - supervisión del aula de convivencia")
    CONVIVENCIA("Aula de Convivencia");

    private final String descripcion;

    TipoGuardia(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
