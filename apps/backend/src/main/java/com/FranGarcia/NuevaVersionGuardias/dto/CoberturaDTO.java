package com.FranGarcia.NuevaVersionGuardias.dto;

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




    /* CODIGO ANTIGUO
    private Long id;
    private LocalDate fecha;
    private String hora;
    private String profesorCubreEmail;
    private String profesorAusenteEmail;


    public CoberturaDTO() {}

    public CoberturaDTO(Long id, LocalDate fecha, String hora, String profesorCubreEmail, String profesorAusenteEmail) {
        this.id = id;
        this.fecha = fecha;
        this.hora = hora;
        this.profesorCubreEmail = profesorCubreEmail;
        this.profesorAusenteEmail = profesorAusenteEmail;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getProfesorCubreEmail() {
        return profesorCubreEmail;
    }

    public void setProfesorCubreEmail(String profesorCubreEmail) {
        this.profesorCubreEmail = profesorCubreEmail;
    }

    public String getProfesorAusenteEmail() {
        return profesorAusenteEmail;
    }

    public void setProfesorAusenteEmail(String profesorAusenteEmail) {
        this.profesorAusenteEmail = profesorAusenteEmail;
    }

     */
