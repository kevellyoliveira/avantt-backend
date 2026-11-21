package com.avantt_backend.controller;

import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.dto.RegisterOrganizacaoRequestDTO;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.OrganizacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static com.avantt_backend.util.Mensagens.MENSAGEM_EMAIL_CADASTRADO_409;

import jakarta.validation.Valid;

/**
 * Exposes a public /register endpoint for clients that call the root path
 * (some frontends expect POST /register). Delegates to the existing AuthService.
 */
@RestController
public class PublicAuthController {

    private final AuthService authService;
    private final OrganizacaoService organizacaoService;

    public PublicAuthController(AuthService authService, OrganizacaoService organizacaoService) {
        this.authService = authService;
        this.organizacaoService = organizacaoService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequestDTO req) {
        var res = authService.register(req);
        if (res == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_EMAIL_CADASTRADO_409);
        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("usuario", res);
        resp.put("user", res);
        return ResponseEntity.status(201).body(resp);
    }

    @PostMapping("/auth/register-organizacao")
    public ResponseEntity<?> registerOrganizacao(@Valid @RequestBody RegisterOrganizacaoRequestDTO req) {
        var userReq = req.getUsuario();

        if (userReq == null) {
            userReq = new RegisterRequestDTO();
            userReq.setName(req.getName());
            userReq.setEmail(req.getEmail());
            userReq.setPassword(req.getPassword());
            userReq.setRole(req.getRole());
        }

        var orgReq = req.getOrganizacao();
        if (orgReq == null) {
            orgReq = new com.avantt_backend.dto.OrganizacaoRequestDTO();
            orgReq.setNomeFantasia(req.getOrganizationName());
            orgReq.setCnpj(req.getCnpj());
            orgReq.setEmailContato(req.getEmailContato());
            orgReq.setTelefone(req.getTelefone());
            orgReq.setContato(req.getContato());
            orgReq.setIsAtivo(req.getIsAtivo());
        }

        if (orgReq.getEmailContato() == null || orgReq.getEmailContato().isBlank()) {
            orgReq.setEmailContato(
                    req.getEmailContato() == null || req.getEmailContato().isBlank()
                            ? userReq.getEmail()
                            : req.getEmailContato());
        }
        if (orgReq.getTelefone() == null || orgReq.getTelefone().isBlank()) {
            orgReq.setTelefone(req.getTelefone());
        }
        if (orgReq.getContato() == null || orgReq.getContato().isBlank()) {
            orgReq.setContato(
                    req.getContato() == null || req.getContato().isBlank()
                            ? userReq.getName()
                            : req.getContato());
        }

        var createdOrg = organizacaoService.create(orgReq);
        Integer orgId = null;
        try {
            orgId = createdOrg.getId() == null ? null : Integer.valueOf(createdOrg.getId());
        } catch (NumberFormatException ex) {
            throw new com.avantt_backend.exception.ApiException("Erro ao criar organização");
        }

        var createdUser = authService.register(userReq, orgId);
        if (createdUser == null) throw new com.avantt_backend.exception.ConflictException(MENSAGEM_EMAIL_CADASTRADO_409);
        java.util.Map<String, Object> resp = new java.util.LinkedHashMap<>();
        resp.put("organizacao", createdOrg);
        resp.put("organization", createdOrg);
        resp.put("usuario", createdUser);
        resp.put("user", createdUser);
        return ResponseEntity.status(201).body(resp);
    }
}
