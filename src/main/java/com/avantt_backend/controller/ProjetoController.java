package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.PROJETOS;

@RestController
@RequestMapping
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping(path = PROJETOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createProjeto(@Valid @RequestBody ProjetoRequestDTO request) {
        var created = projetoService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping(path = PROJETOS, produces = "application/json")
    public ResponseEntity<List<ProjetoResponseDTO>> listProjetos() {
        List<ProjetoResponseDTO> list = projetoService.listAll();
        return ResponseEntity.ok(list);
    }
}
