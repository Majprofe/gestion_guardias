package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "DTO con información de un profesor de guardia")
public class ProfesorGuardiaDto {
    
    @Schema(description = "ID del profesor", example = "1")
    private Long id;
    
    @Schema(description = "Nombre completo del profesor", example = "Juan Pérez García")
    private String nombre;
    
    @Schema(description = "Email del profesor", example = "juan.perez@instituto.edu")
    private String email;
    
    @Schema(description = "Abreviatura del profesor", example = "JPG")
    private String abreviatura;
    
    @Schema(description = "Departamento al que pertenece", example = "Informática")
    private String departamento;
    
    @Schema(description = "Total de guardias realizadas (contador global)", example = "15")
    private Integer guardiasRealizadas;
    
    @Schema(description = "Total de guardias problemáticas realizadas (contador global)", example = "3")
    private Integer guardiasProblematicas;
    
    @Schema(description = "Total de guardias de convivencia realizadas (contador global)", example = "2")
    private Integer guardiasConvivencia;
    
    @Schema(description = "Contadores detallados por día y hora específicos", 
            example = "{'1-3': {'normales': 2, 'problematicas': 1, 'convivencia': 0}}")
    private Map<String, ContadorDetalle> contadoresPorDiaHora;

    /**
     * Constructor sin contadores detallados (para compatibilidad)
     */
    public ProfesorGuardiaDto(Long id, String nombre, String email, String abreviatura, 
                              String departamento, Integer guardiasRealizadas, Integer guardiasProblematicas) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.abreviatura = abreviatura;
        this.departamento = departamento;
        this.guardiasRealizadas = guardiasRealizadas;
        this.guardiasProblematicas = guardiasProblematicas;
        this.guardiasConvivencia = 0; // Valor por defecto
    }

    /**
     * Clase interna para contadores detallados
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de contadores por tipo de guardia")
    public static class ContadorDetalle {
        @Schema(description = "Guardias normales", example = "2")
        private Integer normales;
        
        @Schema(description = "Guardias problemáticas", example = "1")
        private Integer problematicas;
        
        @Schema(description = "Guardias de convivencia", example = "0")
        private Integer convivencia;
        
        @Schema(description = "Total de guardias", example = "3")
        public Integer getTotal() {
            return (normales != null ? normales : 0) + 
                   (problematicas != null ? problematicas : 0) + 
                   (convivencia != null ? convivencia : 0);
        }
    }
}
