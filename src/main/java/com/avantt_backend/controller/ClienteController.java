package com.avantt_backend.controller;

import com.avantt_backend.config.AuthInterceptor;
import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.service.ClienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> list(HttpServletRequest request) {
        return ResponseEntity.ok(service.listByOrganization(currentUser(request).getOrganizacaoId()));
    }

    @PostMapping
    public ResponseEntity<ClienteResponseDTO> create(@Valid @RequestBody ClienteRequestDTO body,
                                                     HttpServletRequest request) {
        return ResponseEntity.status(201).body(service.create(body, currentUser(request).getOrganizacaoId()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> update(@PathVariable Integer id,
                                                     @Valid @RequestBody ClienteRequestDTO body,
                                                     HttpServletRequest request) {
        return ResponseEntity.ok(service.update(id, body, currentUser(request).getOrganizacaoId()));
    }

    private Usuario currentUser(HttpServletRequest request) {
        return (Usuario) request.getAttribute(AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE);
    }
}
