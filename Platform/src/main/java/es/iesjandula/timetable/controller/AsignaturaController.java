package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.model.Asignatura;
import es.iesjandula.timetable.service.AsignaturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/asignaturas")
@Tag(name = "Asignaturas", description = "Operaciones sobre asignaturas")
public class AsignaturaController {

    private final AsignaturaService service;

    public AsignaturaController(AsignaturaService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los asignaturas")
    public List<Asignatura> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un asignatura por ID")
    public Asignatura getById(@PathVariable Long id) {
        return service.findById(id);
    }
}
