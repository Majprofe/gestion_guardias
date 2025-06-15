package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.model.TramoHorario;
import es.iesjandula.timetable.service.TramoHorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tramohorarios")
@Tag(name = "TramoHorarios", description = "Operaciones sobre tramohorarios")
public class TramoHorarioController {

    private final TramoHorarioService service;

    public TramoHorarioController(TramoHorarioService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los tramohorarios")
    public List<TramoHorario> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un tramohorario por ID")
    public TramoHorario getById(@PathVariable Long id) {
        return service.findById(id);
    }
}
