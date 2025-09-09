package es.iesjandula.guardias.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador de test para depurar problemas
 */
@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/simple")
    public ResponseEntity<String> simpleTest() {
        return ResponseEntity.ok("Test simple funcionando");
    }

    @GetMapping("/proxy-debug")  
    public ResponseEntity<String> proxyDebugTest() {
        return ResponseEntity.ok("Test proxy debug - No hay dependencias externas");
    }
}
