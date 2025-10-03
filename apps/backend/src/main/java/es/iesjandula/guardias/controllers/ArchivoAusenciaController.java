package es.iesjandula.guardias.controllers;

import es.iesjandula.guardias.models.ArchivoAusencia;
import es.iesjandula.guardias.services.ArchivoAusenciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controlador REST para la gestión de archivos adjuntos a ausencias.
 */
@RestController
@RequestMapping("/api/archivos")
@CrossOrigin(origins = "*")
@Tag(name = "Archivos de Ausencia", description = "Gestión de archivos adjuntos a ausencias")
public class ArchivoAusenciaController {

    @Autowired
    private ArchivoAusenciaService archivoService;

    /**
     * Sube uno o varios archivos para una ausencia específica.
     *
     * @param ausenciaId ID de la ausencia.
     * @param archivos Archivos a subir.
     * @return Lista de archivos guardados.
     */
    @PostMapping("/ausencia/{ausenciaId}")
    @Operation(summary = "Subir archivos", description = "Sube uno o varios archivos para una ausencia")
    public ResponseEntity<List<ArchivoAusencia>> subirArchivos(
            @PathVariable Long ausenciaId,
            @RequestParam("archivos") MultipartFile[] archivos) {
        
        List<ArchivoAusencia> archivosGuardados = archivoService.guardarArchivos(ausenciaId, archivos);
        return ResponseEntity.ok(archivosGuardados);
    }

    /**
     * Obtiene la lista de archivos de una ausencia específica.
     *
     * @param ausenciaId ID de la ausencia.
     * @return Lista de archivos de la ausencia.
     */
    @GetMapping("/ausencia/{ausenciaId}")
    @Operation(summary = "Listar archivos de ausencia", description = "Obtiene todos los archivos de una ausencia")
    public ResponseEntity<List<ArchivoAusencia>> obtenerArchivosDeAusencia(@PathVariable Long ausenciaId) {
        List<ArchivoAusencia> archivos = archivoService.obtenerArchivosPorAusencia(ausenciaId);
        return ResponseEntity.ok(archivos);
    }

    /**
     * Descarga un archivo específico.
     *
     * @param archivoId ID del archivo.
     * @return El archivo para descarga.
     */
    @GetMapping("/descargar/{archivoId}")
    @Operation(summary = "Descargar archivo", description = "Descarga un archivo específico")
    public ResponseEntity<Resource> descargarArchivo(@PathVariable Long archivoId) {
        Resource archivo = archivoService.cargarArchivo(archivoId);
        ArchivoAusencia metadatos = archivoService.obtenerMetadatosArchivo(archivoId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadatos.getTipoArchivo()))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + metadatos.getNombreArchivo() + "\"")
                .body(archivo);
    }

    /**
     * Elimina un archivo específico.
     *
     * @param archivoId ID del archivo a eliminar.
     * @return Respuesta sin contenido.
     */
    @DeleteMapping("/{archivoId}")
    @Operation(summary = "Eliminar archivo", description = "Elimina un archivo específico")
    public ResponseEntity<Void> eliminarArchivo(@PathVariable Long archivoId) {
        archivoService.eliminarArchivo(archivoId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Obtiene información detallada de un archivo específico.
     *
     * @param archivoId ID del archivo.
     * @return Metadatos del archivo.
     */
    @GetMapping("/{archivoId}")
    @Operation(summary = "Obtener información de archivo", description = "Obtiene los metadatos de un archivo")
    public ResponseEntity<ArchivoAusencia> obtenerInfoArchivo(@PathVariable Long archivoId) {
        ArchivoAusencia archivo = archivoService.obtenerMetadatosArchivo(archivoId);
        return ResponseEntity.ok(archivo);
    }
}
