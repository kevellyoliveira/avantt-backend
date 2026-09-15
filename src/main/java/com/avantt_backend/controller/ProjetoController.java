package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.PROJETOS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping
@Tag(name = "Projetos", description = "Gerenciamento de projetos")
public class ProjetoController {

    private final ProjetoService projetoService;

    public ProjetoController(ProjetoService projetoService) {
        this.projetoService = projetoService;
    }

    @Operation(summary = "Criar um novo projeto", description = "Cria um novo projeto com os dados fornecidos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Projeto criado com sucesso",
                     content = @Content(schema = @Schema(implementation = ProjetoResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito ao criar o projeto"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(path = PROJETOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createProjeto(@Valid @RequestBody ProjetoRequestDTO request) {
        try {
            var created = projetoService.create(request);

            if (created == null) {
                return ResponseEntity.status(409).body(MENSAGEM_ERRO_CRIAR_PROJETO_409);
            }

            return ResponseEntity.status(201).body(created);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @Operation(summary = "Listar projetos", description = "Retorna uma lista de todos os projetos cadastrados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso",
                     content = @Content(schema = @Schema(implementation = ProjetoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhum projeto encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(path = PROJETOS, produces = "application/json")
    public ResponseEntity<?> listProjetos() {
        try {
            List<ProjetoResponseDTO> list = projetoService.listAll();

            if (list.isEmpty()) {
                return ResponseEntity.status(404).body(MENSAGEM_ERRO_LISTAR_PROJETOS_404);
            }

            return ResponseEntity.ok(list);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
