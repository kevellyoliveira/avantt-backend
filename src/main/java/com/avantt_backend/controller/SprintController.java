package com.avantt_backend.controller;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

import static com.avantt_backend.util.ApiPaths.SPRINTS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping(SPRINTS)
@Tag(name = "Sprints", description = "Gerenciamento de sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @Operation(summary = "Criar uma nova sprint", description = "Cria uma nova sprint com base nos dados fornecidos.")
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSprint(
            @Parameter(description = "Dados da sprint a ser criada", required = true)
            @Valid @RequestBody SprintRequestDTO request) {
        var created = sprintService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar sprints", description = "Lista todas as sprints com base no ID do projeto fornecido.")
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> listSprints(
            @Parameter(description = "ID do projeto para filtrar as sprints", required = false)
            @RequestParam(value = "projeto_id", required = false) String projetoId) {
        List<SprintResponseDTO> list = sprintService.listAll(projetoId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Atualizar sprint", description = "Atualiza dados da sprint: nome, datas e associação de membros (adicionar/remover).")
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateSprint(
            @Parameter(description = "ID da sprint a ser atualizada", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody SprintRequestDTO request) {
        var updated = sprintService.update(id, request);
        return ResponseEntity.ok(updated);
    }
}
