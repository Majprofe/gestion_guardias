package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profesor {
    
    // Constructor personalizado que garantiza que los contadores se inicialicen en 0
    public Profesor(Long id, String nombre, String abreviatura, String departamento, String email, Grupo tutoria) {
        this.id = id;
        this.nombre = nombre;
        this.abreviatura = abreviatura;
        this.departamento = departamento;
        this.email = email;
        this.guardiasRealizadas = 0;
        this.guardiasProblematicas = 0;
        this.tutoria = tutoria;
    }

    @Id
    private Long id;

    private String nombre;

    private String abreviatura;

    private String departamento;

    private String email;

    private Integer guardiasRealizadas = 0;

    private Integer guardiasProblematicas = 0;

    @OneToOne
    @JoinColumn(name = "grupo_id")
    private Grupo tutoria;
}
