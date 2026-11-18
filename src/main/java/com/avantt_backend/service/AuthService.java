
package com.avantt_backend.service;

import com.avantt_backend.dto.AuthRequestDTO;
import com.avantt_backend.dto.AuthResponseDTO;
import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.dto.PasswordResetRequestDTO;
import com.avantt_backend.dto.PasswordResetConfirmDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.Perfil;
import com.avantt_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final com.avantt_backend.repository.PerfilRepository perfilRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    public AuthService(
            UsuarioRepository usuarioRepository,
            com.avantt_backend.repository.PerfilRepository perfilRepository) {

        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
    }

    @Transactional
    public UsuarioResponseDTO register(RegisterRequestDTO req) {

        String email = req.getEmail() == null
                ? null
                : req.getEmail().trim().toLowerCase();

        if (email == null || email.isBlank()) {
            throw new com.avantt_backend.exception.ApiException(
                    "email é obrigatório");
        }

        if (usuarioRepository.existsByEmail(email)) {
            return null;
        }

        log.info("Register request received: name='{}' email='{}' role='{}'",
                req.getName(), email, req.getRole());

        Usuario u = new Usuario();
        u.setNome(req.getName());
        u.setEmail(email);
        u.setCargo(req.getRole());
        u.setOrganizacaoId(1);
        u.setSenha(passwordEncoder.encode(req.getPassword()));

        Integer defaultPerfilId = getOrCreatePerfilId("Colaborador");
        u.setPerfilId(defaultPerfilId);

        Usuario saved = usuarioRepository.save(u);

        log.info(
                "Usuario saved: id={} nome='{}' email='{}' perfilId='{}' organizacaoId='{}'",
                saved.getId(),
                saved.getNome(),
                saved.getEmail(),
                saved.getPerfilId(),
                saved.getOrganizacaoId());

        return toResponseDTO(saved);
    }

    /**
     * Registra um usuário vinculado a uma organização.
     */
    @Transactional
    public UsuarioResponseDTO register(
            RegisterRequestDTO req,
            Integer organizacaoId) {

        String email = req.getEmail() == null
                ? null
                : req.getEmail().trim().toLowerCase();

        if (email == null || email.isBlank()) {
            throw new com.avantt_backend.exception.ApiException(
                    "email é obrigatório");
        }

        if (organizacaoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Organização não informada.");
        }

        if (usuarioRepository.existsByEmail(email)) {
            return null;
        }

        log.info(
                "Register(with org) request: name='{}' email='{}' role='{}' orgId={}",
                req.getName(),
                email,
                req.getRole(),
                organizacaoId);

        Usuario u = new Usuario();
        u.setNome(req.getName());
        u.setEmail(email);
        u.setCargo(req.getRole());
        u.setSenha(passwordEncoder.encode(req.getPassword()));
        u.setOrganizacaoId(organizacaoId);

        Integer adminPerfilId = getOrCreatePerfilId("Administrador");
        u.setPerfilId(adminPerfilId);

        Usuario saved = usuarioRepository.save(u);

        log.info(
                "Usuario saved (with org): id={} nome='{}' email='{}' perfilId='{}' organizacaoId='{}'",
                saved.getId(),
                saved.getNome(),
                saved.getEmail(),
                saved.getPerfilId(),
                saved.getOrganizacaoId());

        return toResponseDTO(saved);
    }

    /**
     * Localiza um perfil pelo nome ou cria caso não exista.
     */
    private Integer getOrCreatePerfilId(String nome) {

        try {
            Perfil p = perfilRepository.findByNome(nome);

            if (p != null && p.getId() != null) {
                return p.getId();
            }

            Perfil created = new Perfil();
            created.setNome(nome);

            Perfil saved = perfilRepository.save(created);

            return saved.getId();

        } catch (Exception ex) {
            log.error("Erro ao localizar ou criar perfil: {}", nome, ex);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível localizar ou criar o perfil " + nome);
        }
    }

    /**
     * Valida o token e retorna o usuário autenticado.
     */
    @Transactional(readOnly = true)
    public Usuario obterUsuarioAutenticado(String token) {

        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token de autenticação não informado.");
        }

        Usuario usuario = usuarioRepository.findByAuthToken(token)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Token inválido. Faça login novamente."));

        if (usuario.getAuthTokenExpiry() == null
                || !usuario.getAuthTokenExpiry().isAfter(LocalDateTime.now())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Token expirado. Faça login novamente.");
        }

        return usuario;
    }

    @Transactional
    public AuthResponseDTO login(AuthRequestDTO req) {

        String email = req.getEmail() == null
                ? null
                : req.getEmail().trim().toLowerCase();

        var opt = usuarioRepository.findByEmail(email);

        if (opt.isEmpty()) {
            return null;
        }

        Usuario u = opt.get();

        if (u.getSenha() == null) {
            return null;
        }

        if (!passwordEncoder.matches(req.getPassword(), u.getSenha())) {
            return null;
        }

        String token = UUID.randomUUID().toString();

        u.setAuthToken(token);
        u.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));

        usuarioRepository.save(u);

        UsuarioResponseDTO ur = toResponseDTO(u);

        return new AuthResponseDTO(token, ur);
    }

    @Transactional
    public boolean logout(String token) {

        if (token == null) {
            return false;
        }

        var opt = usuarioRepository.findByAuthToken(token);

        if (opt.isEmpty()) {
            return false;
        }

        Usuario u = opt.get();

        u.setAuthToken(null);
        u.setAuthTokenExpiry(null);

        usuarioRepository.save(u);

        return true;
    }

    @Transactional
    public String requestPasswordReset(PasswordResetRequestDTO req) {

        var opt = usuarioRepository.findByEmail(req.getEmail());

        if (opt.isEmpty()) {
            return null;
        }

        Usuario u = opt.get();

        String token = UUID.randomUUID().toString();

        u.setResetToken(token);
        u.setResetTokenExpiry(LocalDateTime.now().plusHours(2));

        usuarioRepository.save(u);

        // Em produção, o token deve ser enviado por e-mail.
        // Retorno mantido para testes e desenvolvimento.
        return token;
    }

    @Transactional
    public boolean confirmPasswordReset(PasswordResetConfirmDTO req) {

        var opt = usuarioRepository.findByResetToken(req.getToken());

        if (opt.isEmpty()) {
            return false;
        }

        Usuario u = opt.get();

        if (u.getResetTokenExpiry() == null
                || u.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            return false;
        }

        u.setSenha(passwordEncoder.encode(req.getNewPassword()));
        u.setResetToken(null);
        u.setResetTokenExpiry(null);

        usuarioRepository.save(u);

        return true;
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {

        UsuarioResponseDTO dto = new UsuarioResponseDTO();

        dto.setId(
                usuario.getId() == null
                        ? null
                        : String.valueOf(usuario.getId()));

        dto.setName(usuario.getNome());
        dto.setEmail(usuario.getEmail());
        dto.setRole(usuario.getCargo());
        dto.setAvatar(generateInitials(usuario.getNome()));
        dto.setColor("#2563eb");

        // Retorna a organização vinculada ao usuário.
        dto.setOrganizacaoId(usuario.getOrganizacaoId());

        return dto;
    }

    private String generateInitials(String nome) {

        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }

        String[] parts = nome.trim().split("\\s+");

        if (parts.length == 1) {
            return parts[0]
                    .substring(0, Math.min(2, parts[0].length()))
                    .toUpperCase();
        }

        StringBuilder sb = new StringBuilder();

        sb.append(parts[0].substring(0, 1));
        sb.append(parts[parts.length - 1].substring(0, 1));

        return sb.toString().toUpperCase();
    }
}
