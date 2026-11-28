
package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.ErrorResponse;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.service.ProjetoService;
import com.avantt_backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import static com.avantt_backend.util.ApiPaths.PROJETOS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping
@Tag(name = "Projetos", description = "Gerenciamento de projetos")
public class ProjetoController {

    private final ProjetoService projetoService;
    private final AuthService authService;

    public ProjetoController(
            ProjetoService projetoService,
            AuthService authService) {

        this.projetoService = projetoService;
        this.authService = authService;
    }

    // =========================================================
    // AUTENTICAÇÃO E ORGANIZAÇÃO
    // =========================================================

    private Usuario obterUsuarioAutenticado(String authorization) {

        if (authorization == null
                || !authorization.startsWith("Bearer ")) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de autenticação não informado ou inválido."
            );
        }

        String token = authorization.substring(7);

        Usuario usuario = authService.obterUsuarioAutenticado(token);

        if (usuario == null || usuario.getOrganizacaoId() == null) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário autenticado não possui uma organização vinculada."
            );
        }

        return usuario;
    }

    private Integer obterOrganizacaoId(String authorization) {
        return obterUsuarioAutenticado(authorization).getOrganizacaoId();
    }

    // =========================================================
    // CADASTRAR PROJETO
    // =========================================================

    @Operation(
            summary = "Criar um novo projeto",
            description = "Cria um novo projeto com os dados fornecidos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Projeto criado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ProjetoResponseDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "Projeto created example",
                                            value = "{\"id\":\"1\",\"name\":\"Portal Corporativo\",\"description\":\"Novo portal\",\"color\":\"#FF5733\",\"status\":\"Em andamento\",\"startDate\":\"2027-07-01\",\"endDate\":\"2027-08-20\",\"progress\":50}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflito ao criar o projeto",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PostMapping(
            path = {PROJETOS, "/projetos"},
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> createProjeto(

            @RequestBody(
                    description = "Dados do projeto a ser criado",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ProjetoRequestDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "Projeto request example",
                                            value = "{\"name\":\"Portal Corporativo\",\"description\":\"Novo portal corporativo\",\"color\":\"#FF5733\",\"status\":\"Em andamento\",\"startDate\":\"2027-07-01\",\"endDate\":\"2027-08-20\",\"team\":[1,2]}"
                                    )
                            }
                    )
            )
            @Valid
            @org.springframework.web.bind.annotation.RequestBody
            ProjetoRequestDTO request,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Usuario usuario = obterUsuarioAutenticado(authorization);

        ProjetoResponseDTO created = projetoService.create(
                request,
                usuario.getOrganizacaoId(),
                usuario.getId()
        );

        if (created == null) {
            throw new com.avantt_backend.exception.ConflictException(
                    MENSAGEM_ERRO_CRIAR_PROJETO_409
            );
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // =========================================================
    // ATUALIZAR PROJETO
    // =========================================================

    @Operation(
            summary = "Atualizar projeto",
            description = "Atualiza um projeto existente, incluindo seus dados, equipe, marcos e riscos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto atualizado com sucesso",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProjetoResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto não encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @PutMapping(
            path = {PROJETOS + "/{id}", "/projetos/{id}"},
            consumes = "application/json",
            produces = "application/json"
    )
    public ResponseEntity<?> updateProjeto(

            @PathVariable Integer id,

            @RequestBody(
                    description = "Dados para atualizar o projeto",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    implementation = ProjetoRequestDTO.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "Projeto update request example",
                                            value = "{\"name\":\"Portal Corporativo - v2\",\"description\":\"Ajustes\",\"status\":\"Em andamento\",\"startDate\":\"2027-07-02\",\"endDate\":\"2027-08-21\"}"
                                    )
                            }
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody
            ProjetoRequestDTO request,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Integer organizacaoId = obterOrganizacaoId(authorization);

        ProjetoResponseDTO updated = projetoService.update(
                id,
                request,
                organizacaoId
        );

        return ResponseEntity.ok(updated);
    }

    // =========================================================
    // BUSCAR PROJETO POR ID
    // =========================================================

    @Operation(
            summary = "Buscar projeto por ID",
            description = "Retorna os dados de um projeto específico, incluindo equipe, marcos e riscos."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Projeto encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProjetoResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto não encontrado",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping(
            path = {PROJETOS + "/{id}", "/projetos/{id}"},
            produces = "application/json"
    )
    public ResponseEntity<?> buscarProjetoPorId(

            @PathVariable Integer id,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Integer organizacaoId = obterOrganizacaoId(authorization);

        ProjetoResponseDTO projeto = projetoService.buscarPorId(
                id,
                organizacaoId
        );

        return ResponseEntity.ok(projeto);
    }

    // =========================================================
    // LISTAR PROJETOS
    // =========================================================

    @Operation(
            summary = "Listar projetos",
            description = "Retorna os projetos da organização do usuário autenticado e também os projetos nos quais ele está cadastrado como membro."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de projetos retornada com sucesso",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ProjetoResponseDTO.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Erro interno do servidor",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping(
            path = {PROJETOS, "/projetos"},
            produces = "application/json"
    )
    public ResponseEntity<List<ProjetoResponseDTO>> listProjetos(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Usuario usuario = obterUsuarioAutenticado(authorization);

        List<ProjetoResponseDTO> list =
                projetoService.listarPorOrganizacaoOuMembro(
                        usuario.getOrganizacaoId(),
                        usuario.getId()
                );

        // Mesmo sem projetos cadastrados, retorna HTTP 200 com lista vazia.
        return ResponseEntity.ok(list);
    }

    // =========================================================
    // PROGRESSO DO PROJETO
    // =========================================================

    @Operation(
            summary = "Obter progresso do projeto",
            description = "Recalcula e retorna o progresso do projeto (0..100) baseado nas sprints."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Progresso do projeto"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Projeto não encontrado"
            )
    })
    @GetMapping(
            path = {PROJETOS + "/{id}/progress", "/projetos/{id}/progress"},
            produces = "application/json"
    )
    public ResponseEntity<?> getProjectProgress(

            @PathVariable Integer id,

            @RequestHeader(
                    value = "Authorization",
                    required = false
            ) String authorization) {

        Integer organizacaoId = obterOrganizacaoId(authorization);

        // Verifica se o projeto pertence à organização autenticada
        projetoService.buscarPorId(id, organizacaoId);

        var info = projetoService.getProgress(id);

        Map<String, Object> out = new HashMap<>();

        out.put("id", id);
        out.put("progress", info == null ? 0 : info.progress);
        out.put("totalTasks", info == null ? 0 : info.totalTasks);
        out.put("doneTasks", info == null ? 0 : info.doneTasks);

        return ResponseEntity.ok(out);
    }
}
