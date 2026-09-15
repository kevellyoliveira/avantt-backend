package com.avantt_backend.service;

import com.avantt_backend.dto.AuthRequestDTO;
import com.avantt_backend.dto.AuthResponseDTO;
import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.dto.PasswordResetRequestDTO;
import com.avantt_backend.dto.PasswordResetConfirmDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public UsuarioResponseDTO register(RegisterRequestDTO req) {
        if (usuarioRepository.existsByEmail(req.getEmail())) return null;

        Usuario u = new Usuario();
        u.setNome(req.getName());
        u.setEmail(req.getEmail());
        u.setCargo(req.getRole());
        u.setSenha(passwordEncoder.encode(req.getPassword()));

        Usuario saved = usuarioRepository.save(u);

        UsuarioResponseDTO dto = new UsuarioResponseDTO();
        dto.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        dto.setName(saved.getNome());
        dto.setEmail(saved.getEmail());
        dto.setRole(saved.getCargo());
        dto.setAvatar(generateInitials(saved.getNome()));
        dto.setColor("#2563eb");
        return dto;
    }

    @Transactional
    public AuthResponseDTO login(AuthRequestDTO req) {
        var opt = usuarioRepository.findByEmail(req.getEmail());
        if (opt.isEmpty()) return null;
        Usuario u = opt.get();
        if (u.getSenha() == null) return null;
        if (!passwordEncoder.matches(req.getPassword(), u.getSenha())) return null;

        String token = UUID.randomUUID().toString();
        u.setAuthToken(token);
        u.setAuthTokenExpiry(LocalDateTime.now().plusHours(24));
        usuarioRepository.save(u);

        UsuarioResponseDTO ur = new UsuarioResponseDTO();
        ur.setId(u.getId() == null ? null : String.valueOf(u.getId()));
        ur.setName(u.getNome());
        ur.setEmail(u.getEmail());
        ur.setRole(u.getCargo());
        ur.setAvatar(generateInitials(u.getNome()));
        ur.setColor("#2563eb");

        return new AuthResponseDTO(token, ur);
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

    private String generateInitials(String nome) {
        if (nome == null || nome.trim().isEmpty()) return "";
        String[] parts = nome.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        StringBuilder sb = new StringBuilder();
        sb.append(parts[0].substring(0,1));
        sb.append(parts[parts.length-1].substring(0,1));
        return sb.toString().toUpperCase();
    }
}
