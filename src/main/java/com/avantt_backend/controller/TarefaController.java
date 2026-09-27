package com.avantt_backend.controller;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> create(
            @Parameter(description = "Dados da tarefa a ser criada", required = true)
            @Valid @RequestBody TarefaRequestDTO request) {
        try {
            // validations for required fields are handled by DTO (@Valid)
            var created = tarefaService.create(request);
            return ResponseEntity.status(201).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
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
        try {
            List<TarefaResponseDTO> list = tarefaService.listAll(projetoId, sprintId, status);
            return ResponseEntity.ok(list);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PatchMapping(path = "/{id}/assignee", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchAssignee(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        try {
            Integer assigneeId = body.get("assigneeId");
            var updated = tarefaService.updateAssignee(id, assigneeId);
            return ResponseEntity.ok(updated);
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (com.avantt_backend.exception.ApiException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @GetMapping(path = "/{id}/usuarios", produces = "application/json")
    public ResponseEntity<?> listUsersForTask(@PathVariable Integer id) {
        try {
            var list = tarefaService.listUsersForTask(id);
            return ResponseEntity.ok(list);
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @Operation(summary = "Atualizar tarefa", description = "Atualiza todos os campos de uma tarefa existente. Regras de validação iguais à criação.")
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateTask(
            @Parameter(description = "ID da tarefa a ser atualizada", required = true)
            @PathVariable Integer id,
            @Valid @RequestBody TarefaRequestDTO request) {
        try {
            var updated = tarefaService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (com.avantt_backend.exception.ApiException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @PatchMapping(path = "/{id}/prioridade", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchPriority(@PathVariable Integer id, @RequestBody java.util.Map<String, Integer> body) {
        try {
            Integer prioridadeId = body.get("prioridadeId");
            var updated = tarefaService.updatePriority(id, prioridadeId);
            return ResponseEntity.ok(updated);
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (com.avantt_backend.exception.ApiException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
