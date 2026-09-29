package com.avantt_backend.controller;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaResponseDTO.class), examples = {
                @ExampleObject(name = "Tarefa created example", value = "{\"id\":\"14\",\"title\":\"Corrigir bug do login\",\"project\":\"Portal Corporativo\",\"sprint\":\"Sprint 03 - Melhorias\",\"assignee\":\"Bruno Henrique Lima\",\"avatar\":\"\",\"avatarColor\":\"#4B7BF5\",\"priority\":\"alta\",\"status\":\"em andamento\",\"statusId\":2,\"statusName\":\"Em andamento\",\"daysDelayed\":0,\"plannedEnd\":\"2027-08-15\",\"estimatedHours\":8,\"blockedBy\":null,\"description\":\"Corrigir problema X\",\"tagIds\":[1,3],\"sprintProgress\":33}")
            })
        ),
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> create(
            @RequestBody(description = "Dados da tarefa a ser criada", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaRequestDTO.class), examples = {
                    @ExampleObject(name = "Tarefa request example", value = "{\"title\":\"Corrigir bug do login\",\"projectId\":1,\"sprintId\":3,\"assigneeId\":2,\"statusId\":2,\"plannedEnd\":\"2027-08-15\",\"priority\":\"alta\",\"tagIds\":[1,3],\"description\":\"Corrigir problema X\"}")
                })
            )
            @Valid @org.springframework.web.bind.annotation.RequestBody TarefaRequestDTO request) {
        var created = tarefaService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar tarefas", description = "Lista todas as tarefas com base nos filtros fornecidos.")
    @ApiResponse(responseCode = "200", description = "Lista de tarefas",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Tarefas list example", value = "[{\"id\":\"14\",\"title\":\"Corrigir bug do login\",\"project\":\"Portal Corporativo\",\"sprint\":\"Sprint 03 - Melhorias\",\"assignee\":\"Bruno Henrique Lima\",\"priority\":\"alta\",\"status\":\"em andamento\",\"statusId\":2,\"statusName\":\"Em andamento\",\"plannedEnd\":\"2027-08-15\",\"sprintProgress\":33}]")
        }))
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
    @Operation(summary = "Atribuir/Remover responsável", description = "Atribui ou remove o responsável de uma tarefa. Envie {\"assigneeId\": &lt;id&gt;} para atribuir ou {\"assigneeId\": null} para desatribuir.")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada (assignee)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaResponseDTO.class), examples = {
            @ExampleObject(name = "Assignee updated example", value = "{\"id\":\"14\",\"assignee\":\"Bruno Henrique Lima\",\"sprintProgress\":33}")
        }))
    public ResponseEntity<?> patchAssignee(@PathVariable Integer id, @RequestBody(description = "Corpo: { \"assigneeId\": &lt;id&gt; } (use null para remover)", required = true,
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "Patch assignee example", value = "{\"assigneeId\": 2}")
            })) @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Integer> body) {
        Integer assigneeId = body.get("assigneeId");
        var updated = tarefaService.updateAssignee(id, assigneeId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping(path = "/{id}/usuarios", produces = "application/json")
    @Operation(summary = "Listar usuários da sprint da tarefa", description = "Retorna os usuários associados à sprint da tarefa, úteis para selecionar um assignee.")
    @ApiResponse(responseCode = "200", description = "Lista de usuários disponíveis para atribuição",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Users for task example", value = "[{\"id\":2,\"nome\":\"Bruno Henrique Lima\",\"email\":\"bruno.lima@email.com\"}]")
        }))
    public ResponseEntity<?> listUsersForTask(@PathVariable Integer id) {
        var list = tarefaService.listUsersForTask(id);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Atualizar tarefa", description = "Atualiza todos os campos de uma tarefa existente. Regras de validação iguais à criação.")
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaResponseDTO.class), examples = {
                @ExampleObject(name = "Tarefa updated example", value = "{\"id\":\"14\",\"title\":\"Corrigir bug do login\",\"project\":\"Portal Corporativo\",\"sprint\":\"Sprint 03 - Melhorias\",\"assignee\":\"Bruno Henrique Lima\",\"avatar\":\"\",\"avatarColor\":\"#4B7BF5\",\"priority\":\"alta\",\"status\":\"em andamento\",\"statusId\":2,\"statusName\":\"Em andamento\",\"daysDelayed\":0,\"plannedEnd\":\"2027-08-15\",\"estimatedHours\":8,\"blockedBy\":null,\"description\":\"Corrigir problema X\",\"tagIds\":[1,3],\"sprintProgress\":33}")
            })
        ),
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                content = @Content(schema = @Schema(implementation = com.avantt_backend.dto.ErrorResponse.class)))
    })
    public ResponseEntity<?> updateTask(
            @Parameter(description = "ID da tarefa a ser atualizada", required = true)
            @PathVariable Integer id,
            @RequestBody(description = "Dados da tarefa a ser atualizada", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaRequestDTO.class), examples = {
                    @ExampleObject(name = "Tarefa update request example", value = "{\"title\":\"Corrigir bug do login\",\"projectId\":1,\"sprintId\":3,\"assigneeId\":2,\"statusId\":2,\"plannedEnd\":\"2027-08-15\",\"priority\":\"alta\",\"tagIds\":[1,3],\"description\":\"Corrigir problema X\"}")
                }))
            @Valid @org.springframework.web.bind.annotation.RequestBody TarefaRequestDTO request) {
        var updated = tarefaService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Atualizar prioridade da tarefa", description = "Define ou remove a prioridade da tarefa (enviar prioridadeId ou null para remover).")
    @PatchMapping(path = "/{id}/prioridade", consumes = "application/json", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada (prioridade)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaResponseDTO.class), examples = {
            @ExampleObject(name = "Tarefa priority updated example", value = "{\"id\":\"14\",\"priority\":\"alta\",\"status\":\"em andamento\",\"sprintProgress\":33}")
        }))
    public ResponseEntity<?> patchPriority(@PathVariable Integer id, @RequestBody(description = "Corpo: { \"prioridadeId\": <id> } — enviar null para remover a prioridade", required = true,
            content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Patch prioridade example", value = "{\"prioridadeId\": 2}")
            })) @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Integer> body) {
        Integer prioridadeId = body.get("prioridadeId");
        var updated = tarefaService.updatePriority(id, prioridadeId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Atualizar status da tarefa", description = "Altera apenas o status de uma tarefa e recalcula o progresso da sprint associada.")
    @PatchMapping(path = "/{id}/status", consumes = "application/json", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Tarefa atualizada (status)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.TarefaResponseDTO.class), examples = {
            @ExampleObject(name = "Tarefa status updated example", value = "{\"id\":\"14\",\"title\":\"Corrigir bug do login\",\"project\":\"Portal Corporativo\",\"sprint\":\"Sprint 03 - Melhorias\",\"assignee\":\"Bruno Henrique Lima\",\"priority\":\"alta\",\"status\":\"concluída\",\"statusId\":3,\"sprintProgress\":67}")
        }))
    public ResponseEntity<?> patchStatus(@PathVariable Integer id, @RequestBody(description = "Corpo: { \"statusId\": <id> } - campo statusId é obrigatório", required = true,
            content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Patch status example", value = "{\"statusId\": 3}")
            })) @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        var updated = tarefaService.updateStatus(id, statusId);
        return ResponseEntity.ok(updated);
    }
}
