package com.avantt_backend.controller;

import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.service.UsuarioService;
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

import static com.avantt_backend.util.ApiPaths.USUARIOS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping
@Tag(name = "Usuários", description = "Gerenciamento de usuários")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Criar um novo usuário", description = "Cria um novo usuário com os dados fornecidos.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso",
                     content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito ao criar o usuário"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PostMapping(path = USUARIOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioRequestDTO request) {
        try {
            UsuarioResponseDTO response = usuarioService.create(request);
            if (response == null) {
                return ResponseEntity.status(409).body(MENSAGEM_ERRO_CRIAR_USUARIO_409);
            }
            return ResponseEntity.status(201).body(response);
        } catch (com.avantt_backend.exception.ResourceNotFoundException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }

    }

    @Operation(summary = "Listar usuários", description = "Retorna uma lista de todos os usuários cadastrados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
                     content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @GetMapping(path = USUARIOS, produces = "application/json")
    public ResponseEntity<?> listUsuarios(@RequestParam(value = "projetoId", required = false) Integer projetoId) {
        try {
            List<UsuarioResponseDTO> list = usuarioService.listAll(projetoId);
            return ResponseEntity.ok(list);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @Operation(summary = "Atualizar um usuário", description = "Atualiza os dados de um usuário existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                     content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado"),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor")
    })
    @PatchMapping(path = USUARIOS + "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchUsuario(@Valid @PathVariable Integer id, @RequestBody UsuarioRequestDTO request) {
        try {
            UsuarioResponseDTO updated = usuarioService.update(id, request);

            if (updated == null) {
                return ResponseEntity.status(404).body(MENSAGEM_ERRO_EDITAR_USUARIO_404);
            }

            return ResponseEntity.status(200).body(updated);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
