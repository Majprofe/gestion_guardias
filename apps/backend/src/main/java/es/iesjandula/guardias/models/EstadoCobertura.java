package es.iesjandula.guardias.models;

/**
 * Enum que representa los estados posibles de una cobertura
 */
public enum EstadoCobertura {
    
    /**
     * Cobertura asignada automáticamente por el sistema.
     * Puede cambiar si hay nuevas ausencias que requieren redistribución.
     */
    ASIGNADA("Asignada automáticamente"),
    
    /**
     * Cobertura validada por el administrador.
     * Se garantiza que esta cobertura se realizó realmente.
     */
    VALIDADA("Validada por administrador"),
    
    /**
     * Cobertura cancelada.
     * No se realizó finalmente, por lo que no debe contarse en estadísticas.
     */
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoCobertura(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Determina si esta cobertura debe contarse en las estadísticas
     */
    public boolean debeContarse() {
        return this == VALIDADA;
    }

    /**
     * Determina si esta cobertura puede ser redistribuida automáticamente
     */
    public boolean puedeReasignarse() {
        return this == ASIGNADA;
    }
}
