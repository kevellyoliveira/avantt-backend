package com.avantt_backend.controller;

import com.avantt_backend.entity.Perfil;
import com.avantt_backend.repository.PerfilRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.avantt_backend.dto.ErrorResponse;
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

    @Operation(summary = "Listar perfis", description = "Retorna todos os perfis disponíveis no sistema.")
    @GetMapping(produces = "application/json")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de perfis",
            content = @Content(mediaType = "application/json", examples = {
                @ExampleObject(name = "Perfis example", value = "[{\"id\":1,\"nome\":\"Admin\"},{\"id\":2,\"nome\":\"Colaborador\"}]")
            })),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> list() {
        List<Perfil> all = repo.findAll();
        return ResponseEntity.ok(all);
    }
}
