package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class HorarioDiaResponse {
    private Long idProfesor;
    private String nombreProfesor;
    private String diaSemana;
    private List<ActividadDto> actividades;
}
