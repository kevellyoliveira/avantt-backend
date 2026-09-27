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
            @RequestBody TarefaRequestDTO request) {
        try {
            if (request.getStatusId() == null) {
                return ResponseEntity.status(400).body("O campo statusId é obrigatório.");
            }
            if (request.getProjectId() == null) {
                return ResponseEntity.status(400).body("O campo projectId é obrigatório.");
            }
            if (request.getSprintId() == null) {
                return ResponseEntity.status(400).body("O campo sprintId é obrigatório.");
            }
            // rename: accept assigneeId in request body
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
}
