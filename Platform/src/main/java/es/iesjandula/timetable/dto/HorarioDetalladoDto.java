package es.iesjandula.timetable.dto;

import java.util.List;
import java.util.Map;

public class HorarioDetalladoDto {
    public Long profesorId;
    public String nombre;
    public Map<String, List<EntradaHorarioDto>> horario;

    public static class EntradaHorarioDto {
        public int hora;
        public String tipo;
        public String asignatura;
        public String grupo;
        public String aula;
    }
}
