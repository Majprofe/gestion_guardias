package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignatura {

    @Id
    private Long id;

    private String abreviatura;

    private String nombre;

    private String nivel;

    private String curso;
}
