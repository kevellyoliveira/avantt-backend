package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Status;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;
    @Mock
    private ProjetoRepository projetoRepository;
    @Mock
    private SprintRepository sprintRepository;
    @Mock
    private com.avantt_backend.service.SprintService sprintService;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PrioridadeRepository prioridadeRepository;
    @Mock
    private StatusTarefaRepository statusTarefaRepository;
    @Mock
    private com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository;
    @Mock
    private com.avantt_backend.repository.ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock
    private com.avantt_backend.repository.TagRepository tagRepository;
    @Mock
    private com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository;

    @InjectMocks
    private TarefaService tarefaService;

    private TarefaRequestDTO buildValidDto() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("Fix login");
        dto.setProjectId(1);
        dto.setSprintId(2);
        dto.setStatusId(1);
        dto.setPlannedEnd(LocalDate.now().plusDays(5));
        dto.setTagIds(List.of(1));
        return dto;
    }

    @Test
    void create_withValidData_savesAndSchedulesRecalc() {
        TarefaRequestDTO dto = buildValidDto();

        Projeto proj = new Projeto(); proj.setId(1); proj.setName("P");
        when(projetoRepository.findById(1)).thenReturn(Optional.of(proj));

        Sprint sprint = new Sprint(); sprint.setId(2); sprint.setNome("S"); sprint.setDataFim(LocalDate.now().plusDays(10));
        when(sprintRepository.findById(2)).thenReturn(Optional.of(sprint));

        Status st = new Status(); st.setId(1); st.setNome("To Do");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(st));

        Tarefa saved = new Tarefa(); saved.setId(100); saved.setSprintId(2);
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(saved);

        org.mockito.Mockito.lenient().when(tagRepository.findById(1)).thenReturn(Optional.of(new com.avantt_backend.entity.Tag()));
        // save tags and delete no-op
        org.mockito.Mockito.lenient().doNothing().when(tarefaTagRepository).deleteByIdTarefaId(100);

        TarefaResponseDTO res = tarefaService.create(dto);

        assertNotNull(res);
        assertEquals("100", res.getId());

        verify(tarefaRepository).save(any(Tarefa.class));
        verify(tarefaTagRepository).deleteByIdTarefaId(100);
        verify(tarefaTagRepository).save(any());
    }

    @Test
    void create_plannedEndAfterSprint_throwsApiException() {
        TarefaRequestDTO dto = buildValidDto();
        Projeto proj = new Projeto(); proj.setId(1); proj.setName("P");
        when(projetoRepository.findById(1)).thenReturn(Optional.of(proj));
        Sprint sprint = new Sprint(); sprint.setId(2); sprint.setNome("S"); sprint.setDataFim(LocalDate.now().plusDays(1));
        when(sprintRepository.findById(2)).thenReturn(Optional.of(sprint));

        // ensure status exists so flow reaches plannedEnd validation
        Status st = new Status(); st.setId(1); st.setNome("To Do");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(st));

        dto.setPlannedEnd(LocalDate.now().plusDays(5));

        ApiException ex = assertThrows(ApiException.class, () -> tarefaService.create(dto));
        assertTrue(ex.getMessage().contains("data de fim da sprint"));
    }

    @Test
    void update_nonExisting_throwsNotFound() {
        TarefaRequestDTO dto = buildValidDto();
        when(tarefaRepository.findById(99)).thenReturn(Optional.empty());

        org.springframework.web.server.ResponseStatusException ex = assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> tarefaService.update(99, dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateStatus_callsRecalcAndReturns() {
        Tarefa t = new Tarefa(); t.setId(5); t.setSprintId(2); t.setStatusId(1);
        when(tarefaRepository.findById(5)).thenReturn(Optional.of(t));
        when(statusTarefaRepository.findById(2)).thenReturn(Optional.of(new Status(){ { setId(2); setNome("Done"); } }));
        when(tarefaRepository.save(any(Tarefa.class))).thenReturn(t);
        when(sprintService.recalculateAndPersistProgress(2)).thenReturn(new com.avantt_backend.service.SprintService.ProgressInfo(3,1,33));

        TarefaResponseDTO res = tarefaService.updateStatus(5, 2);
        assertNotNull(res);
        assertEquals(33, res.getSprintProgress());
        verify(tarefaRepository).flush();
        verify(sprintService).recalculateAndPersistProgress(2);
    }

    @Test
    void listUsersForTask_buildsUserList() {
        Tarefa t = new Tarefa(); t.setId(7); t.setSprintId(3);
        when(tarefaRepository.findById(7)).thenReturn(Optional.of(t));
        com.avantt_backend.entity.SprintUsuario su = new com.avantt_backend.entity.SprintUsuario();
        com.avantt_backend.entity.SprintUsuarioId sid = new com.avantt_backend.entity.SprintUsuarioId(3, 11);
        su.setId(sid);
        when(sprintUsuarioRepository.findByIdSprintId(3)).thenReturn(List.of(su));
        when(usuarioRepository.findById(11)).thenReturn(Optional.of(new com.avantt_backend.entity.Usuario(){ { setId(11); setNome("Alice"); setEmail("a@x"); } }));

        var list = tarefaService.listUsersForTask(7);
        assertEquals(1, list.size());
        assertEquals(11, list.get(0).get("id"));
    }
}
