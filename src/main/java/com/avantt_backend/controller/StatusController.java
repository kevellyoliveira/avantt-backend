package com.avantt_backend.controller;

import com.avantt_backend.entity.Status;
import com.avantt_backend.repository.StatusTarefaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import jakarta.annotation.PostConstruct;

import static com.avantt_backend.util.ApiPaths.STATUS;

@RestController
@RequestMapping(STATUS)
@Tag(name = "Status", description = "Listagem de status")
public class StatusController {

    private final StatusTarefaRepository repo;

    public StatusController(StatusTarefaRepository repo) {
        this.repo = repo;
    }

    @PostConstruct
    public void ensureTaskStatuses() {
        ensureStatus("Planejada", "Tarefa planejada, mas ainda não iniciada");
        ensureStatus("Em andamento", "Tarefa atualmente em execução");
        ensureStatus("Concluída", "Tarefa finalizada");
        ensureStatus("Bloqueada", "Tarefa impedida de continuar");
        ensureStatus("Revisão", "Tarefa aguardando revisão");
        ensureStatus("Cancelada", "Tarefa cancelada");
    }

    private void ensureStatus(String nome, String descricao) {
        if (repo.findByNomeIgnoreCase(nome).isEmpty()) {
            Status status = new Status();
            status.setNome(nome);
            status.setDescricao(descricao);
            repo.save(status);
        }
    }

    @Operation(summary = "Listar status", description = "Retorna todos os status válidos para projeto/sprint/tarefa.")
    @GetMapping(produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Lista de status",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Status example", value = "[{\"id\":1,\"nome\":\"Planejada\"},{\"id\":3,\"nome\":\"Concluída\"}]")
        }))
    public ResponseEntity<?> list() {
        List<Status> all = repo.findAll().stream()
                .filter(status -> com.avantt_backend.util.StatusUtils.isValidTaskStatus(status.getNome()))
                .toList();
        return ResponseEntity.ok(all);
    }
}
