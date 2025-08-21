package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Aula {

    @Id
    private Long id;

    private String abreviatura;

    private String nombre;
}
