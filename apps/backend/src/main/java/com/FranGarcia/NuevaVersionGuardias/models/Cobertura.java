package com.FranGarcia.NuevaVersionGuardias.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Cobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "ausencia_id", nullable = false, unique = true)
    private Ausencia ausencia;

    private String profesorCubreEmail;

    private String grupo;

    private String aula;

}
