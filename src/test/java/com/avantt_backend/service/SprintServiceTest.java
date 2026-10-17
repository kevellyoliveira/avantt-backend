package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Status;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SprintServiceTest {
    @Mock SprintRepository sprintRepository;
    @Mock ProjetoRepository projetoRepository;
    @Mock TarefaRepository tarefaRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock SprintUsuarioRepository sprintUsuarioRepository;
    @Mock StatusTarefaRepository statusTarefaRepository;
    @InjectMocks SprintService sprintService;

    private SprintRequestDTO validRequest() {
        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setProjectId(1);
        dto.setName("Sprint");
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setEndDate(LocalDate.now().plusDays(21));
        dto.setStatusId(1);
        return dto;
    }

    @Test
    void createRejectsProjectFromAnotherOrganization() {
        Projeto project = new Projeto();
        project.setId(1);
        project.setOrganizacaoId(9);
        when(projetoRepository.findById(1)).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class,
                () -> sprintService.create(validRequest(), 7));
        verify(sprintRepository, never()).save(any());
    }

    @Test
    void createPersistsSprintInOrganization() {
        Projeto project = new Projeto();
        project.setId(1);
        project.setOrganizacaoId(7);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusDays(60));
        when(projetoRepository.findById(1)).thenReturn(Optional.of(project));
        Status status = new Status();
        status.setId(1);
        status.setNome("Em andamento");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(status));
        Sprint saved = new Sprint();
        saved.setId(3);
        saved.setProjetoId(1);
        when(sprintRepository.save(any(Sprint.class))).thenReturn(saved);

        assertNotNull(sprintService.create(validRequest(), 7));
        verify(sprintRepository).save(any(Sprint.class));
    }

    @Test
    void updateRejectsMissingSprint() {
        when(sprintRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class,
                () -> sprintService.update(99, validRequest(), 7));
    }
}
