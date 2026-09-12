package com.avantt_backend.controller;

import com.avantt_backend.config.ApiPaths;
import com.avantt_backend.dto.ProjetoFrontendDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.service.ProjetoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.config.ApiPaths.API;

@RestController
@RequestMapping
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @PostMapping(path = API + "/projetos", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createProjeto(@Valid @RequestBody ProjetoRequestDTO request) {
        var created = projetoService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping(path = API + "/projetos", produces = "application/json")
    public ResponseEntity<List<ProjetoFrontendDTO>> listProjetos() {
        List<ProjetoFrontendDTO> list = projetoService.listAllForFrontend();
        return ResponseEntity.ok(list);
    }
}
