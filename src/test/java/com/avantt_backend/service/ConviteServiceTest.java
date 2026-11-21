package com.avantt_backend.service;

import com.avantt_backend.dto.AceitarConviteRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.UsuarioRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConviteServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConviteService conviteService;

    @Test
    void convidarUsuarioSemSenhaEnviaLinkParaDefinirSenha() {
        Usuario usuario = new Usuario();
        usuario.setId(42);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setPrimeiroAcessoValidado(false);

        when(usuarioRepository.findById(42)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        ReflectionTestUtils.setField(
                conviteService,
                "frontendUrl",
                "https://app.example/"
        );

        conviteService.convidarSeNecessario(42);

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarEmail(
                eq("joao@example.com"),
                eq("Código de primeiro acesso - AVANTT"),
                message.capture()
        );
        assertTrue(message.getValue().contains(
                "https://app.example/?convite=1&email=joao%40example.com"
        ));
        assertTrue(message.getValue().matches("(?s).*Código: \\d{6}.*"));
    }

    @Test
    void aceitarConviteDefineSenhaQuandoUsuarioAindaNaoTemUma() throws Exception {
        String code = "123456";
        Usuario usuario = new Usuario();
        usuario.setId(42);
        usuario.setNome("João Silva");
        usuario.setEmail("joao@example.com");
        usuario.setPrimeiroAcessoValidado(false);
        usuario.setConviteTokenHash(hash(code));
        usuario.setConviteTokenExpiry(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByEmailIgnoreCase("joao@example.com"))
                .thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AceitarConviteRequestDTO request = new AceitarConviteRequestDTO();
        request.setEmail("Joao@Example.com");
        request.setPassword("senha-segura-123");
        request.setCodigo(code);

        var response = conviteService.aceitarConvite(request);

        assertNotNull(response.getToken());
        assertTrue(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder()
                .matches(request.getPassword(), usuario.getSenha()));
        assertTrue(usuario.getPrimeiroAcessoValidado());
        assertNull(usuario.getConviteTokenHash());
        assertNull(usuario.getConviteTokenExpiry());
        assertNotNull(usuario.getAuthTokenExpiry());
    }

    private String hash(String value) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
