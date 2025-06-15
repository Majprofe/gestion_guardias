package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorGuardiaDto {
    private Long id;
    private String nombre;
    private String email;
    private String abreviatura;
    private String departamento;
    private Integer guardiasRealizadas;
    private Integer guardiasProblematicas;
}
