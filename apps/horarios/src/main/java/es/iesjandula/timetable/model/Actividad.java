package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Profesor profesor;

    @ManyToOne
    private TramoHorario tramo;

    @ManyToOne
    private Asignatura asignatura;

    @ManyToOne
    private Aula aula;

    @Enumerated(EnumType.STRING)
    private TipoActividad tipo;

    @ManyToMany
    private List<Grupo> grupos;
}
