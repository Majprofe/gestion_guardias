package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Entidad que representa un profesor del instituto")
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
        this.guardiasConvivencia = 0;
        this.tutoria = tutoria;
    }

    @Id
    @Schema(description = "ID único del profesor")
    private Long id;

    @Schema(description = "Nombre completo del profesor", example = "Juan Pérez García")
    private String nombre;

    @Schema(description = "Abreviatura del profesor", example = "JPG")
    private String abreviatura;

    @Schema(description = "Departamento al que pertenece", example = "Informática")
    private String departamento;

    @Schema(description = "Email del profesor", example = "juan.perez@instituto.edu")
    private String email;

    @Column(name = "guardias_realizadas")
    @Schema(description = "Total de guardias realizadas", example = "15")
    private Integer guardiasRealizadas = 0;

    @Column(name = "guardias_problematicas")
    @Schema(description = "Total de guardias problemáticas realizadas", example = "3")
    private Integer guardiasProblematicas = 0;

    @Column(name = "guardias_convivencia")
    @Schema(description = "Total de guardias de convivencia realizadas", example = "2")
    private Integer guardiasConvivencia = 0;

    @OneToOne
    @JoinColumn(name = "grupo_id")
    @Schema(description = "Grupo del que es tutor")
    private Grupo tutoria;

    /**
     * Incrementa los contadores de guardias según el tipo
     */
    public void incrementarGuardias(String tipoGuardia) {
        this.guardiasRealizadas++;
        
        switch (tipoGuardia.toUpperCase()) {
            case "PROBLEMATICA" -> this.guardiasProblematicas++;
            case "CONVIVENCIA" -> this.guardiasConvivencia++;
            // NORMAL no necesita contador adicional, solo el general
        }
    }

    /**
     * Calcula el total de guardias especiales (problemáticas + convivencia)
     */
    public Integer getGuardiasEspeciales() {
        return guardiasProblematicas + guardiasConvivencia;
    }
}
