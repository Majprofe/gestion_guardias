package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.dto.HorarioProfesorDto;
import es.iesjandula.timetable.model.Profesor;
import es.iesjandula.timetable.service.ProfesorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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


}
