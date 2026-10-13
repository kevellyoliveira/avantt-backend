package com.avantt_backend.service;

import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.UsuarioRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import java.lang.reflect.Field;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    }
}
