package com.avantt_backend.controller;

import com.avantt_backend.service.PrioridadeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.avantt_backend.util.ApiPaths.API;
import static com.avantt_backend.util.Mensagens.MENSAGEM_ERRO_INTERNO_500;

@RestController
@RequestMapping(API + "/prioridades")
@Tag(name = "Prioridades", description = "Listagem de prioridades")
public class PrioridadeController {

    private final PrioridadeService service;

    public PrioridadeController(PrioridadeService service) {
        this.service = service;
    }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> list() {
        var all = service.listAll();
        return ResponseEntity.ok(all);
    }
}
