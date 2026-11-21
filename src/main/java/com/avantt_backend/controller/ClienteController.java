package com.avantt_backend.controller;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import com.avantt_backend.service.AuthService;

import static com.avantt_backend.util.ApiPaths.CLIENTES;

@RestController
@RequestMapping
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @Autowired
    private AuthService authService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes")
    @GetMapping(path = CLIENTES, produces = "application/json")
    public ResponseEntity<List<ClienteResponseDTO>> listAll(HttpServletRequest request) {
        Integer organizacaoId = getOrganizacaoId(request);
        List<ClienteResponseDTO> list = organizacaoId == null
                ? clienteService.listAll()
                : clienteService.listAll(organizacaoId);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obter cliente por id", description = "Retorna o cliente correspondente ao id informado.")
    @GetMapping(path = CLIENTES + "/{id}", produces = "application/json")
    public ResponseEntity<ClienteResponseDTO> getById(@PathVariable Integer id) {
        ClienteResponseDTO dto = clienteService.getById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Criar cliente", description = "Cria um novo cliente")
    @PostMapping(path = CLIENTES, consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClienteResponseDTO> create(
            @Valid @RequestBody ClienteRequestDTO request,
            HttpServletRequest httpRequest) {
        Integer organizacaoId = getOrganizacaoId(httpRequest);
        ClienteResponseDTO created = organizacaoId == null
                ? clienteService.create(request)
                : clienteService.create(request, organizacaoId);
        // Location header like /api/clientes/{id}
        URI location = URI.create(CLIENTES + "/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza um cliente existente. Suporta PUT e PATCH")
    @RequestMapping(path = CLIENTES + "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Integer id, @RequestBody ClienteRequestDTO request) {
        if (request.getId() != null && !request.getId().equals(id)) {
            throw new IllegalArgumentException("ID do body não confere com id do path");
        }
        ClienteResponseDTO updated = clienteService.update(id, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Excluir cliente", description = "Desativa (soft-delete) o cliente informado")
    @DeleteMapping(path = CLIENTES + "/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        clienteService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private Integer getOrganizacaoId(HttpServletRequest request) {
        if (authService == null || request == null) return null;
        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) return null;
        String token = authorization.trim();
        if (token.regionMatches(true, 0, "Bearer ", 0, 7)) token = token.substring(7).trim();
        if (token.isBlank()) return null;
        try {
            return authService.obterUsuarioAutenticado(token).getOrganizacaoId();
        } catch (Exception ignored) {
            return null;
        }
    }
}
