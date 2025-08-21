package com.FranGarcia.NuevaVersionGuardias.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AusenciaConGuardiasDTO {
    private Long id;
    private String profesorAusenteEmail;
    private LocalDate fecha;
    private String grupo;
    private String aula;
    private Integer hora;
    private String tarea;
    private String profesorEnGuardiaEmail;
}

