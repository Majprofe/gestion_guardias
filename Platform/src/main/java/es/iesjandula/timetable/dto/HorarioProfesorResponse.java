package es.iesjandula.timetable.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioProfesorResponse {
    private Long profesorId;
    private String profesorNombre;
    private Map<String, List<ActividadDto>> horario;
}
