package com.avantt_backend.controller;

import com.avantt_backend.config.ApiPaths;
import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.service.TarefaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.config.ApiPaths.API;

@RestController
@RequestMapping
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @PostMapping(path = API + "/tarefas", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createTarefa(@RequestBody TarefaRequestDTO request) {
        var created = tarefaService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping(path = API + "/tarefas", produces = "application/json")
    public ResponseEntity<List<TarefaResponseDTO>> listTarefas(@RequestParam(value = "projetoId", required = false) String projetoId,
                                                              @RequestParam(value = "sprintId", required = false) String sprintId,
                                                              @RequestParam(value = "status", required = false) String status) {
        List<TarefaResponseDTO> list = tarefaService.listAll(projetoId, sprintId, status);
        return ResponseEntity.ok(list);
    }
}
