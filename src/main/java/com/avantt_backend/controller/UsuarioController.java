package com.avantt_backend.controller;

import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.ErrorResponse;
import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
        @ApiResponse(responseCode = "409", description = "Conflito ao criar o usuário",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = USUARIOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createUsuario(@RequestBody(description = "Dados do usuário a ser criado", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.UsuarioRequestDTO.class), examples = {
                @ExampleObject(name = "Usuario create example", value = "{\"name\": \"Bruno Lima\", \"email\": \"bruno.lima@email.com\", \"perfilId\": 2, \"role\": \"Colaborador\"}")
            })) @Valid @org.springframework.web.bind.annotation.RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO response = usuarioService.create(request);
        if (response == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_ERRO_CRIAR_USUARIO_409);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(summary = "Listar usuários", description = "Retorna uma lista de todos os usuários cadastrados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuários retornada com sucesso",
                     content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhum usuário encontrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(path = USUARIOS, produces = "application/json")
    public ResponseEntity<?> listUsuarios(@RequestParam(value = "projetoId", required = false) Integer projetoId) {
        List<UsuarioResponseDTO> list = usuarioService.listAll(projetoId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Atualizar um usuário", description = "Atualiza os dados de um usuário existente.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Usuário atualizado com sucesso",
                     content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping(path = USUARIOS + "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> patchUsuario(@PathVariable Integer id, @RequestBody(description = "Dados para atualizar o usuário (parciais permitidas)", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.UsuarioRequestDTO.class), examples = {
                @ExampleObject(name = "Usuario patch example", value = "{\"name\": \"Bruno H. Lima\", \"color\": \"#4B7BF5\"}")
            })) @Valid @org.springframework.web.bind.annotation.RequestBody UsuarioRequestDTO request) {
        UsuarioResponseDTO updated = usuarioService.update(id, request);
        if (updated == null) throw new com.avantt_backend.exception.ResourceNotFoundException(MENSAGEM_ERRO_EDITAR_USUARIO_404);
        return ResponseEntity.status(200).body(updated);
    }
}
