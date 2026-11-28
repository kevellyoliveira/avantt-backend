package com.avantt_backend.service;

import com.avantt_backend.dto.AceitarConviteRequestDTO;
import com.avantt_backend.entity.Perfil;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.PerfilRepository;
import com.avantt_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConviteServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private ConviteService conviteService;

    @Test
    void aceitarConvite_returnsDeveloperRoleFromSavedProfile() throws Exception {
        assertRoleFromProfile("Desenvolvedor", "developer");
    }

    @Test
    void aceitarConvite_returnsManagerRoleFromSavedProfile() throws Exception {
        assertRoleFromProfile("Gerente", "manager");
    }

    private void assertRoleFromProfile(
            String profileName,
            String expectedRole) throws Exception {
        String code = "123456";
        Usuario usuario = new Usuario();
        usuario.setId(7);
        usuario.setNome("Ana");
        usuario.setEmail("ana@example.com");
        usuario.setCargo("QA");
        usuario.setPerfilId(3);
        usuario.setSenha(new BCryptPasswordEncoder().encode("password"));
        usuario.setPrimeiroAcessoValidado(false);
        usuario.setConviteTokenHash(hashCode(code));
        usuario.setConviteTokenExpiry(LocalDateTime.now().plusMinutes(10));

        Perfil perfil = new Perfil();
        perfil.setId(3);
        perfil.setNome(profileName);

        when(usuarioRepository.findByEmail("ana@example.com"))
                .thenReturn(Optional.of(usuario));
        when(perfilRepository.findById(3)).thenReturn(Optional.of(perfil));
        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AceitarConviteRequestDTO request = new AceitarConviteRequestDTO();
        request.setEmail("ana@example.com");
        request.setPassword("password");
        request.setCodigo(code);

        var response = conviteService.aceitarConvite(request);

        assertEquals(expectedRole, response.getUser().getRole());
        assertEquals(expectedRole, response.getUser().getPapel());
        assertEquals(expectedRole, usuario.getPapel());
        verify(perfilRepository).findById(3);
    }

    private String hashCode(String code) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return HexFormat.of().formatHex(
                digest.digest(code.getBytes(StandardCharsets.UTF_8))
        );
    }
}
