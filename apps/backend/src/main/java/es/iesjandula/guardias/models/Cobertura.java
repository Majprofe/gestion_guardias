package es.iesjandula.guardias.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Entidad que representa la cobertura de una ausencia")
public class Cobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "ID único de la cobertura")
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "hora_ausencia_id", nullable = false, unique = true)
    @Schema(description = "Hora de ausencia que cubre esta cobertura")
    private HoraAusencia horaAusencia;

    @Schema(description = "Email del profesor que cubre la ausencia", example = "profesor.guardia@instituto.edu")
    private String profesorCubreEmail;

    @Schema(description = "Grupo que se cubre", example = "1DAW")
    private String grupo;

    @Schema(description = "Aula donde se realiza la cobertura", example = "A101")
    private String aula;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_guardia")
    @Schema(description = "Tipo de guardia realizada", example = "NORMAL")
    private TipoGuardia tipoGuardia = TipoGuardia.NORMAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @Schema(description = "Estado de la cobertura", example = "ASIGNADA")
    private EstadoCobertura estado = EstadoCobertura.ASIGNADA;

    @Column(name = "validada_por_admin")
    @Schema(description = "Indica si fue validada por el administrador")
    private Boolean validadaPorAdmin = false;

    @Column(name = "fecha_validacion")
    @Schema(description = "Fecha y hora de validación por el administrador")
    private java.time.LocalDateTime fechaValidacion;

    @Column(name = "admin_validador_email", length = 100)
    @Schema(description = "Email del administrador que validó", example = "admin@instituto.edu")
    private String adminValidadorEmail;

}
