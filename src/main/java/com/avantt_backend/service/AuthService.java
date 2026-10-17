package com.avantt_backend.service;

import com.avantt_backend.dto.AuthRequestDTO;
import com.avantt_backend.dto.AuthResponseDTO;
import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.dto.PasswordResetRequestDTO;
import com.avantt_backend.dto.PasswordResetConfirmDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.dto.ChangePasswordRequestDTO;
import com.avantt_backend.dto.AcceptInvitationRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.Organizacao;
import com.avantt_backend.repository.OrganizacaoRepository;
import com.avantt_backend.repository.PerfilRepository;
import com.avantt_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final OrganizacaoRepository organizacaoRepository;
    private final PerfilRepository perfilRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(
            UsuarioRepository usuarioRepository,
            OrganizacaoRepository organizacaoRepository,
            PerfilRepository perfilRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.perfilRepository = perfilRepository;
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) return null;

        Organizacao organization = new Organizacao();
        organization.setNomeFantasia("Organização de " + req.getName().trim());
        organization.setEmailContato(req.getEmail().trim().toLowerCase());
        Organizacao savedOrganization = organizacaoRepository.save(organization);

        Usuario u = new Usuario();
        u.setNome(req.getName());
        u.setEmail(req.getEmail());
        u.setCargo(req.getRole());
        u.setPapel(req.getRole());
        u.setOrganizacaoId(savedOrganization.getId());
        u.setPerfilId(perfilRepository.findByNomeIgnoreCase("Desenvolvedor")
                .or(() -> perfilRepository.findByNomeIgnoreCase("Colaborador"))
                .orElseThrow(() -> new com.avantt_backend.exception.ApiException("Perfil de desenvolvedor não configurado"))
                .getId());
        u.setSenha(passwordEncoder.encode(req.getPassword()));
        u.setIsAtivo(true);

        Usuario saved = usuarioRepository.save(u);

        String token = UUID.randomUUID().toString();
        saved.setAuthToken(token);
        saved.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(saved);

        return new AuthResponseDTO(token, toResponse(saved));
    }

    @Transactional
    public AuthResponseDTO registerOrganization(com.avantt_backend.dto.OrganizationRegisterRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) {
            throw new com.avantt_backend.exception.ConflictException("Email já cadastrado");
        }

        String normalizedCnpj = req.getCnpj() == null || req.getCnpj().isBlank()
                ? null
                : req.getCnpj().replaceAll("\\D", "");
        if (normalizedCnpj != null && organizacaoRepository.existsByCnpj(normalizedCnpj)) {
            throw new com.avantt_backend.exception.ConflictException("CNPJ já cadastrado");
        }

        Organizacao organization = new Organizacao();
        organization.setNomeFantasia(req.getOrganizationName().trim());
        organization.setCnpj(normalizedCnpj);
        organization.setEmailContato(req.getEmail().trim().toLowerCase());
        organization.setIsAtivo(true);
        Organizacao savedOrganization = organizacaoRepository.save(organization);

        Usuario manager = new Usuario();
        manager.setNome(req.getName().trim());
        manager.setEmail(req.getEmail().trim().toLowerCase());
        manager.setCargo("Gestor");
        Integer adminProfileId = perfilRepository.findByNomeIgnoreCase("Administrador")
                .or(() -> perfilRepository.findByNomeIgnoreCase("Admin"))
                .orElseThrow(() -> new com.avantt_backend.exception.ApiException("Perfil Admin não configurado"))
                .getId();
        manager.setPerfilId(adminProfileId);
        manager.setOrganizacaoId(savedOrganization.getId());
        manager.setSenha(passwordEncoder.encode(req.getPassword()));
        manager.setIsAtivo(true);
        manager = usuarioRepository.save(manager);

        String token = UUID.randomUUID().toString();
        manager.setAuthToken(token);
        manager.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(manager);

        return new AuthResponseDTO(token, toResponse(manager));
    }

    @Transactional
    public AuthResponseDTO login(AuthRequestDTO req) {
        var opt = usuarioRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) return null;
        Usuario u = opt.get();
        if (Boolean.FALSE.equals(u.getIsAtivo())) return null;
        if (u.getSenha() == null) return null;
        if (!passwordEncoder.matches(req.getPassword(), u.getSenha())) return null;

        String token = UUID.randomUUID().toString();
        u.setAuthToken(token);
        u.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(u);

        return new AuthResponseDTO(token, toResponse(u));
    }

    @Transactional(readOnly = true)
    public Usuario authenticateToken(String token) {
        if (token == null || token.isBlank()) return null;
        var opt = usuarioRepository.findByAuthToken(token.trim());
        if (opt.isEmpty()) return null;
        Usuario user = opt.get();
        if (Boolean.FALSE.equals(user.getIsAtivo())) return null;
        if (user.getAuthTokenExpiry() == null || user.getAuthTokenExpiry().isBefore(LocalDateTime.now())) return null;
        return user;
    }

    @Transactional
    public void changePassword(String token, ChangePasswordRequestDTO req) {
        Usuario user = authenticateToken(token);
        if (user == null) throw new com.avantt_backend.exception.UnauthorizedException("Sessão inválida ou expirada");
        if (user.getSenha() == null || !passwordEncoder.matches(req.getSenhaAtual(), user.getSenha())) {
            throw new com.avantt_backend.exception.ApiException("Senha atual inválida");
        }
        if (passwordEncoder.matches(req.getNovaSenha(), user.getSenha())) {
            throw new com.avantt_backend.exception.ApiException("A nova senha deve ser diferente da senha atual");
        }
        user.setSenha(passwordEncoder.encode(req.getNovaSenha()));
        usuarioRepository.save(user);
    }

    @Transactional
    public boolean logout(String token) {
        if (token == null) return false;
        var opt = usuarioRepository.findByAuthToken(token);
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        u.setAuthToken(null);
        u.setAuthTokenExpiry(null);
        usuarioRepository.save(u);
        return true;
    }

    @Transactional
    public String requestPasswordReset(PasswordResetRequestDTO req) {
        var opt = usuarioRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) return null;
        Usuario u = opt.get();
        String token = UUID.randomUUID().toString();
        u.setResetToken(token);
        u.setResetTokenExpiry(LocalDateTime.now().plusHours(2));
        usuarioRepository.save(u);
        // In real app: send email. For now return the token for testing/dev.
        return token;
    }

    @Transactional
    public boolean confirmPasswordReset(PasswordResetConfirmDTO req) {
        var opt = usuarioRepository.findByResetToken(req.getToken());
        if (opt.isEmpty()) return false;
        Usuario u = opt.get();
        if (u.getResetTokenExpiry() == null || u.getResetTokenExpiry().isBefore(LocalDateTime.now())) return false;
        u.setSenha(passwordEncoder.encode(req.getNewPassword()));
        u.setResetToken(null);
        u.setResetTokenExpiry(null);
        usuarioRepository.save(u);
        return true;
    }

    @Transactional
    public AuthResponseDTO acceptInvitation(AcceptInvitationRequestDTO req) {
        var opt = usuarioRepository.findByResetToken(req.getToken());
        if (opt.isEmpty()) throw new com.avantt_backend.exception.UnauthorizedException("Convite inválido ou expirado");
        Usuario user = opt.get();
        if (user.getResetTokenExpiry() == null || user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new com.avantt_backend.exception.UnauthorizedException("Convite inválido ou expirado");
        }
        user.setNome(req.getName().trim());
        user.setSenha(passwordEncoder.encode(req.getPassword()));
        user.setIsAtivo(true);
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        user.setAuthToken(UUID.randomUUID().toString());
        user.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(user);
        return new AuthResponseDTO(user.getAuthToken(), toResponse(user));
    }

    private String generateInitials(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        String[] parts = nome.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0].substring(0,1));
        sb.append(parts[parts.length-1].substring(0,1));
        return sb.toString().toUpperCase();
    }

    private UsuarioResponseDTO toResponse(Usuario user) {
        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(user.getId() == null ? null : String.valueOf(user.getId()));
        dto.setName(user.getNome());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getCargo());
        dto.setAvatar(generateInitials(user.getNome()));
        dto.setColor("#2563eb");
        dto.setOrganizationId(user.getOrganizacaoId());
        if (user.getOrganizacaoId() != null) {
            organizacaoRepository.findById(user.getOrganizacaoId())
                    .ifPresent(organization -> dto.setOrganizationName(organization.getNomeFantasia()));
        }
        return dto;
    }
}
