package com.FranGarcia.NuevaVersionGuardias.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;

/**
 * Filtro CORS inteligente que se adapta al perfil de ejecución.
 */
@Component
@Order(1)
public class CorsFilter implements Filter {

    private final Environment environment;
    
    @Value("${cors.allowed.origins:*}")
    private String allowedOrigins;
    
    @Value("${cors.allowed.credentials:false}")
    private boolean allowCredentials;

    public CorsFilter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Verificar si estamos en producción
        boolean isProduction = Arrays.asList(environment.getActiveProfiles()).contains("prod");
        
        if (isProduction) {
            // PRODUCCIÓN: Configuración restrictiva
            handleProductionCors(httpRequest, httpResponse);
        } else {
            // DESARROLLO: Configuración permisiva
            handleDevelopmentCors(httpResponse);
        }

        // Manejar peticiones OPTIONS (preflight)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            httpResponse.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        chain.doFilter(request, response);
    }
    
    private void handleProductionCors(HttpServletRequest request, HttpServletResponse response) {
        String origin = request.getHeader("Origin");
        
        // Verificar si el origin está permitido
        if (origin != null && isOriginAllowed(origin)) {
            response.setHeader("Access-Control-Allow-Origin", origin);
        }
        
        // Métodos más restrictivos
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        
        // Headers específicos
        response.setHeader("Access-Control-Allow-Headers", 
            "Authorization, Content-Type, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers");
        
        // Headers expuestos limitados
        response.setHeader("Access-Control-Expose-Headers", 
            "Access-Control-Allow-Origin, Access-Control-Allow-Credentials");
        
        response.setHeader("Access-Control-Allow-Credentials", String.valueOf(allowCredentials));
        response.setHeader("Access-Control-Max-Age", "1800"); // 30 minutos
    }
    
    private void handleDevelopmentCors(HttpServletResponse response) {
        // DESARROLLO: Permisivo
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD, PATCH");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Expose-Headers", "*");
        response.setHeader("Access-Control-Max-Age", "3600");
    }
    
    private boolean isOriginAllowed(String origin) {
        if ("*".equals(allowedOrigins)) {
            return true;
        }
        
        String[] allowed = allowedOrigins.split(",");
        for (String allowedOrigin : allowed) {
            if (allowedOrigin.trim().equals(origin)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}
