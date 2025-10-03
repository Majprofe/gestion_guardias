package es.iesjandula.timetable.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Entidad que mantiene el contador detallado de guardias realizadas por cada profesor
 * desglosado por día de la semana y hora específica
 */
@Entity
@Table(name = "contador_detallado",
       indexes = {
           @Index(name = "idx_contador_profesor", columnList = "profesor_id"),
           @Index(name = "idx_contador_dia_hora", columnList = "dia_semana, hora_dia")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_contador_profesor_dia_hora", 
                           columnNames = {"profesor_id", "dia_semana", "hora_dia"})
       })
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Contador detallado de guardias realizadas por profesor, día y hora")
public class ContadorDetallado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único del contador")
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "profesor_id", nullable = false)
    @Schema(description = "Profesor al que pertenece el contador")
    private Profesor profesor;

    @Column(name = "dia_semana", nullable = false)
    @Schema(description = "Día de la semana (1=Lunes, 2=Martes, ..., 5=Viernes)", example = "1")
    private Integer diaSemana;

    @Column(name = "hora_dia", nullable = false)
    @Schema(description = "Hora del día (1-8)", example = "3")
    private Integer horaDia;

    @Column(name = "guardias_normales", nullable = false)
    @Schema(description = "Número de guardias normales realizadas", example = "5")
    private Integer guardiasNormales = 0;

    @Column(name = "guardias_problematicas", nullable = false)
    @Schema(description = "Número de guardias problemáticas realizadas", example = "2")
    private Integer guardiasProblematicas = 0;

    @Column(name = "guardias_convivencia", nullable = false)
    @Schema(description = "Número de guardias de convivencia realizadas", example = "1")
    private Integer guardiasConvivencia = 0;

    /**
     * Constructor con parámetros básicos
     */
    public ContadorDetallado(Profesor profesor, Integer diaSemana, Integer horaDia) {
        this.profesor = profesor;
        this.diaSemana = diaSemana;
        this.horaDia = horaDia;
        this.guardiasNormales = 0;
        this.guardiasProblematicas = 0;
        this.guardiasConvivencia = 0;
    }

    /**
     * Calcula el total de guardias realizadas
     */
    @Schema(description = "Total de guardias realizadas", example = "8")
    public Integer getTotalGuardias() {
        return guardiasNormales + guardiasProblematicas + guardiasConvivencia;
    }

    /**
     * Incrementa el contador según el tipo de guardia
     */
    public void incrementarContador(String tipoGuardia) {
        switch (tipoGuardia.toUpperCase()) {
            case "NORMAL" -> guardiasNormales++;
            case "PROBLEMATICA" -> guardiasProblematicas++;
            case "CONVIVENCIA" -> guardiasConvivencia++;
        }
    }

    /**
     * Incrementa el contador según el tipo de guardia con cantidad específica
     */
    public void incrementarContador(String tipoGuardia, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) return;
        
        switch (tipoGuardia.toUpperCase()) {
            case "NORMAL", "NORMALES" -> guardiasNormales = (guardiasNormales != null ? guardiasNormales : 0) + cantidad;
            case "PROBLEMATICA", "PROBLEMATICAS" -> guardiasProblematicas = (guardiasProblematicas != null ? guardiasProblematicas : 0) + cantidad;
            case "CONVIVENCIA" -> guardiasConvivencia = (guardiasConvivencia != null ? guardiasConvivencia : 0) + cantidad;
        }
    }

    /**
     * Decrementa el contador según el tipo de guardia (para cancelaciones)
     */
    public void decrementarContador(String tipoGuardia) {
        switch (tipoGuardia.toUpperCase()) {
            case "NORMAL" -> {
                if (guardiasNormales > 0) guardiasNormales--;
            }
            case "PROBLEMATICA" -> {
                if (guardiasProblematicas > 0) guardiasProblematicas--;
            }
            case "CONVIVENCIA" -> {
                if (guardiasConvivencia > 0) guardiasConvivencia--;
            }
        }
    }

    /**
     * Decrementa el contador según el tipo de guardia con cantidad específica
     */
    public void decrementarContador(String tipoGuardia, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) return;
        
        switch (tipoGuardia.toUpperCase()) {
            case "NORMAL", "NORMALES" -> guardiasNormales = Math.max(0, (guardiasNormales != null ? guardiasNormales : 0) - cantidad);
            case "PROBLEMATICA", "PROBLEMATICAS" -> guardiasProblematicas = Math.max(0, (guardiasProblematicas != null ? guardiasProblematicas : 0) - cantidad);
            case "CONVIVENCIA" -> guardiasConvivencia = Math.max(0, (guardiasConvivencia != null ? guardiasConvivencia : 0) - cantidad);
        }
    }
}
