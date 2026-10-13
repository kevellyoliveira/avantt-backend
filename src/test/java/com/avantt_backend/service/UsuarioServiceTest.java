package com.avantt_backend.service;

import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Field;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    ExistenceRepository existenceRepository;

    @Mock
    EntityManager em;

    @InjectMocks
    UsuarioService usuarioService;

    @BeforeEach
    void injectEntityManager() throws Exception {
        // Mockito sometimes doesn't inject @PersistenceContext fields; set it via reflection to ensure em is available.
        Field f = UsuarioService.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(usuarioService, em);
    }

    @Test
    void create_conflictWhenEmailExists() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Ana");
        dto.setEmail("a@b.com");

        when(usuarioRepository.existsByEmail("a@b.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> usuarioService.create(dto));
    }

    @Test
    void create_success_generatesInitialsAndDefaults() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Joao Silva");
        dto.setEmail("joao@example.com");

        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);

        // capture the saved entity and return it with an id
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(42);
            return u;
        });

        UsuarioResponseDTO res = usuarioService.create(dto);

        assertEquals("42", res.getId());
        assertEquals("Joao Silva", res.getName());
        assertEquals("joao@example.com", res.getEmail());
        // initials from "Joao Silva" -> JS
        assertEquals("JS", res.getAvatar());
        assertEquals("#2563eb", res.getColor());
        assertNotNull(res.getProjects());
        assertNotNull(res.getTasks());
        verify(em).flush();
        verify(em).refresh(any(Usuario.class));
    }

    @Test
    void update_conflictWhenChangingToExistingEmail() {
        Usuario u = new Usuario();
        u.setId(1);
        u.setNome("Ori");
        u.setEmail("old@example.com");

        when(usuarioRepository.findById(1)).thenReturn(Optional.of(u));

        // user tries to change email to an email that already exists
        when(usuarioRepository.existsByEmail("taken@example.com")).thenReturn(true);

        var dto = new UsuarioRequestDTO();
        dto.setEmail("taken@example.com");

        assertThrows(ConflictException.class, () -> usuarioService.update(1, dto));
    }

    @Test
    void update_success_changesFields() {
        Usuario u = new Usuario();
        u.setId(2);
        u.setNome("Old Name");
        u.setEmail("old@example.com");

        when(usuarioRepository.findById(2)).thenReturn(Optional.of(u));
        when(usuarioRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = new UsuarioRequestDTO();
        dto.setName("New Name");
        dto.setEmail("new@example.com");

        var res = usuarioService.update(2, dto);

        assertEquals("2", res.getId());
        assertEquals("New Name", res.getName());
        assertEquals("new@example.com", res.getEmail());
    private UsuarioRepository usuarioRepository;

    @Mock
    private ExistenceRepository existenceRepository;

    @Mock
    private EntityManager em;

    @InjectMocks
    private UsuarioService usuarioService;

    @BeforeEach
    void setup() throws Exception {
        // Ensure the @PersistenceContext EntityManager field is populated since Mockito
        // constructor injection doesn't set it automatically in this class.
        java.lang.reflect.Field emField = UsuarioService.class.getDeclaredField("em");
        emField.setAccessible(true);
        emField.set(usuarioService, em);
    }

    private UsuarioRequestDTO buildRequest(String name, String email, Integer perfilId) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName(name);
        dto.setEmail(email);
        dto.setPerfilId(perfilId);
        dto.setRole("Colaborador");
        return dto;
    }

    @Test
    void create_withValidData_savesAndReturnsDto() {
        UsuarioRequestDTO dto = buildRequest("Bruno Lima", "bruno@example.com", 2);

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(existenceRepository.existsPerfilById(dto.getPerfilId())).thenReturn(true);

        Usuario saved = new Usuario();
        saved.setId(1);
        saved.setNome(dto.getName());
        saved.setEmail(dto.getEmail());
        saved.setCargo(dto.getRole());

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(saved);

        UsuarioResponseDTO res = usuarioService.create(dto);

        assertNotNull(res);
        assertEquals("1", res.getId());
        assertEquals(dto.getName(), res.getName());
        // avatar should be initials "BL"
        assertEquals("BL", res.getAvatar());
        // default color when dto.color is null
        assertEquals("#2563eb", res.getColor());

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verify(existenceRepository).existsPerfilById(dto.getPerfilId());
        verify(usuarioRepository).save(any(Usuario.class));
        verify(em).flush();
        verify(em).refresh(saved);
    }

    @Test
    void create_withDuplicateEmail_throwsConflict() {
        UsuarioRequestDTO dto = buildRequest("Ana", "ana@example.com", 1);
        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> usuarioService.create(dto));

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verifyNoMoreInteractions(existenceRepository);
    }

    @Test
    void create_withMissingPerfil_throwsNotFound() {
        UsuarioRequestDTO dto = buildRequest("Carlos", "carlos@example.com", 99);
        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(false);
        when(existenceRepository.existsPerfilById(dto.getPerfilId())).thenReturn(false);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> usuarioService.create(dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());

        verify(usuarioRepository).existsByEmail(dto.getEmail());
        verify(existenceRepository).existsPerfilById(dto.getPerfilId());
    }

    @Test
    void update_existingUser_updatesAndReturnsDto() {
        Usuario existing = new Usuario();
        existing.setId(5);
        existing.setNome("Marcia");
        existing.setEmail("marcia@old.com");
        existing.setCargo("Dev");

        when(usuarioRepository.findById(5)).thenReturn(Optional.of(existing));

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Marcia Silva");
        dto.setAvatar(null);
        dto.setColor(null);

        when(usuarioRepository.save(existing)).thenReturn(existing);

        UsuarioResponseDTO res = usuarioService.update(5, dto);

        assertNotNull(res);
        assertEquals("5", res.getId());
        assertEquals("Marcia Silva", res.getName());
        // initials for "Marcia Silva" => MS
        assertEquals("MS", res.getAvatar());
        assertEquals("#2563eb", res.getColor());

        verify(usuarioRepository).findById(5);
        verify(usuarioRepository).save(existing);
    }

    @Test
    void update_nonExistingUser_throwsNotFound() {
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        UsuarioRequestDTO dto = new UsuarioRequestDTO();

        assertThrows(ResourceNotFoundException.class, () -> usuarioService.update(99, dto));

        verify(usuarioRepository).findById(99);
    }

    @Test
    void update_changeEmailToExisting_throwsConflict() {
        Usuario existing = new Usuario();
        existing.setId(7);
        existing.setNome("Pedro");
        existing.setEmail("pedro@old.com");

        when(usuarioRepository.findById(7)).thenReturn(Optional.of(existing));

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setEmail("taken@example.com");

        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);

        assertThrows(ConflictException.class, () -> usuarioService.update(7, dto));

        verify(usuarioRepository).findById(7);
        verify(usuarioRepository).existsByEmail(dto.getEmail());
    }
}
