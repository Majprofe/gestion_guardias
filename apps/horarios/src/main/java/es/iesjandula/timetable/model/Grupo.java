package es.iesjandula.timetable.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Grupo {

    @Id
    private Long id;

    private String abreviatura;

    private String nombre;

    private boolean esProblematico;

    @OneToOne(mappedBy = "tutoria")
    @JsonIgnore
    private Profesor profesor;
}
