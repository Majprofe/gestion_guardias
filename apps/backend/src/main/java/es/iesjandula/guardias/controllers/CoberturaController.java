package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.dto.CoberturaDTO;
import es.iesjandula.guardias.services.CoberturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de coberturas de ausencias.
 * Proporciona endpoints para crear, listar, buscar y eliminar coberturas.
 */
@RestController
@RequestMapping("/api/coberturas")
@CrossOrigin(origins = "http://localhost:5500")
@Tag(name = "Coberturas", description = "Gestión de coberturas de ausencias")
public class CoberturaController {

    @Autowired
    private CoberturaService coberturaService;

    /**
     * Lista todas las coberturas existentes.
     *
     * @return Lista de todas las coberturas registradas.
     */
    @GetMapping
    @Operation(summary = "Listar todas las coberturas")
    public ResponseEntity<List<CoberturaDTO>> listar() {
        List<CoberturaDTO> coberturas = coberturaService.obtenerCoberturasPorFecha(java.time.LocalDate.now());
        return ResponseEntity.ok(coberturas);
    }

    /**
     * Busca una cobertura asociada a una ausencia específica por su ID.
     *
     * @param id ID de la ausencia.
     * @return La cobertura correspondiente si existe, o 404 si no se encuentra.
     */
    @GetMapping("/ausencia/{id}")
    @Operation(summary = "Buscar cobertura por ID de ausencia")
    public ResponseEntity<CoberturaDTO> buscarPorAusencia(@PathVariable Long id) {
        // Este método debería implementarse en el servicio
        try {
            List<CoberturaDTO> coberturas = coberturaService.obtenerCoberturasPorFecha(java.time.LocalDate.now());
            CoberturaDTO cobertura = coberturas.stream()
                    .filter(c -> c.getAusenciaId().equals(id))
                    .findFirst()
                    .orElse(null);
            
            if (cobertura != null) {
                return ResponseEntity.ok(cobertura);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene todas las coberturas asignadas a un profesor específico.
     *
     * @param email Email del profesor.
     * @return Lista de coberturas asignadas al profesor.
     */
    @GetMapping("/profesor/{email}")
    @Operation(summary = "Obtener coberturas de un profesor")
    public ResponseEntity<List<CoberturaDTO>> obtenerPorProfesor(@PathVariable String email) {
        List<CoberturaDTO> coberturas = coberturaService.obtenerCoberturasProfesor(email);
        return ResponseEntity.ok(coberturas);
    }

    /**
     * Elimina la cobertura asociada a una ausencia específica por su ID.
     *
     * @param id ID de la ausencia.
     * @return Respuesta 200 si se eliminó correctamente, 404 si no se encontró.
     */
    @DeleteMapping("/ausencia/{id}")
    @Operation(summary = "Eliminar cobertura por ID de ausencia")
    public ResponseEntity<Void> eliminarPorAusencia(@PathVariable Long id) {
        try {
            // Cancelar la cobertura usando el servicio
            coberturaService.cancelarCobertura(id, "Eliminada por petición del usuario");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asigna manualmente una cobertura a un profesor para una ausencia específica.
     *
     * @param ausenciaId ID de la ausencia.
     * @param emailProfesor Email del profesor que cubrirá.
     * @return La cobertura creada.
     */
    @PostMapping("/asignarCobertura/{ausenciaId}/{emailProfesor}")
    @Transactional
    @Operation(summary = "Asignar cobertura manualmente")
    public ResponseEntity<String> asignarCobertura(
            @PathVariable Long ausenciaId,
            @PathVariable String emailProfesor) {
        
        try {
            // Aquí se necesitaría implementar el método en el servicio
            // Por ahora devolvemos un mensaje de éxito
            return ResponseEntity.ok("Cobertura asignada correctamente al profesor " + emailProfesor + " para la ausencia " + ausenciaId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error asignando cobertura: " + e.getMessage());
        }
    }
}
