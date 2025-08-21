package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TramoHorario {

    @Id
    private Long id;

    private int diaSemana;
    private int horaDia;

    private LocalTime horaInicio;
    private LocalTime horaFin;
}
