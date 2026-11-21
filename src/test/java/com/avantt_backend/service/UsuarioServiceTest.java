
package com.avantt_backend.service;

import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.UsuarioRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    private static final Integer ORGANIZACAO_ID = 1;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ExistenceRepository existenceRepository;

    @Mock
    private EntityManager em;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void injectEntityManager() throws Exception {
        Field field = UsuarioService.class.getDeclaredField("em");
        field.setAccessible(true);
        field.set(usuarioService, em);
    }

    private UsuarioRequestDTO buildRequest(
            String name, String email, Integer perfilId) {

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPerfilId(perfilId);
        dto.setRole("Colaborador");

        return dto;
    }

    @Test
    void create_withDuplicateEmail_throwsConflict() {

        UsuarioRequestDTO dto =
                buildRequest("Ana", "ana@example.com", 1);

        when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> usuarioService.create(dto, ORGANIZACAO_ID)
        );

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verifyNoInteractions(existenceRepository);
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void create_withValidData_savesAndReturnsDto() {

        UsuarioRequestDTO dto =
                buildRequest("Bruno Lima", "bruno@example.com", 2);

        when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);

        when(existenceRepository.existsPerfilById(dto.getPerfilId()))
                .thenReturn(true);

        when(usuarioRepository.save(any(Usuario.class)))
                .thenAnswer(invocation -> {
                    Usuario usuario = invocation.getArgument(0);

                    usuario.setId(1);

                    return usuario;
                });

        UsuarioResponseDTO res =
                usuarioService.create(dto, ORGANIZACAO_ID);

        assertNotNull(res);
        assertEquals("1", res.getId());
        assertEquals("Bruno Lima", res.getName());
        assertEquals("bruno@example.com", res.getEmail());
        assertEquals("BL", res.getAvatar());
        assertEquals("#2563eb", res.getColor());
        assertNotNull(res.getProjects());
        assertNotNull(res.getTasks());

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verify(existenceRepository).existsPerfilById(dto.getPerfilId());

        verify(usuarioRepository).save(argThat(usuario ->
                ORGANIZACAO_ID.equals(usuario.getOrganizacaoId())
                        && "Bruno Lima".equals(usuario.getNome())
                        && "bruno@example.com".equals(usuario.getEmail())
        ));

        verify(em).flush();
        verify(em).refresh(any(Usuario.class));
    }

    @Test
    void create_withMissingPerfil_throwsNotFound() {

        UsuarioRequestDTO dto =
                buildRequest("Carlos", "carlos@example.com", 99);

        when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(false);

        when(existenceRepository.existsPerfilById(dto.getPerfilId()))
                .thenReturn(false);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.create(dto, ORGANIZACAO_ID)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verify(existenceRepository).existsPerfilById(dto.getPerfilId());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void create_withoutOrganization_throwsBadRequest() {

        UsuarioRequestDTO dto =
                buildRequest("Carlos", "carlos@example.com", 2);

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.create(dto, null)
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(existenceRepository);
    }

    @Test
    void update_existingUser_updatesAndReturnsDto() {

        Usuario existing = new Usuario();
        existing.setId(5);
        existing.setNome("Marcia");
        existing.setEmail("marcia@old.com");
        existing.setCargo("Dev");

        when(usuarioRepository.findById(5))
                .thenReturn(Optional.of(existing));

        when(usuarioRepository.save(existing))
                .thenReturn(existing);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Marcia Silva");
        dto.setAvatar(null);
        dto.setColor(null);

        UsuarioResponseDTO res = usuarioService.update(5, dto);

        assertNotNull(res);
        assertEquals("5", res.getId());
        assertEquals("Marcia Silva", res.getName());
        assertEquals("MS", res.getAvatar());
        assertEquals("#2563eb", res.getColor());

        verify(usuarioRepository).findById(5);
        verify(usuarioRepository).save(existing);
    }

    @Test
    void update_nonExistingUser_throwsNotFound() {

        when(usuarioRepository.findById(99))
                .thenReturn(Optional.empty());

        UsuarioRequestDTO dto = new UsuarioRequestDTO();

        assertThrows(
                ResourceNotFoundException.class,
                () -> usuarioService.update(99, dto)
        );

        verify(usuarioRepository).findById(99);
    }

    @Test
    void update_changeEmailToExisting_throwsConflict() {

        Usuario existing = new Usuario();
        existing.setId(7);
        existing.setNome("Pedro");
        existing.setEmail("pedro@old.com");

        when(usuarioRepository.findById(7))
                .thenReturn(Optional.of(existing));

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("taken@example.com");

        when(usuarioRepository.existsByEmail(dto.getEmail()))
                .thenReturn(true);

        assertThrows(
                ConflictException.class,
                () -> usuarioService.update(7, dto)
        );

        verify(usuarioRepository).findById(7);
        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void update_success_changesFields() {

        Usuario existing = new Usuario();
        existing.setId(2);
        existing.setNome("Old Name");
        existing.setEmail("old@example.com");

        when(usuarioRepository.findById(2))
                .thenReturn(Optional.of(existing));

        when(usuarioRepository.existsByEmail("new@example.com"))
                .thenReturn(false);

        when(usuarioRepository.save(existing))
                .thenReturn(existing);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("New Name");
        dto.setEmail("new@example.com");

        UsuarioResponseDTO res = usuarioService.update(2, dto);

        assertEquals("2", res.getId());
        assertEquals("New Name", res.getName());
        assertEquals("new@example.com", res.getEmail());

        verify(usuarioRepository).findById(2);
        verify(usuarioRepository).existsByEmail("new@example.com");
        verify(usuarioRepository).save(existing);
    }

    @Test
    void update_profileFields_updatesAndReturnsCpfAndPhone() {
        Usuario existing = new Usuario();
        existing.setId(3);
        existing.setNome("Ana");
        existing.setEmail("ana@example.com");

        when(usuarioRepository.findById(3)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(existing)).thenReturn(existing);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setCpf("123.456.789-00");
        dto.setPhone("(11) 99999-1234");

        UsuarioResponseDTO response = usuarioService.update(3, dto);

        assertEquals("123.456.789-00", existing.getCpf());
        assertEquals("(11) 99999-1234", existing.getTelefone());
        assertEquals(existing.getCpf(), response.getCpf());
        assertEquals(existing.getTelefone(), response.getPhone());
    }

    @Test
    void update_profileFields_emptyValuesClearPersistedValues() {
        Usuario existing = new Usuario();
        existing.setId(4);
        existing.setNome("Ana");
        existing.setEmail("ana@example.com");
        existing.setCpf("123.456.789-00");
        existing.setTelefone("(11) 99999-1234");

        when(usuarioRepository.findById(4)).thenReturn(Optional.of(existing));
        when(usuarioRepository.save(existing)).thenReturn(existing);

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setCpf("");
        dto.setPhone("");

        UsuarioResponseDTO response = usuarioService.update(4, dto);

        assertNull(existing.getCpf());
        assertNull(existing.getTelefone());
        assertNull(response.getCpf());
        assertNull(response.getPhone());
    }
}
