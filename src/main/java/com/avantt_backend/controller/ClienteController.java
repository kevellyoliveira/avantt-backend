package com.avantt_backend.controller;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

import static com.avantt_backend.util.ApiPaths.CLIENTES;

@RestController
@RequestMapping
@Tag(name = "Clientes", description = "Gerenciamento de clientes")
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    private final ClienteService clienteService;
    private final AuthService authService;

    public ClienteController(
            ClienteService clienteService,
            AuthService authService) {
        this.clienteService = clienteService;
        this.authService = authService;
    }

    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes")
    @GetMapping(path = CLIENTES, produces = "application/json")
    public ResponseEntity<List<ClienteResponseDTO>> listAll(
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
        List<ClienteResponseDTO> list =
                clienteService.listAll(obterOrganizacaoId(authorization));
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obter cliente por id", description = "Retorna o cliente correspondente ao id informado.")
    @GetMapping(path = CLIENTES + "/{id}", produces = "application/json")
    public ResponseEntity<ClienteResponseDTO> getById(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
        ClienteResponseDTO dto = clienteService.getById(
                id,
                obterOrganizacaoId(authorization)
        );
        return ResponseEntity.ok(dto);
    }

    @Operation(summary = "Criar cliente", description = "Cria um novo cliente")
    @PostMapping(path = CLIENTES, consumes = "application/json", produces = "application/json")
    // Temporarily removed @Valid so we can log raw payload when debugging client-side validation issues.
    public ResponseEntity<ClienteResponseDTO> create(
            @RequestBody ClienteRequestDTO request,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
        logger.info("POST /api/clientes payload: {}", request);
        ClienteResponseDTO created = clienteService.create(
                request,
                obterOrganizacaoId(authorization)
        );
        // Location header like /api/clientes/{id}
        URI location = URI.create(CLIENTES + "/" + created.getId());
        return ResponseEntity.created(location).body(created);
    }

    @Operation(summary = "Atualizar cliente", description = "Atualiza um cliente existente. Suporta PUT e PATCH")
    @RequestMapping(path = CLIENTES + "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH}, consumes = "application/json", produces = "application/json")
    public ResponseEntity<ClienteResponseDTO> update(
            @PathVariable Integer id,
            @RequestBody ClienteRequestDTO request,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
        if (request.getId() != null && !request.getId().equals(id)) {
            throw new IllegalArgumentException("ID do body não confere com id do path");
        }
        ClienteResponseDTO updated = clienteService.update(
                id,
                request,
                obterOrganizacaoId(authorization)
        );
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Excluir cliente", description = "Desativa (soft-delete) o cliente informado")
    @DeleteMapping(path = CLIENTES + "/{id}")
    public ResponseEntity<?> delete(
            @PathVariable Integer id,
            @RequestHeader(value = "Authorization", required = false)
            String authorization) {
        clienteService.delete(id, obterOrganizacaoId(authorization));
        return ResponseEntity.noContent().build();
    }

    private Integer obterOrganizacaoId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de autenticação não informado ou inválido."
            );
        }

        Usuario usuario = authService.obterUsuarioAutenticado(
                authorization.substring(7)
        );
        if (usuario == null || usuario.getOrganizacaoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Usuário autenticado não possui uma organização vinculada."
            );
        }
        return usuario.getOrganizacaoId();
    }
}
