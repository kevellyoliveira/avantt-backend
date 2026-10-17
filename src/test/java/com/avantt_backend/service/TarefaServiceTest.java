package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.entity.Status;
import com.avantt_backend.repository.*;
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
class TarefaServiceTest {
    @Mock TarefaRepository tarefaRepository;
    @Mock ProjetoRepository projetoRepository;
    @Mock SprintRepository sprintRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PrioridadeRepository prioridadeRepository;
    @Mock StatusTarefaRepository statusTarefaRepository;
    @Mock SprintService sprintService;
    @Mock SprintUsuarioRepository sprintUsuarioRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock TagRepository tagRepository;
    @Mock TarefaTagRepository tarefaTagRepository;
    @Mock TarefaUsuarioRepository tarefaUsuarioRepository;
    @InjectMocks TarefaService tarefaService;

    @Test
    void updateRejectsTaskFromAnotherOrganization() {
        Tarefa task = new Tarefa();
        task.setId(4);
        task.setProjetoId(10);
        when(tarefaRepository.findById(4)).thenReturn(Optional.of(task));
        Projeto project = new Projeto();
        project.setId(10);
        project.setOrganizacaoId(9);
        lenient().when(projetoRepository.findById(10)).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class,
                () -> tarefaService.updateStatus(4, 1, 7));
    }

    @Test
    void updateAssigneeRejectsTaskFromAnotherOrganization() {
        Tarefa task = new Tarefa();
        task.setId(4);
        task.setProjetoId(10);
        when(tarefaRepository.findById(4)).thenReturn(Optional.of(task));
        Projeto project = new Projeto();
        project.setId(10);
        project.setOrganizacaoId(9);
        when(projetoRepository.findById(10)).thenReturn(Optional.of(project));

        assertThrows(ResponseStatusException.class,
                () -> tarefaService.updateAssignee(4, 2, 7));
    }

    @Test
    void createAllowsBacklogWithoutSprint() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("Backlog");
        dto.setProjectId(10);
        dto.setStatusId(1);
        Projeto project = new Projeto();
        project.setId(10);
        project.setName("Projeto");
        project.setOrganizacaoId(7);
        when(projetoRepository.findById(10)).thenReturn(Optional.of(project));
        Status status = new Status();
        status.setId(1);
        status.setNome("Em andamento");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(status));
        Tarefa saved = new Tarefa();
        saved.setId(8);
        saved.setProjetoId(10);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(saved);

        assertNotNull(tarefaService.create(dto, 7));
        verify(tarefaRepository).save(any(Tarefa.class));
    }
}
