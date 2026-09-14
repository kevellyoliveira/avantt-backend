package com.avantt_backend.controller;

import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.USUARIOS;

@RestController
@RequestMapping
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping(path = USUARIOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        var response = usuarioService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping(path = USUARIOS, produces = "application/json")
    public ResponseEntity<List<UsuarioResponseDTO>> listUsuarios() {
        try {
            List<UsuarioResponseDTO> list = usuarioService.listAll();

            if (list.isEmpty()) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.status(200).body(list);


        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PatchMapping(path = USUARIOS + "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UsuarioResponseDTO> patchUsuario(@PathVariable Integer id, @RequestBody UsuarioRequestDTO request) {
        try {
            UsuarioResponseDTO updated = usuarioService.update(id, request);

            if (updated == null) {
                return ResponseEntity.status(404).build();
            }
            return ResponseEntity.status(200).body(updated);


        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
