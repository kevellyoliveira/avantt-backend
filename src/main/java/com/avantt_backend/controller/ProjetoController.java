package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.ErrorResponse;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.service.ProjetoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
                     content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjetoResponseDTO.class), examples = {
                          @ExampleObject(name = "Projeto created example", value = "{\"id\":1,\"name\":\"Portal Corporativo\",\"description\":\"Novo portal\",\"color\":\"#FF5733\",\"status\":\"Em andamento\",\"startDate\":\"2027-07-01\",\"endDate\":\"2027-08-20\",\"progress\":50}")
                     })),
        @ApiResponse(responseCode = "409", description = "Conflito ao criar o projeto",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = PROJETOS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createProjeto(@RequestBody(description = "Dados do projeto a ser criado", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjetoRequestDTO.class), examples = {
                @ExampleObject(name = "Projeto request example", value = "{\"name\": \"Portal Corporativo\", \"description\": \"Novo portal corporativo\", \"color\": \"#FF5733\", \"statusId\": 2, \"startDate\": \"2027-07-01\", \"endDate\": \"2027-08-20\", \"team\": [1,2]}")
            })) @Valid ProjetoRequestDTO request) {
        var created = projetoService.create(request);
        if (created == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_ERRO_CRIAR_PROJETO_409);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Atualizar projeto", description = "Atualiza um projeto existente. Mantém as regras de validação de datas (mínimo 15 dias) e permite adicionar/remover membros do projeto.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso",
                     content = @Content(schema = @Schema(implementation = ProjetoResponseDTO.class))),
        @ApiResponse(responseCode = "400", description = "Requisição inválida",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Projeto não encontrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @ApiResponse(responseCode = "200", description = "Projeto atualizado com sucesso",
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjetoResponseDTO.class), examples = {
            @ExampleObject(name = "Projeto updated example", value = "{\"id\": \"1\", \"name\": \"Portal Corporativo\", \"description\": \"Portal atualizado\", \"progress\": 60}")
        }))
    @PutMapping(path = PROJETOS + "/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateProjeto(@PathVariable Integer id, @RequestBody(description = "Dados para atualizar o projeto", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjetoRequestDTO.class), examples = {
                @ExampleObject(name = "Projeto update request example", value = "{\"name\": \"Portal Corporativo - v2\", \"description\": \"Ajustes\", \"statusId\": 2, \"startDate\": \"2027-07-02\", \"endDate\": \"2027-08-21\"}")
            })) @Valid ProjetoRequestDTO request) {
        var updated = projetoService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Listar projetos", description = "Retorna uma lista de todos os projetos cadastrados.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de projetos retornada com sucesso",
                     content = @Content(schema = @Schema(implementation = ProjetoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Nenhum projeto encontrado",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno do servidor",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(path = PROJETOS, produces = "application/json")
    public ResponseEntity<?> listProjetos() {
        List<ProjetoResponseDTO> list = projetoService.listAll();
        if (list.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException(MENSAGEM_ERRO_LISTAR_PROJETOS_404);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obter progresso do projeto", description = "Recalcula e retorna o progresso do projeto (0..100) baseado nas sprints.")
    @ApiResponse(responseCode = "200", description = "Progresso do projeto",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Project progress example", description = "Exemplo de resposta com progresso do projeto calculado a partir das sprints", value = "{\"progress\":33,\"id\":10,\"totalTasks\":3,\"doneTasks\":1}")
        }))
    @GetMapping(path = PROJETOS + "/{id}/progress", produces = "application/json")
    public ResponseEntity<?> getProjectProgress(@PathVariable Integer id) {
        var info = projetoService.getProgress(id);
        java.util.Map<String,Object> out = new java.util.HashMap<>();
        out.put("id", id);
        out.put("progress", info == null ? 0 : info.progress);
        out.put("totalTasks", info == null ? 0 : info.totalTasks);
        out.put("doneTasks", info == null ? 0 : info.doneTasks);
        return ResponseEntity.ok(out);
    }
}
