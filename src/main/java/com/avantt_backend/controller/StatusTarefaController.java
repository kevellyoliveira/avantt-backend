package com.avantt_backend.controller;

import com.avantt_backend.entity.Status;
import com.avantt_backend.repository.StatusTarefaRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.STATUS;
import static com.avantt_backend.util.Mensagens.MENSAGEM_ERRO_INTERNO_500;

@RestController
@RequestMapping(STATUS)
@Tag(name = "Status", description = "Listagem de status")
public class StatusTarefaController {

    private final StatusTarefaRepository repo;

    public StatusTarefaController(StatusTarefaRepository repo) {
        this.repo = repo;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> list() {
        try {
            List<Status> all = repo.findAll();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
