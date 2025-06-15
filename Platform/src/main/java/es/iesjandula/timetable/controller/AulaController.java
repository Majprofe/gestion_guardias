package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.model.Aula;
import es.iesjandula.timetable.service.AulaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/aulas")
@Tag(name = "Aulas", description = "Operaciones sobre aulas")
public class AulaController {

    private final AulaService service;

    public AulaController(AulaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los aulas")
    public List<Aula> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un aula por ID")
    public Aula getById(@PathVariable Long id) {
        return service.findById(id);
    }
}
