package com.avantt_backend.controller;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.TAREFAS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping(TAREFAS)
@Tag(name = "Tarefas", description = "Gerenciamento de tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @Operation(summary = "Criar uma nova tarefa", description = "Cria uma nova tarefa com base nos dados fornecidos.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> create(
            @Parameter(description = "Dados da tarefa a ser criada", required = true)
            @Valid @RequestBody TarefaRequestDTO request) {
        var created = tarefaService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar tarefas", description = "Lista todas as tarefas com base nos filtros fornecidos.")
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> list(
            @Parameter(description = "ID do projeto para filtrar tarefas", required = false)
            @RequestParam(value = "projetoId", required = false) String projetoId,
            @Parameter(description = "ID da sprint para filtrar tarefas", required = false)
            @RequestParam(value = "sprintId", required = false) String sprintId,
            @Parameter(description = "Status das tarefas para filtrar", required = false)
            @RequestParam(value = "status", required = false) String status) {
        List<TarefaResponseDTO> list = tarefaService.listAll(projetoId, sprintId, status);
        return ResponseEntity.ok(list);
    }

    @PatchMapping(path = "/{id}/assignee", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchAssignee(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        Integer assigneeId = body.get("assigneeId");
        var updated = tarefaService.updateAssignee(id, assigneeId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping(path = "/{id}/usuarios", produces = "application/json")
    public ResponseEntity<?> listUsersForTask(@PathVariable Integer id) {
        var list = tarefaService.listUsersForTask(id);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Atualizar tarefa", description = "Atualiza todos os campos de uma tarefa existente. Regras de validação iguais à criação.")
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    @io.swagger.v3.oas.annotations.responses.ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @io.swagger.v3.oas.annotations.media.Content(schema = @io.swagger.v3.oas.annotations.media.Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> updateTask(
            @Parameter(description = "ID da tarefa a ser atualizada", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody TarefaRequestDTO request) {
        var updated = tarefaService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping(path = "/{id}/prioridade", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchPriority(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        Integer prioridadeId = body.get("prioridadeId");
        var updated = tarefaService.updatePriority(id, prioridadeId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Atualizar status da tarefa", description = "Altera apenas o status de uma tarefa e recalcula o progresso da sprint associada.")
    @PatchMapping(path = "/{id}/status", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchStatus(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        var updated = tarefaService.updateStatus(id, statusId);
        return ResponseEntity.ok(updated);
    }
}
