package es.iesjandula.guardias.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Servicio de integración con el backend de horarios.
 * Permite consultar profesores de guardia y validar horarios.
 */
@Service
public class HorarioIntegrationService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioIntegrationService.class);
    
    private final WebClient webClient;
    private final String horariosBaseUrl;

    public HorarioIntegrationService(WebClient.Builder webClientBuilder,
                                   @Value("${horarios.service.url:http://localhost:8082}") String horariosBaseUrl) {
        this.webClient = webClientBuilder.baseUrl(horariosBaseUrl).build();
        this.horariosBaseUrl = horariosBaseUrl;
    }

    /**
     * Obtiene la lista de profesores de guardia para un día y hora específicos.
     * 
     * @param diaSemana Día de la semana (1=Lunes, ..., 5=Viernes)
     * @param hora Hora del día
     * @return Lista de profesores de guardia o vacía si hay error
     */
    @SuppressWarnings("unchecked")
    public Mono<List<Map<String, Object>>> getProfesoresGuardia(int diaSemana, int hora) {
        logger.debug("Consultando profesores de guardia para día {} hora {}", diaSemana, hora);
        
        return webClient.get()
                .uri("/horario/guardia/dia/{dia}/hora/{hora}", diaSemana, hora)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Object profesoresObj = response.get("profesores");
                    List<Map<String, Object>> profesores = (List<Map<String, Object>>) profesoresObj;
                    logger.info("Encontrados {} profesores de guardia para día {} hora {}", 
                              profesores != null ? profesores.size() : 0, diaSemana, hora);
                    return profesores != null ? profesores : List.<Map<String, Object>>of();
                })
                .doOnError(error -> 
                    logger.error("Error consultando profesores de guardia: {}", error.getMessage())
                )
                .onErrorReturn(List.<Map<String, Object>>of());
    }

    /**
     * Verifica si un profesor existe en el sistema de horarios.
     * 
     * @param email Email del profesor
     * @return true si existe, false si no existe o hay error
     */
    public Mono<Boolean> existeProfesor(String email) {
        logger.debug("Verificando existencia de profesor: {}", email);
        
        return webClient.get()
                .uri("/horario/profesor/email?email={email}", email)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    boolean existe = response != null && response.containsKey("id");
                    logger.debug("Profesor {} {}", email, existe ? "encontrado" : "no encontrado");
                    return existe;
                })
                .doOnError(error -> 
                    logger.warn("Error verificando profesor {}: {}", email, error.getMessage())
                )
                .onErrorReturn(false);
    }

    /**
     * Obtiene las clases que tiene un profesor en un día específico.
     * Útil para verificar conflictos de horario.
     * 
     * @param email Email del profesor
     * @param diaSemana Día de la semana
     * @return Lista de actividades del profesor
     */
    @SuppressWarnings("unchecked")
    public Mono<List<Map<String, Object>>> getClasesProfesor(String email, int diaSemana) {
        logger.debug("Consultando clases de profesor {} para día {}", email, diaSemana);
        
        return webClient.get()
                .uri("/horario/profesor/email/{email}/dia/{dia}/clases", email, diaSemana)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(response -> {
                    Object actividadesObj = response.get("actividades");
                    List<Map<String, Object>> actividades = (List<Map<String, Object>>) actividadesObj;
                    logger.info("Profesor {} tiene {} clases el día {}", 
                              email, actividades != null ? actividades.size() : 0, diaSemana);
                    return actividades != null ? actividades : List.<Map<String, Object>>of();
                })
                .doOnError(error -> 
                    logger.error("Error consultando clases de profesor {}: {}", email, error.getMessage())
                )
                .onErrorReturn(List.<Map<String, Object>>of());
    }
}
