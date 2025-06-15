package com.FranGarcia.NuevaVersionGuardias.controllers;

import com.FranGarcia.NuevaVersionGuardias.dto.CoberturaDTO;
import com.FranGarcia.NuevaVersionGuardias.models.Cobertura;


import com.FranGarcia.NuevaVersionGuardias.services.CoberturaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;


/**
 * Controlador REST para la gestión de coberturas de ausencias.
 * Proporciona endpoints para crear, listar, buscar y eliminar coberturas.
 */
@RestController
@RequestMapping("/api/coberturas")
@CrossOrigin(origins = "http://localhost:5500") // Ajusta según tu frontend
@Tag(name = "Coberturas", description = "Gestión de coberturas de ausencias")
public class CoberturaController {

    @Autowired
    private CoberturaService coberturaService;

    /**
     * Guarda una nueva cobertura a partir de un DTO.
     *
     * @param dto Objeto DTO con los datos necesarios para crear una cobertura.
     * @return La cobertura creada envuelta en una ResponseEntity.
     */
    @PostMapping
    @Operation(summary = "Guardar una nueva cobertura")
    public ResponseEntity<Cobertura> guardar(@RequestBody CoberturaDTO dto) {
        Cobertura cobertura = coberturaService.guardarDesdeDto(dto);
        return ResponseEntity.ok(cobertura);
    }

    /**
     * Lista todas las coberturas existentes.
     *
     * @return Lista de todas las coberturas registradas.
     */
    @GetMapping
    @Operation(summary = "Listar todas las coberturas")
    public ResponseEntity<List<Cobertura>> listar() {
        return ResponseEntity.ok(coberturaService.listar());
    }

    /**
     * Busca una cobertura asociada a una ausencia específica por su ID.
     *
     * @param id ID de la ausencia.
     * @return La cobertura correspondiente si existe, o 404 si no se encuentra.
     */
    @GetMapping("/ausencia/{id}")
    public ResponseEntity<CoberturaDTO> buscarPorAusencia(@PathVariable Long id) {
        return coberturaService.findByAusenciaId(id)
                .map(c -> {
                    CoberturaDTO dto = new CoberturaDTO();
                    dto.setAusenciaId(c.getAusencia().getId());
                    dto.setProfesorCubreEmail(c.getProfesorCubreEmail());
                    dto.setGrupo(c.getGrupo());
                    dto.setAula(c.getAula());
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }


    /**
     * Elimina la cobertura asociada a una ausencia específica por su ID.
     *
     * @param id ID de la ausencia.
     * @return 200 OK si fue eliminada, o 404 si no se encontró cobertura asociada.
     */
    @DeleteMapping("/ausencia/{id}")
    @Operation(summary = "Eliminar cobertura por ID de ausencia")
    public ResponseEntity<Void> eliminarPorAusencia(@PathVariable Long id) {
        boolean eliminado = coberturaService.eliminarCoberturaPorAusencia(id);
        return eliminado ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    /**
     * Obtiene todas las coberturas en las que está asignado un profesor específico.
     *
     * @param email Correo electrónico del profesor.
     * @return Lista de coberturas asignadas al profesor.
     */
    @GetMapping("/profesor/{email}")
    public ResponseEntity<List<CoberturaDTO>> obtenerPorProfesor(@PathVariable String email) {
        return ResponseEntity.ok(coberturaService.obtenerCoberturasDTOPorProfesor(email));
    }

    @PostMapping("/asignarCobertura/{ausenciaId}/{emailProfesor}")
    @Transactional
    public ResponseEntity<Cobertura> asignarCobertura(
            @PathVariable Long ausenciaId,
            @PathVariable String emailProfesor) {
        return ResponseEntity.ok(coberturaService.asignarCoberturaManual(ausenciaId, emailProfesor));
    }

}





    /*
    // Asignar una cobertura a una ausencia
    @PostMapping("/asignarCobertura/{ausenciaId}/{profesorEmail}")
    public ResponseEntity<?> asignarCobertura(@PathVariable Long ausenciaId, @PathVariable String profesorEmail) {
        System.out.println("📌 Recibida solicitud para asignar cobertura: ausencia ID " + ausenciaId + ", profesor: " + profesorEmail);

        try {
            Cobertura cobertura = coberturaService.asignarCobertura(ausenciaId, profesorEmail);
            return ResponseEntity.ok(cobertura);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error asignando cobertura: " + e.getMessage());
        }
    }

    // Buscar cobertura por ID de ausencia
    @GetMapping("/buscarPorAusencia/{ausenciaId}")
    public ResponseEntity<?> buscarCoberturaPorAusencia(@PathVariable Long ausenciaId) {
        Optional<Cobertura> cobertura = coberturaService.findByAusenciaId(ausenciaId);
        if (cobertura.isPresent()) {
            return ResponseEntity.ok(cobertura.get().getProfesorCubre().getEmail());  // Devuelve el email del profesor que cubrió la ausencia
        } else {
            return ResponseEntity.ok(null);  // Devuelve null si no hay cobertura
        }
    }



    // Eliminar una cobertura específica
    @DeleteMapping("/eliminarPorAusencia/{ausenciaId}")
    public ResponseEntity<String> eliminarCoberturaPorAusencia(@PathVariable Long ausenciaId) {
        System.out.println("📌 Recibida solicitud para eliminar cobertura de la ausencia ID: " + ausenciaId);

        boolean eliminado = coberturaService.eliminarCoberturaPorAusencia(ausenciaId);

        if (eliminado) {
            System.out.println("✅ Cobertura eliminada correctamente de la base de datos.");
            return ResponseEntity.ok("Cobertura eliminada correctamente.");
        } else {
            System.out.println("❌ No se encontró cobertura para la ausencia ID: " + ausenciaId);
            return ResponseEntity.status(404).body("Cobertura no encontrada.");
        }
    }




    // Obtener el profesor que cubrió una ausencia
    @GetMapping("/profesorQueCubre/{ausenciaId}")
    public ResponseEntity<String> obtenerProfesorQueCubre(@PathVariable Long ausenciaId) {
        Optional<String> profesorEmail = coberturaService.obtenerProfesorQueCubre(ausenciaId);
        return profesorEmail.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body("Cobertura no encontrada"));
    }

    @GetMapping("/misCoberturas/{email}")
    public ResponseEntity<List<Map<String, Object>>> obtenerMisCoberturas(@PathVariable String email) {
        List<Cobertura> coberturas = coberturaRepository.findByProfesorCubreEmail(email);

        List<Map<String, Object>> respuesta = coberturas.stream().map(cobertura -> {
            Map<String, Object> coberturaDTO = new HashMap<>();
            coberturaDTO.put("id", cobertura.getId());
            coberturaDTO.put("fecha", cobertura.getAusencia().getFecha());
            coberturaDTO.put("hora", cobertura.getAusencia().getHora().toString());
            coberturaDTO.put("profesorCubreEmail", cobertura.getProfesorCubre().getEmail());
            coberturaDTO.put("profesorAusenteEmail", cobertura.getAusencia().getProfesorAusente().getEmail());
            coberturaDTO.put("ausenciaId", cobertura.getAusencia().getId()); // 🔹 Agregar ausenciaId
            return coberturaDTO;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(respuesta);
    }



}

     */
