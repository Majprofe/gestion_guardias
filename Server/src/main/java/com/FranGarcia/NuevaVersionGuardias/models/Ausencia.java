package com.FranGarcia.NuevaVersionGuardias.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
    Una ausencia correspondera a una hora, con profesor que cubra
 */

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ausencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String profesorAusenteEmail;

    @Column(nullable = false)
    private LocalDate fecha;

    private String grupo;
    private String aula;

    @Column(nullable = false)
    private Integer hora;

    private String tarea;

    @OneToOne(mappedBy = "ausencia", cascade = CascadeType.ALL)
    private Cobertura cobertura;
}




