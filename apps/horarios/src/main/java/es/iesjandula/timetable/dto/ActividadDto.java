package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ActividadDto {
    private int hora;
    private String tipo;
    private String asignatura;
    private String grupo;
    private String aula;
    private String abreviaturaAula;
}
