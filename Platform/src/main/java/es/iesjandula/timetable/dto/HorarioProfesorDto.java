package es.iesjandula.timetable.dto;

import java.util.List;
import java.util.Map;

public class HorarioProfesorDto {
    public Long id;
    public String nombre;
    public Map<String, List<ActividadDto>> horario;
}
