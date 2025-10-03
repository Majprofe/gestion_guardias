package es.iesjandula.guardias.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class CoberturaDTO {
    private Long ausenciaId;
    private String profesorCubreEmail;
    private String grupo;
    private String aula;
    private Integer hora;
    private LocalDate fecha;
    private String profesorAusenteEmail;
    private String tarea;

}
