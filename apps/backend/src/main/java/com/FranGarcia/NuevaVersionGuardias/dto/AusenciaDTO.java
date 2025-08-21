package com.FranGarcia.NuevaVersionGuardias.dto;

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


    /* CODIGO ANTIGUO
    private Long id;
    private LocalDate fecha;
    private String profesorAusenteEmail;
    private Integer hora;
    private String tarea;
    private String profesorCubreNombre;

    public AusenciaDTO() {}

    public AusenciaDTO(Long id, LocalDate fecha, String profesorAusenteEmail, Integer hora, String tarea, String profesorCubreNombre) {
        this.id = id;
        this.fecha = fecha;
        this.profesorAusenteEmail = profesorAusenteEmail;
        this.hora = hora;
        this.tarea = tarea;
        this.profesorCubreNombre = profesorCubreNombre;
    }

    //Constructor sin ID, cuando no nos interesa almacenar su id por ejemplo en el tema de visualizaciones
    public AusenciaDTO(LocalDate fecha, String profesorAusenteEmail, Integer hora, String tarea) {
        this.fecha = fecha;
        this.profesorAusenteEmail = profesorAusenteEmail;
        this.hora = hora;
        this.tarea = tarea;
    }

    public Long getId() {
        return id;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public String getProfesorAusenteEmail() {
        return profesorAusenteEmail;
    }

    public void setProfesorAusenteEmail(String profesorAusenteEmail) {
        this.profesorAusenteEmail = profesorAusenteEmail;
    }

    public Integer getHora() {
        return hora;
    }

    public void setHora(Integer hora) {
        this.hora = hora;
    }

    public String getTarea() {
        return tarea;
    }

    public void setTarea(String tarea) {
        this.tarea = tarea;
    }



     */


