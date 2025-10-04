package es.iesjandula.guardias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para representar una hora de ausencia con su cobertura asignada
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Hora de ausencia con información de cobertura")
public class HoraAusenciaConCoberturaDTO {

    @Schema(description = "ID de la hora de ausencia", example = "1")
    private Long id;

    @Schema(description = "Número de hora (1-6, sin recreo)", example = "3")
    private Integer hora;

    @Schema(description = "Grupo afectado", example = "1DAW")
    private String grupo;

    @Schema(description = "Aula", example = "2.2")
    private String aula;

    @Schema(description = "Tarea asignada", example = "Ejercicios 1-10")
    private String tarea;

    @Schema(description = "Cobertura asignada a esta hora (puede ser null si aún no se asignó)")
    private CoberturaDTO cobertura;

    @Schema(description = "Archivos asociados a esta hora (material didáctico)")
    private java.util.List<ArchivoDTO> archivos;
}
