package com.avantt_backend.controller;

import com.avantt_backend.dto.ErrorResponse;
import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.service.OrganizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.ORGANIZACOES;

@RestController
@RequestMapping
@Tag(name = "Organizações", description = "Gerenciamento de organizações")
public class OrganizacaoController {

    private final OrganizacaoService organizacaoService;

    public OrganizacaoController(OrganizacaoService organizacaoService) {
        this.organizacaoService = organizacaoService;
    }

    @Operation(summary = "Criar uma nova organização", description = "Cria uma organização.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Organização criada com sucesso",
                     content = @Content(schema = @Schema(implementation = OrganizacaoResponseDTO.class))),
        @ApiResponse(responseCode = "409", description = "Conflito ao criar a organização",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Erro interno",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping(path = ORGANIZACOES, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createOrganizacao(@Valid @RequestBody OrganizacaoRequestDTO request) {
        OrganizacaoResponseDTO created = organizacaoService.create(request);
        if (created == null) throw new com.avantt_backend.exception.ConflictException("Erro ao criar organização");
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Atualizar organização", description = "Atualiza uma organização existente. Suporta PUT e PATCH")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organização atualizada",
                     content = @Content(schema = @Schema(implementation = OrganizacaoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organização não encontrada",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @RequestMapping(path = ORGANIZACOES + "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> updateOrganizacao(@PathVariable Integer id, @Valid @RequestBody OrganizacaoRequestDTO request) {
        // If client included id in body, ensure it matches the path id
        if (request.getId() != null && !request.getId().equals(id)) {
            throw new IllegalArgumentException("ID do body não confere com id do path");
        }

        OrganizacaoResponseDTO updated = organizacaoService.update(id, request);
        if (updated == null) throw new com.avantt_backend.exception.ResourceNotFoundException("Organização não encontrada");
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Listar organizações", description = "Retorna todas as organizações ou, se o parâmetro query 'id' for fornecido, retorna apenas a organização informada.")
    @GetMapping(path = ORGANIZACOES, produces = "application/json")
    public ResponseEntity<?> listAll(
            @Parameter(in = ParameterIn.QUERY,
                       description = "ID da organização (opcional). Se fornecido, o resultado conterá apenas a organização com este id.",
                       example = "1",
                       schema = @Schema(type = "integer", format = "int32"))
            @RequestParam(value = "id", required = false) Integer id) {
        // If id query parameter provided, delegate to getById for convenience
        if (id != null) {
            OrganizacaoResponseDTO dto = organizacaoService.getById(id);
            return ResponseEntity.ok(dto);
        }

        List<OrganizacaoResponseDTO> list = organizacaoService.listAll();
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obter organização por id", description = "Retorna a organização correspondente ao id informado.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organização encontrada",
                     content = @Content(schema = @Schema(implementation = OrganizacaoResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "Organização não encontrada",
                     content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping(path = ORGANIZACOES + "/{id}", produces = "application/json")
    public ResponseEntity<?> getOrganizacaoById(
            @Parameter(in = ParameterIn.PATH,
                       description = "ID da organização",
                       required = true,
                       schema = @Schema(type = "integer", format = "int32", example = "1"))
            @PathVariable Integer id) {
        OrganizacaoResponseDTO dto = organizacaoService.getById(id);
        return ResponseEntity.ok(dto);
    }
}
