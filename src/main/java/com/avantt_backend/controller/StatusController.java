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

import static com.avantt_backend.util.ApiPaths.STATUS;

@RestController
@RequestMapping(STATUS)
@Tag(name = "Status", description = "Listagem de status")
public class StatusController {

    private final StatusTarefaRepository repo;

    public StatusController(StatusTarefaRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Listar status", description = "Retorna todos os status válidos para projeto/sprint/tarefa.")
    @GetMapping(produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Lista de status",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Status example", value = "[{\"id\":1,\"nome\":\"Planejada\"},{\"id\":3,\"nome\":\"Concluída\"}]")
        }))
    public ResponseEntity<?> list() {
        List<Status> all = repo.findAll();
        return ResponseEntity.ok(all);
    }
}
