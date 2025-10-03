package es.iesjandula.timetable.controller;

import es.iesjandula.timetable.importer.XmlHorarioImporter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/import")
@Tag(name = "Importación", description = "Importar horarios desde XML")
public class ImportController {

    private final XmlHorarioImporter importer;

    public ImportController(XmlHorarioImporter importer) {
        this.importer = importer;
    }

    @PostMapping(value = "/xml", consumes = "multipart/form-data")
    @Operation(summary = "Importar datos desde un archivo XML de horarios")
    public ResponseEntity<String> importarXml(@RequestParam("file") MultipartFile file) {
        try {
            importer.importar(file);
            return ResponseEntity.ok("✅ XML importado correctamente");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("❌ Error al importar el XML: " + e.getMessage());
        }
    }

}
