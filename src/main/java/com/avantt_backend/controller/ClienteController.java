package com.avantt_backend.controller;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @Operation(summary = "Listar clientes", description = "Retorna todos os clientes")
    @GetMapping(path = CLIENTES, produces = "application/json")
    public ResponseEntity<List<ClienteResponseDTO>> listAll() {
        List<ClienteResponseDTO> list = clienteService.listAll();
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
    // Temporarily removed @Valid so we can log raw payload when debugging client-side validation issues.
    public ResponseEntity<ClienteResponseDTO> create(@RequestBody ClienteRequestDTO request) {
        logger.info("POST /api/clientes payload: {}", request);
        ClienteResponseDTO created = clienteService.create(request);
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
}
