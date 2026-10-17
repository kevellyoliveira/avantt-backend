package com.avantt_backend.service;

import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.repository.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {
    @Mock UsuarioRepository usuarioRepository;
    @Mock ExistenceRepository existenceRepository;
    @Mock EntityManager em;
    @Mock ProjetoRepository projetoRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock PerfilRepository perfilRepository;
    @InjectMocks UsuarioService usuarioService;

    @Test
    void createRejectsExistingEmail() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Ana");
        dto.setEmail("ana@example.com");
        when(usuarioRepository.existsByEmail(dto.getEmail())).thenReturn(true);
        assertThrows(ConflictException.class, () -> usuarioService.create(dto, 7));
    }

    @Test
    void updateRejectsUserFromAnotherOrganization() {
        Usuario user = new Usuario();
        user.setId(2);
        user.setOrganizacaoId(9);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(user));
        assertThrows(com.avantt_backend.exception.ResourceNotFoundException.class,
                () -> usuarioService.update(2, new UsuarioRequestDTO(), 7));
    }
}
