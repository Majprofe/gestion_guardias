package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.dto.HorarioProfesorResponse;
import es.iesjandula.timetable.dto.HorarioDiaResponse;
import es.iesjandula.timetable.dto.ProfesorGuardiaResponse;
import es.iesjandula.timetable.service.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/horario")
@Tag(name = "Horario", description = "Operaciones relacionadas con los horarios del profesorado")
@Validated
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @GetMapping("/profesor/{id}")
    @Operation(
            summary = "Obtener el horario completo de un profesor",
            description = "Devuelve todas las actividades organizadas por día de la semana con asignatura, grupo, aula y abreviatura."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "400", description = "ID de profesor inválido")
    })
    public ResponseEntity<HorarioProfesorResponse> getHorarioCompleto(
            @Parameter(description = "ID del profesor (debe ser positivo)", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "El ID del profesor debe ser positivo") Long id
    ) {
        return ResponseEntity.ok(horarioService.getHorarioProfesor(id));
    }

    @GetMapping("/profesor/{id}/dia/{dia}")
    @Operation(
            summary = "Obtener el horario de un profesor para un día concreto",
            description = "Devuelve las actividades de un profesor en un día específico (1=lunes, 2=martes...)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario del día obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "400", description = "Parámetros inválidos")
    })
    public ResponseEntity<HorarioDiaResponse> getHorarioEnDia(
            @Parameter(description = "ID del profesor (debe ser positivo)", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "El ID del profesor debe ser positivo") Long id,
            @Parameter(description = "Día de la semana (1=Lunes, ..., 5=Viernes)", required = true, example = "1")
            @PathVariable @Min(value = 1, message = "El día debe estar entre 1 y 5") 
                        @Max(value = 5, message = "El día debe estar entre 1 y 5") int dia
    ) {
        return ResponseEntity.ok(horarioService.getHorarioProfesorEnDia(id, dia));
    }

    @Operation(
            summary = "Obtener el horario completo de un profesor excluyendo actividades de tipo OTRA",
            description = "Devuelve el horario completo sin mostrar actividades de tipo OTRA"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario sin actividades OTRA obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "400", description = "ID de profesor inválido")
    })
    @GetMapping("/profesor/{id}/sin-otras")
    public ResponseEntity<HorarioProfesorResponse> getHorarioSinOtras(
            @Parameter(description = "ID del profesor (debe ser positivo)", example = "4") 
            @PathVariable @Min(value = 1, message = "El ID del profesor debe ser positivo") Long id) {
        return ResponseEntity.ok(horarioService.getHorarioProfesorSinOtras(id));
    }

    @Operation(
            summary = "Obtener horario completo por el correo",
            description = "Busca un profesor por email y devuelve su horario completo"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Horario obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Profesor no encontrado"),
        @ApiResponse(responseCode = "400", description = "Email inválido")
    })
    @GetMapping("/profesor/email")
    public ResponseEntity<HorarioProfesorResponse> getHorarioPorEmail(
            @Parameter(description = "Email del profesor", required = true, example = "profesor@iesjandula.es")
            @RequestParam @NotBlank(message = "El email no puede estar vacío") 
                         @Email(message = "El formato del email no es válido") String email) {
        return ResponseEntity.ok(horarioService.getHorarioProfesorPorEmail(email));
    }


    @GetMapping("/profesor/email/{email}/dia/{dia}")
    @Operation(summary = "Obtener el horario de un profesor en un día concreto por email")
    public ResponseEntity<HorarioDiaResponse> getHorarioEnDiaPorEmail(@PathVariable String email, @PathVariable int dia) {
        return ResponseEntity.ok(horarioService.getHorarioProfesorEnDiaPorEmail(email, dia));
    }

    @GetMapping("/profesor/email/{email}/sin-otras")
    @Operation(summary = "Obtener el horario sin actividades tipo OTRA por email")
    public ResponseEntity<HorarioProfesorResponse> getHorarioSinOtrasPorEmail(@PathVariable String email) {
        return ResponseEntity.ok(horarioService.getHorarioProfesorSinOtrasPorEmail(email));
    }
    
    @GetMapping("/profesor/email/{email}/dia/{dia}/clases")
    @Operation(summary = "Obtener sólo las actividades de tipo CLASE de un profesor en un día específico")
    public ResponseEntity<HorarioDiaResponse> getHorarioClasesPorEmailYDia(
            @PathVariable String email, 
            @PathVariable int dia) {
        return ResponseEntity.ok(horarioService.getHorarioClasesPorEmailYDia(email, dia));
    }
    
    @GetMapping("/guardia/dia/{dia}/hora/{hora}")
    @Operation(
            summary = "Obtener profesores de guardia en un día y hora específicos",
            description = "Devuelve la lista de profesores que están de guardia en el día y hora indicados"
    )
    public ResponseEntity<ProfesorGuardiaResponse> getProfesoresGuardia(
            @Parameter(description = "Día de la semana (1=Lunes, ..., 5=Viernes)", required = true)
            @PathVariable int dia,
            @Parameter(description = "Hora del día", required = true)
            @PathVariable int hora) {
        return ResponseEntity.ok(horarioService.getProfesoresGuardia(dia, hora));
    }
}
