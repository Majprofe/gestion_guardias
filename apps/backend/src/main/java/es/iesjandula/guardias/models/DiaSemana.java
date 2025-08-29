package es.iesjandula.guardias.models;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum que representa los días de la semana lectivos
 */
@Schema(description = "Días de la semana lectivos")
public enum DiaSemana {
    
    @Schema(description = "Lunes")
    LUNES("Lunes", 1),
    
    @Schema(description = "Martes")
    MARTES("Martes", 2),
    
    @Schema(description = "Miércoles")
    MIERCOLES("Miércoles", 3),
    
    @Schema(description = "Jueves")
    JUEVES("Jueves", 4),
    
    @Schema(description = "Viernes")
    VIERNES("Viernes", 5);

    private final String nombre;
    private final int numero;

    DiaSemana(String nombre, int numero) {
        this.nombre = nombre;
        this.numero = numero;
    }

    public String getNombre() {
        return nombre;
    }

    public int getNumero() {
        return numero;
    }

    /**
     * Obtiene el día de la semana a partir de su número
     */
    public static DiaSemana fromNumero(int numero) {
        for (DiaSemana dia : values()) {
            if (dia.numero == numero) {
                return dia;
            }
        }
        throw new IllegalArgumentException("Número de día no válido: " + numero);
    }

    /**
     * Obtiene el día de la semana a partir de un LocalDate
     */
    public static DiaSemana fromLocalDate(java.time.LocalDate fecha) {
        java.time.DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        
        return switch (dayOfWeek) {
            case MONDAY -> LUNES;
            case TUESDAY -> MARTES;
            case WEDNESDAY -> MIERCOLES;
            case THURSDAY -> JUEVES;
            case FRIDAY -> VIERNES;
            default -> throw new IllegalArgumentException(
                "El día " + dayOfWeek + " no es un día lectivo válido");
        };
    }
}
