
package com.avantt_backend.service;

import com.avantt_backend.dto.AuthRequestDTO;
import com.avantt_backend.dto.PasswordResetConfirmDTO;
import com.avantt_backend.dto.PasswordResetRequestDTO;
import com.avantt_backend.dto.RegisterRequestDTO;
import com.avantt_backend.entity.Perfil;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.repository.PerfilRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PerfilRepository perfilRepository;

    @Mock
    EmailService emailService;

    @Mock
    ConviteService conviteService;

    @InjectMocks
    AuthService authService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(
                authService,
                "frontendUrl",
                "http://localhost:8443"
        );
    }

    @Test
    void register_returnsNullWhenEmailExists() {

        when(usuarioRepository.existsByEmail("a@b.com"))
                .thenReturn(true);

        RegisterRequestDTO req = new RegisterRequestDTO();
        req.setEmail("a@b.com");
        req.setName("A");
        req.setPassword("pw");

        var res = authService.register(req);

        assertNull(res);

        verify(usuarioRepository).existsByEmail("a@b.com");
        verifyNoInteractions(emailService, conviteService);
    }

    @Test
    void register_sendsFirstAccessInvitation() {
        Perfil perfil = new Perfil();
        perfil.setId(3);
        when(perfilRepository.findByNome("Colaborador")).thenReturn(perfil);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);
                    usuario.setId(17);
                    return usuario;
                });

        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setName("Nova desenvolvedora");
        request.setEmail("dev@example.com");
        request.setPassword("senha-segura");
        request.setRole("Colaborador");

        var response = authService.register(request);

        assertEquals("17", response.getId());
        verify(conviteService).convidarSeNecessario(17);
    }

    @Test
    void login_successAndFailure() {

        Usuario u = new Usuario();

        u.setId(5);
        u.setNome("User");
        u.setEmail("u@e.com");
        u.setPapel("developer");

        // Usuário já concluiu a validação do primeiro acesso.
        u.setPrimeiroAcessoValidado(true);

        BCryptPasswordEncoder enc = new BCryptPasswordEncoder();

        u.setSenha(enc.encode("secret"));

        when(usuarioRepository.findByEmail("u@e.com"))
                .thenReturn(Optional.of(u));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthRequestDTO good = new AuthRequestDTO();
        good.setEmail("u@e.com");
        good.setPassword("secret");

        var res = authService.login(good);

        assertNotNull(res);
        assertNotNull(res.getToken());
        assertEquals("User", res.getUser().getName());
        assertEquals("developer", res.getUser().getRole());

        AuthRequestDTO bad = new AuthRequestDTO();
        bad.setEmail("u@e.com");
        bad.setPassword("wrong");

        assertNull(authService.login(bad));
    }

    @Test
    void login_recoversSystemRoleFromProfileForLegacyUser() {
        Usuario usuario = new Usuario();
        usuario.setId(8);
        usuario.setNome("User");
        usuario.setEmail("legacy@example.com");
        usuario.setCargo("QA");
        usuario.setPerfilId(3);
        usuario.setSenha(new BCryptPasswordEncoder().encode("secret"));
        usuario.setPrimeiroAcessoValidado(true);

        Perfil perfil = new Perfil();
        perfil.setId(3);
        perfil.setNome("Desenvolvedor");

        when(usuarioRepository.findByEmail("legacy@example.com"))
                .thenReturn(Optional.of(usuario));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfil));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthRequestDTO request = new AuthRequestDTO();
        request.setEmail("legacy@example.com");
        request.setPassword("secret");

        var response = authService.login(request);

        assertEquals("developer", response.getUser().getRole());
        assertEquals("developer", response.getUser().getPapel());
        assertEquals("developer", usuario.getPapel());
    }

    @Test
    void logout_deveLimparTokenDeAutenticacao() {

        Usuario u = new Usuario();

        u.setId(7);
        u.setEmail("x@y.com");
        u.setAuthToken("tok123");

        when(usuarioRepository.findByAuthToken("tok123"))
                .thenReturn(Optional.of(u));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertTrue(authService.logout("tok123"));

        assertNull(u.getAuthToken());

        verify(usuarioRepository).save(u);
    }

    @Test
    void requestPasswordReset_deveGerarTokenEEnviarEmail() {

        Usuario usuario = new Usuario();

        usuario.setId(8);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("reset@e.com");

        when(usuarioRepository.findByEmail("reset@e.com"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetRequestDTO request =
                new PasswordResetRequestDTO();

        request.setEmail("reset@e.com");

        authService.requestPasswordReset(request);

        assertNotNull(usuario.getResetToken());
        assertNotNull(usuario.getResetTokenExpiry());

        assertTrue(
                usuario.getResetTokenExpiry().isAfter(LocalDateTime.now())
        );

        assertTrue(
                usuario.getResetTokenExpiry().isBefore(
                        LocalDateTime.now().plusHours(2).plusSeconds(5)
                )
        );

        verify(usuarioRepository).save(usuario);

        verify(emailService).enviarEmailRecuperacaoSenha(
                eq("reset@e.com"),
                eq("Usuario Teste"),
                contains("resetToken=")
        );

        verify(emailService, times(1))
                .enviarEmailRecuperacaoSenha(
                        anyString(),
                        anyString(),
                        anyString()
                );
    }

    @Test
    void requestPasswordReset_naoDeveEnviarEmailQuandoUsuarioNaoExiste() {

        when(usuarioRepository.findByEmail("inexistente@e.com"))
                .thenReturn(Optional.empty());

        PasswordResetRequestDTO request =
                new PasswordResetRequestDTO();

        request.setEmail("inexistente@e.com");

        assertDoesNotThrow(
                () -> authService.requestPasswordReset(request)
        );

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void requestPasswordReset_naoDeveProcessarEmailVazio() {

        PasswordResetRequestDTO request =
                new PasswordResetRequestDTO();

        request.setEmail(" ");

        assertDoesNotThrow(
                () -> authService.requestPasswordReset(request)
        );

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void requestPasswordReset_naoDeveProcessarRequisicaoNula() {

        assertDoesNotThrow(
                () -> authService.requestPasswordReset(null)
        );

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    void confirmPasswordReset_deveAlterarSenhaEInvalidarToken() {

        Usuario usuario = new Usuario();

        usuario.setId(9);
        usuario.setNome("Usuario Teste");
        usuario.setEmail("reset@e.com");
        usuario.setResetToken("token-valido");
        usuario.setResetTokenExpiry(
                LocalDateTime.now().plusHours(1)
        );

        when(usuarioRepository.findByResetToken("token-valido"))
                .thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken("token-valido");
        confirm.setNewPassword("novaSenha123");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertTrue(resultado);

        assertNull(usuario.getResetToken());
        assertNull(usuario.getResetTokenExpiry());

        BCryptPasswordEncoder encoder =
                new BCryptPasswordEncoder();

        assertTrue(
                encoder.matches(
                        "novaSenha123",
                        usuario.getSenha()
                )
        );

        verify(usuarioRepository).save(usuario);
    }

    @Test
    void confirmPasswordReset_deveRejeitarTokenExpirado() {

        Usuario usuario = new Usuario();

        usuario.setId(10);
        usuario.setResetToken("token-expirado");
        usuario.setResetTokenExpiry(
                LocalDateTime.now().minusHours(1)
        );

        when(usuarioRepository.findByResetToken("token-expirado"))
                .thenReturn(Optional.of(usuario));

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken("token-expirado");
        confirm.setNewPassword("novaSenha123");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertFalse(resultado);

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void confirmPasswordReset_deveRejeitarTokenInexistente() {

        when(usuarioRepository.findByResetToken("token-invalido"))
                .thenReturn(Optional.empty());

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken("token-invalido");
        confirm.setNewPassword("novaSenha123");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertFalse(resultado);

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void confirmPasswordReset_deveRejeitarTokenNulo() {

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken(null);
        confirm.setNewPassword("novaSenha123");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertFalse(resultado);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void confirmPasswordReset_deveRejeitarTokenVazio() {

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken(" ");
        confirm.setNewPassword("novaSenha123");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertFalse(resultado);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void confirmPasswordReset_deveRejeitarSenhaVazia() {

        PasswordResetConfirmDTO confirm =
                new PasswordResetConfirmDTO();

        confirm.setToken("token-valido");
        confirm.setNewPassword(" ");

        boolean resultado =
                authService.confirmPasswordReset(confirm);

        assertFalse(resultado);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void confirmPasswordReset_deveRejeitarRequisicaoNula() {

        boolean resultado =
                authService.confirmPasswordReset(null);

        assertFalse(resultado);

        verifyNoInteractions(usuarioRepository);
    }
}
