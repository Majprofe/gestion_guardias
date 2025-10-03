package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.model.Grupo;
import es.iesjandula.timetable.service.GrupoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/grupos")
@Tag(name = "Grupos", description = "Operaciones sobre grupos")
public class GrupoController {

    private final GrupoService service;

    public GrupoController(GrupoService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Listar todos los grupos")
    public List<Grupo> getAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un grupo por ID")
    public Grupo getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @GetMapping("/abreviatura/{abreviatura}")
    @Operation(summary = "Obtener un grupo por abreviatura", 
               description = "Retorna la información de un grupo incluyendo si es problemático")
    public Grupo getByAbreviatura(@PathVariable String abreviatura) {
        return service.findByAbreviatura(abreviatura);
    }
}
