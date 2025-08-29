package es.iesjandula.guardias.dto;

import lombok.Data;

import java.time.LocalDate;


@Data
public class AusenciaDTO {
    private String profesorAusenteEmail;
    private LocalDate fecha;
    private String grupo;
    private String aula;
    private Integer hora;
    private String tarea;
}
