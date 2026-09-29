package com.avantt_backend.controller;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.service.SprintService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
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
    @ApiResponse(responseCode = "201", description = "Sprint criada com sucesso",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintResponseDTO.class), examples = {
            @ExampleObject(name = "Sprint created example", value = "{\"id\": 12, \"name\": \"Sprint 05 - Entrega\", \"project\": \"Portal Corporativo\", \"startDate\": \"2027-09-01\", \"endDate\": \"2027-09-15\", \"daysDelayed\": 0, \"progress\": 0, \"totalTasks\": 0, \"doneTasks\": 0, \"blockedTasks\": 0, \"team\": [\"Ana Carolina Souza\"], \"statusId\": 2, \"statusName\": \"Em andamento\"}")
        }))
    public ResponseEntity<?> createSprint(
            @RequestBody(description = "Dados da sprint a ser criada", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintRequestDTO.class), examples = {
                    @ExampleObject(name = "Sprint request example", value = "{\"name\": \"Sprint 05 - Entrega\", \"projectId\": 1, \"startDate\": \"2027-09-01\", \"endDate\": \"2027-09-15\", \"statusId\": 2, \"team\": [1,2]}")
                }))
            @Valid @org.springframework.web.bind.annotation.RequestBody SprintRequestDTO request) {
        var created = sprintService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Listar sprints", description = "Lista todas as sprints com base no ID do projeto fornecido.")
    @ApiResponse(responseCode = "200", description = "Lista de sprints",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Sprints list example", value = "[{\"id\":12,\"name\":\"Sprint 05 - Entrega\",\"project\":\"Portal Corporativo\",\"startDate\":\"2027-09-01\",\"endDate\":\"2027-09-15\",\"progress\":10}]")
        }))
    @GetMapping(produces = "application/json")
    public ResponseEntity<?> listSprints(
            @Parameter(description = "ID do projeto para filtrar as sprints", required = false)
            @RequestParam(value = "projeto_id", required = false) String projetoId) {
        List<SprintResponseDTO> list = sprintService.listAll(projetoId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Atualizar sprint", description = "Atualiza dados da sprint: nome, datas e associação de membros (adicionar/remover).")
    @PutMapping(path = "/{id}", consumes = "application/json", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Sprint atualizada com sucesso",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintResponseDTO.class), examples = {
            @ExampleObject(name = "Sprint updated example", value = "{\"id\": 12, \"name\": \"Sprint 05 - Entrega\", \"project\": \"Portal Corporativo\", \"startDate\": \"2027-09-01\", \"endDate\": \"2027-09-15\", \"daysDelayed\": 0, \"progress\": 10, \"totalTasks\": 5, \"doneTasks\": 1, \"blockedTasks\": 0, \"team\": [\"Ana\",\"Bruno\"], \"statusId\": 2, \"statusName\": \"Em andamento\"}")
        }))
    public ResponseEntity<?> updateSprint(
            @Parameter(description = "ID da sprint a ser atualizada", required = true)
            @PathVariable Integer id,
            @RequestBody(description = "Dados para atualizar a sprint", required = true,
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintRequestDTO.class), examples = {
                    @ExampleObject(name = "Sprint update request example", value = "{\"name\": \"Sprint 05 - Entrega atualizada\", \"startDate\": \"2027-09-02\", \"endDate\": \"2027-09-16\", \"addTeam\": [3], \"removeTeam\": [2], \"statusId\": 2}")
                })) @Valid @org.springframework.web.bind.annotation.RequestBody SprintRequestDTO request) {
        var updated = sprintService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Atualizar status da sprint", description = "Altera apenas o status de uma sprint.")
    @PatchMapping(path = "/{id}/status", consumes = "application/json", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Sprint atualizada (status)",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintResponseDTO.class), examples = {
            @ExampleObject(name = "Sprint status updated example", value = "{\"id\":12,\"name\":\"Sprint 05 - Entrega\",\"progress\":100}")
        }))
    public ResponseEntity<?> patchSprintStatus(
            @Parameter(description = "ID da sprint a ser atualizada", required = true)
            @PathVariable Integer id,
    @RequestBody(description = "Corpo: { \"statusId\": &lt;id&gt; }", required = true,
                content = @Content(mediaType = "application/json", examples = {
                    @ExampleObject(name = "Patch sprint status example", value = "{\"statusId\": 3}")
                })) @org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Integer> body) {
        Integer statusId = body.get("statusId");
        var updated = sprintService.updateStatus(id, statusId);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Obter progresso da sprint", description = "Recalcula e retorna o progresso da sprint (0..100).")
    @ApiResponse(responseCode = "200", description = "Progresso da sprint",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Sprint progress example", value = "{\"progress\":33,\"id\":10,\"totalTasks\":3,\"doneTasks\":1}")
        }))
    @GetMapping(path = "/{id}/progress", produces = "application/json")
    public ResponseEntity<?> getSprintProgress(
            @Parameter(description = "ID da sprint", required = true)
            @PathVariable Integer id) {
        var info = sprintService.recalculateAndPersistProgress(id);
        java.util.Map<String,Object> out = new java.util.HashMap<>();
        out.put("id", id);
        out.put("progress", info.progress);
        out.put("totalTasks", info.total);
        out.put("doneTasks", info.done);
        return ResponseEntity.ok(out);
    }
}
