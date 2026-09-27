package com.avantt_backend.controller;

import com.avantt_backend.entity.Perfil;
import com.avantt_backend.repository.PerfilRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.API;
import static com.avantt_backend.util.Mensagens.MENSAGEM_ERRO_INTERNO_500;

@RestController
@RequestMapping(API + "/perfis")
@Tag(name = "Perfis", description = "Listagem de perfis")
public class PerfilController {

    private final PerfilRepository repo;

    public PerfilController(PerfilRepository repo) { this.repo = repo; }

    @GetMapping(produces = "application/json")
    public ResponseEntity<?> list() {
        List<Perfil> all = repo.findAll();
        return ResponseEntity.ok(all);
    }
}
