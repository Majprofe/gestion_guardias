package es.iesjandula.guardias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para representar una ausencia con sus horas y coberturas asignadas
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Ausencia con toda su información incluyendo coberturas")
public class AusenciaResponseDTO {

    @Schema(description = "ID de la ausencia", example = "1")
    private Long id;

    @Schema(description = "Email del profesor ausente", example = "profesor@instituto.edu")
    private String profesorAusenteEmail;

    @Schema(description = "Nombre completo del profesor ausente", example = "Juan Pérez García")
    private String profesorAusenteNombre;

    @Schema(description = "Fecha de la ausencia", example = "2025-10-09")
    private LocalDate fecha;

    @Schema(description = "Horas de ausencia con sus coberturas asignadas")
    private List<HoraAusenciaConCoberturaDTO> horas;
}
