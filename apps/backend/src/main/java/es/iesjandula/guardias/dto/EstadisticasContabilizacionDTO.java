package es.iesjandula.guardias.dto;

import java.time.LocalDate;

/**
 * DTO para estadísticas de contabilización de guardias
 */
public class EstadisticasContabilizacionDTO {
    
    private LocalDate fecha;
    private long coberturasValidadas;
    private long coberturasAsignadas;
    private long coberturasCanceladas;
    
    public EstadisticasContabilizacionDTO() {}
    
    public EstadisticasContabilizacionDTO(LocalDate fecha, long validadas, long asignadas, long canceladas) {
        this.fecha = fecha;
        this.coberturasValidadas = validadas;
        this.coberturasAsignadas = asignadas;
        this.coberturasCanceladas = canceladas;
    }
    
    // Getters y Setters
    public LocalDate getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }
    
    public long getCoberturasValidadas() {
        return coberturasValidadas;
    }
    
    public void setCoberturasValidadas(long coberturasValidadas) {
        this.coberturasValidadas = coberturasValidadas;
    }
    
    public long getCoberturasAsignadas() {
        return coberturasAsignadas;
    }
    
    public void setCoberturasAsignadas(long coberturasAsignadas) {
        this.coberturasAsignadas = coberturasAsignadas;
    }
    
    public long getCoberturasCanceladas() {
        return coberturasCanceladas;
    }
    
    public void setCoberturasCanceladas(long coberturasCanceladas) {
        this.coberturasCanceladas = coberturasCanceladas;
    }
}
