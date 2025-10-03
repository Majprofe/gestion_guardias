package es.iesjandula.timetable.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un grupo de alumnos")
public class Grupo {

    @Id
    @Schema(description = "ID único del grupo")
    private Long id;

    @Schema(description = "Abreviatura del grupo", example = "1DAW")
    private String abreviatura;

    @Schema(description = "Nombre completo del grupo", example = "1º Desarrollo de Aplicaciones Web")
    private String nombre;

    @Schema(description = "Indica si el grupo es considerado problemático", example = "false")
    private boolean esProblematico;

    @OneToOne(mappedBy = "tutoria")
    @JsonIgnore
    @Schema(description = "Profesor tutor del grupo")
    private Profesor profesor;
}
