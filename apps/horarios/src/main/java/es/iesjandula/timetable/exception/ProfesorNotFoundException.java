package es.iesjandula.timetable.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción personalizada para cuando un profesor no es encontrado.
 * Proporciona información específica del error y código de estado HTTP.
 */
public class ProfesorNotFoundException extends RuntimeException {
    
    private final HttpStatus httpStatus;
    
    public ProfesorNotFoundException(String message) {
        super(message);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public ProfesorNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public ProfesorNotFoundException(Long profesorId) {
        super("No se encontró profesor con ID: " + profesorId);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public ProfesorNotFoundException(String campo, String valor) {
        super("No se encontró profesor con " + campo + ": " + valor);
        this.httpStatus = HttpStatus.NOT_FOUND;
    }
    
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
