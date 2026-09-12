package com.avantt_backend.controller;

import com.avantt_backend.config.ApiPaths;
import com.avantt_backend.dto.IdResponseDTO;
import com.avantt_backend.dto.UsuarioFrontendDTO;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.config.ApiPaths.API;


@RestController
@RequestMapping
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping(path = API + "/usuarios", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        var response = usuarioService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(path = API + "/usuarios", produces = "application/json")
    public ResponseEntity<List<UsuarioFrontendDTO>> listUsuarios() {
        List<UsuarioFrontendDTO> list = usuarioService.listAllForFrontend();
        return ResponseEntity.ok(list);
    }

    @PatchMapping(path = API + "/usuarios/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UsuarioFrontendDTO> patchUsuario(@PathVariable Integer id, @RequestBody UsuarioRequestDTO request) {
        UsuarioFrontendDTO updated = usuarioService.update(id, request);
        return ResponseEntity.ok(updated);
    }
}
