package com.avantt_backend.service;

import com.avantt_backend.dto.AuthRequestDTO;
import com.avantt_backend.dto.PasswordResetConfirmDTO;
import com.avantt_backend.dto.PasswordResetRequestDTO;
import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setup() {
        // nothing
    }

    @Test
    void register_returnsNullWhenEmailExists() {
        when(usuarioRepository.existsByEmail("a@b.com")).thenReturn(true);
        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setEmail("a@b.com");
        req.setName("A");
        req.setPassword("pw");
        var res = authService.register(req);
        assertNull(res);
    }

    @Test
    void login_successAndFailure() {
        Usuario u = new Usuario();
        u.setId(5);
        u.setNome("User");
        u.setEmail("u@e.com");
        // set encoded password using BCrypt to match
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder enc = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        u.setSenha(enc.encode("secret"));

        when(usuarioRepository.findByEmail("u@e.com")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuthRequestDTO good = new AuthRequestDTO();
        good.setEmail("u@e.com");
        good.setPassword("secret");

        var res = authService.login(good);
        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("User", res.getUser().getName());

        AuthRequestDTO bad = new AuthRequestDTO();
        bad.setEmail("u@e.com");
        bad.setPassword("wrong");
        assertNull(authService.login(bad));
    }

    @Test
    void logout_andPasswordResetFlow() {
        Usuario u = new Usuario();
        u.setId(7);
        u.setEmail("x@y.com");
        u.setAuthToken("tok123");

        when(usuarioRepository.findByAuthToken("tok123")).thenReturn(Optional.of(u));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(authService.logout("tok123"));

        // request password reset
        Usuario u2 = new Usuario();
        u2.setId(8);
        u2.setEmail("reset@e.com");
        when(usuarioRepository.findByEmail("reset@e.com")).thenReturn(Optional.of(u2));

        PasswordResetRequestDTO pr = new PasswordResetRequestDTO();
        pr.setEmail("reset@e.com");
        String token = authService.requestPasswordReset(pr);
        assertNotNull(token);

        // confirm with expired token -> should fail
        Usuario u3 = new Usuario();
        u3.setId(9);
        u3.setResetToken("t1");
        u3.setResetTokenExpiry(LocalDateTime.now().minusHours(1));
        when(usuarioRepository.findByResetToken("t1")).thenReturn(Optional.of(u3));

        PasswordResetConfirmDTO confirm = new PasswordResetConfirmDTO();
        confirm.setToken("t1");
        confirm.setNewPassword("newpass");
        assertFalse(authService.confirmPasswordReset(confirm));
    }
}
