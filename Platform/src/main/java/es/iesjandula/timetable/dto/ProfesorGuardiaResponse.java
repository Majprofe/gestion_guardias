package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorGuardiaResponse {
    private int diaSemana;
    private int horaDia;
    private List<ProfesorGuardiaDto> profesores;
}
