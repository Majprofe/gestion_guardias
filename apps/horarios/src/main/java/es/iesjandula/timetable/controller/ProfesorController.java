package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.dto.HorarioProfesorDto;
import es.iesjandula.timetable.dto.ProfesorGuardiaDto;
import es.iesjandula.timetable.dto.ActualizarContadorDto;
import es.iesjandula.timetable.model.Profesor;
import es.iesjandula.timetable.service.ProfesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/profesores")
@Tag(name = "Profesores", description = "Operaciones sobre profesores")
public class ProfesorController {

    private final ProfesorService service;

    public ProfesorController(ProfesorService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los profesores")
    public List<Profesor> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un profesor por ID")
    public Profesor getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Obtener un profesor por email")
    public Profesor getByEmail(@PathVariable String email) {
        return service.findByEmail(email);
    }

    @GetMapping("/email-by-nombre")
    @Operation(summary = "Obtener el email de un profesor a partir de su nombre")
    public ResponseEntity<String> getEmailByNombre(@RequestParam String nombre) {
        return service.obtenerEmailPorNombre(nombre)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/buscar-nombres")
    @Operation(summary = "Buscar nombres de profesores que coincidan parcialmente con un texto")
    public List<String> buscarNombresParciales(@RequestParam String nombreParcial) {
        if (nombreParcial.isEmpty()) {
            return service.findAll()
                    .stream()
                    .map(Profesor::getNombre)
                    .collect(Collectors.toList());
        }
        return service.buscarNombresParciales(nombreParcial);
    }

    @PostMapping("/incrementar-guardia-normal/{id}")
    public ResponseEntity<Void> incrementarGuardiaNormal(@PathVariable Long id) {
        Profesor p = service.incrementarGuardiaRealizada(id);
        return (p != null) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/incrementar-guardia-problematica/{id}")
    public ResponseEntity<Void> incrementarGuardiaProblematica(@PathVariable Long id) {
        Profesor p = service.incrementarGuardiaProblematica(id);
        return (p != null) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/decrementar-guardia-normal/{id}")
    public ResponseEntity<Void> decrementarGuardiaNormal(@PathVariable Long id) {
        Profesor p = service.decrementarGuardiaRealizada(id);
        return (p != null) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/decrementar-guardia-problematica/{id}")
    public ResponseEntity<Void> decrementarGuardiaProblematica(@PathVariable Long id) {
        Profesor p = service.decrementarGuardiaProblematica(id);
        return (p != null) ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    // ==================== NUEVOS ENDPOINTS PARA INTEGRACIÓN ====================

    @GetMapping("/guardias")
    @Operation(summary = "Obtener profesores de guardia", 
               description = "Retorna la lista de profesores con información de guardias realizadas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<ProfesorGuardiaDto>> getProfesoresDeGuardia() {
        List<ProfesorGuardiaDto> profesores = service.getProfesoresConGuardias();
        return ResponseEntity.ok(profesores);
    }

    @GetMapping("/{email}/guardias-detalle")
    @Operation(summary = "Obtener información detallada de guardias de un profesor",
               description = "Retorna información detallada de guardias de un profesor por su email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Información obtenida exitosamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ProfesorGuardiaDto> getProfesorGuardiasDetalle(
            @Parameter(description = "Email del profesor", example = "juan.perez@instituto.edu")
            @PathVariable String email) {
        ProfesorGuardiaDto profesor = service.getProfesorConGuardias(email);
        return ResponseEntity.ok(profesor);
    }

    @PutMapping("/{email}/contadores")
    @Operation(summary = "Actualizar contadores de guardias de un profesor",
               description = "Actualiza los contadores específicos de guardias para un profesor en un día y hora determinados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contadores actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ProfesorGuardiaDto> actualizarContadores(
            @Parameter(description = "Email del profesor", example = "juan.perez@instituto.edu")
            @PathVariable String email,
            @Valid @RequestBody ActualizarContadorDto contadorDto) {
        
        // Validar que el email coincida
        if (!email.equals(contadorDto.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        
        ProfesorGuardiaDto profesorActualizado = service.actualizarContadores(contadorDto);
        return ResponseEntity.ok(profesorActualizado);
    }

    @PostMapping("/contadores/batch")
    @Operation(summary = "Actualizar múltiples contadores en lote",
               description = "Actualiza múltiples contadores de guardias para diferentes profesores en una sola operación")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contadores actualizados exitosamente"),
        @ApiResponse(responseCode = "400", description = "Algunos datos de entrada son inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<List<ProfesorGuardiaDto>> actualizarContadoresLote(
            @Valid @RequestBody List<ActualizarContadorDto> contadoresDto) {
        
        List<ProfesorGuardiaDto> profesoresActualizados = service.actualizarContadoresLote(contadoresDto);
        return ResponseEntity.ok(profesoresActualizados);
    }

    @GetMapping("/{email}/contadores/{dia}/{hora}")
    @Operation(summary = "Obtener contador específico",
               description = "Obtiene el contador de guardias de un profesor para un día y hora específicos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contador obtenido exitosamente"),
        @ApiResponse(responseCode = "404", description = "Contador o profesor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<ProfesorGuardiaDto.ContadorDetalle> getContadorEspecifico(
            @Parameter(description = "Email del profesor", example = "juan.perez@instituto.edu")
            @PathVariable String email,
            @Parameter(description = "Día de la semana (1-7)", example = "1")
            @PathVariable Integer dia,
            @Parameter(description = "Hora del día (1-12)", example = "3")
            @PathVariable Integer hora) {
        
        ProfesorGuardiaDto.ContadorDetalle contador = service.getContadorEspecifico(email, dia, hora);
        return ResponseEntity.ok(contador);
    }

    @DeleteMapping("/{email}/contadores")
    @Operation(summary = "Resetear contadores de un profesor",
               description = "Elimina todos los contadores de guardias de un profesor específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contadores reseteados exitosamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    public ResponseEntity<Void> resetearContadores(
            @Parameter(description = "Email del profesor", example = "juan.perez@instituto.edu")
            @PathVariable String email) {
        
        service.resetearContadores(email);
        return ResponseEntity.ok().build();
    }
}
