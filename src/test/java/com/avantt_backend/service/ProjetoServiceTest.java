package com.avantt_backend.service;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.Status;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjetoServiceTest {
    @Mock ProjetoRepository projetoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock SprintService sprintService;
    @Mock TarefaService tarefaService;
    @Mock SprintRepository sprintRepository;
    @Mock TarefaRepository tarefaRepository;
    @Mock StatusTarefaRepository statusTarefaRepository;
    @Mock OrganizacaoRepository organizacaoRepository;
    @Mock ClienteRepository clienteRepository;
    @InjectMocks ProjetoService projetoService;

    private ProjetoRequestDTO validRequest() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("Projeto");
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(21));
        return dto;
    }

    @Test
    void createAssociatesOnlyUsersFromOrganization() {
        ProjetoRequestDTO dto = validRequest();
        dto.setTeam(List.of(2));
        Status status = new Status();
        status.setId(1);
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(status));
        Projeto project = new Projeto();
        project.setId(10);
        project.setOrganizacaoId(7);
        when(projetoRepository.save(any(Projeto.class))).thenReturn(project);
        Usuario user = new Usuario();
        user.setId(2);
        user.setOrganizacaoId(7);
        when(usuarioRepository.findById(2)).thenReturn(Optional.of(user));

        assertNotNull(projetoService.create(dto, 7));
        verify(projetoUsuarioRepository).save(any(ProjetoUsuario.class));
    }

    @Test
    void updateRejectsProjectFromAnotherOrganization() {
        Projeto project = new Projeto();
        project.setId(5);
        project.setOrganizacaoId(9);
        when(projetoRepository.findById(5)).thenReturn(Optional.of(project));
        assertThrows(ResponseStatusException.class,
                () -> projetoService.update(5, validRequest(), 7));
        verify(projetoRepository, never()).save(any());
    }

    @Test
    void updateRejectsRemovingMemberWithAssignedTasks() {
        Projeto project = new Projeto();
        project.setId(5);
        project.setOrganizacaoId(7);
        when(projetoRepository.findById(5)).thenReturn(Optional.of(project));
        Status status = new Status();
        status.setId(1);
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(status));
        when(projetoUsuarioRepository.existsById(new ProjetoUsuarioId(5, 3))).thenReturn(true);
        when(tarefaRepository.countByProjetoIdAndAssignee(5, 3)).thenReturn(2);
        ProjetoRequestDTO dto = validRequest();
        dto.setRemoveTeam(List.of(3));

        assertThrows(ApiException.class, () -> projetoService.update(5, dto, 7));
    }
}
