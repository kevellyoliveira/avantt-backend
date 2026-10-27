package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Status;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ExistenceRepository;
import com.avantt_backend.repository.PrioridadeRepository;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.StatusTarefaRepository;
import com.avantt_backend.repository.TagRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.TarefaTagRepository;
import com.avantt_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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
    private UsuarioRepository usuarioRepository;

    @Mock
    private PrioridadeRepository prioridadeRepository;

    @Mock
    private StatusTarefaRepository statusTarefaRepository;

    @Mock
    private SprintService sprintService;

    @Mock
    private SprintUsuarioRepository sprintUsuarioRepository;

    @Mock
    private ProjetoUsuarioRepository projetoUsuarioRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TarefaTagRepository tarefaTagRepository;

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
    void create_throwsWhenNoTitle() {
        TarefaRequestDTO dto = new TarefaRequestDTO();

        assertThrows(
                IllegalArgumentException.class,
                () -> tarefaService.create(dto)
        );
    }

    @Test
    void create_throwsWhenProjectNotFound() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("T1");
        dto.setProjectId(1);

        when(projetoRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> tarefaService.create(dto)
        );
    }

    @Test
    void create_withValidData_savesAndSchedulesRecalc() {
        TarefaRequestDTO dto = buildValidDto();

        Projeto projeto = new Projeto();
        projeto.setId(1);
        projeto.setName("P");

        when(projetoRepository.findById(1))
                .thenReturn(Optional.of(projeto));

        Sprint sprint = new Sprint();
        sprint.setId(2);
        sprint.setNome("S");
        sprint.setDataFim(LocalDate.now().plusDays(10));

        when(sprintRepository.findById(2))
                .thenReturn(Optional.of(sprint));

        Status status = new Status();
        status.setId(1);
        status.setNome("To Do");

        when(statusTarefaRepository.findById(1))
                .thenReturn(Optional.of(status));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenAnswer(invocation -> {
                    Tarefa tarefa = invocation.getArgument(0);
                    tarefa.setId(100);
                    tarefa.setSprintId(2);
                    return tarefa;
                });

        when(tagRepository.findById(1))
                .thenReturn(Optional.of(
                        new com.avantt_backend.entity.Tag()
                ));

        doNothing().when(tarefaTagRepository)
                .deleteByIdTarefaId(100);

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

        Projeto projeto = new Projeto();
        projeto.setId(1);
        projeto.setName("P");

        when(projetoRepository.findById(1))
                .thenReturn(Optional.of(projeto));

        Sprint sprint = new Sprint();
        sprint.setId(2);
        sprint.setNome("S");
        sprint.setDataFim(LocalDate.now().plusDays(1));

        when(sprintRepository.findById(2))
                .thenReturn(Optional.of(sprint));

        Status status = new Status();
        status.setId(1);
        status.setNome("To Do");

        when(statusTarefaRepository.findById(1))
                .thenReturn(Optional.of(status));

        dto.setPlannedEnd(LocalDate.now().plusDays(5));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> tarefaService.create(dto)
        );

        assertTrue(
                ex.getMessage().contains("data de fim da sprint")
        );
    }

    @Test
    void update_nonExisting_throwsNotFound() {
        TarefaRequestDTO dto = buildValidDto();

        when(tarefaRepository.findById(99))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> tarefaService.update(99, dto)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void updateStatus_callsRecalcAndReturns() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(5);
        tarefa.setSprintId(2);
        tarefa.setStatusId(1);

        when(tarefaRepository.findById(5))
                .thenReturn(Optional.of(tarefa));

        Status status = new Status();
        status.setId(2);
        status.setNome("Done");

        when(statusTarefaRepository.findById(2))
                .thenReturn(Optional.of(status));

        when(tarefaRepository.save(any(Tarefa.class)))
                .thenReturn(tarefa);

        when(sprintService.recalculateAndPersistProgress(2))
                .thenReturn(
                        new SprintService.ProgressInfo(3, 1, 33)
                );

        TarefaResponseDTO res = tarefaService.updateStatus(5, 2);

        assertNotNull(res);
        assertEquals(33, res.getSprintProgress());

        verify(tarefaRepository).flush();
        verify(sprintService).recalculateAndPersistProgress(2);
    }

    @Test
    void listUsersForTask_buildsUserList() {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(7);
        tarefa.setSprintId(3);

        when(tarefaRepository.findById(7))
                .thenReturn(Optional.of(tarefa));

        com.avantt_backend.entity.SprintUsuario sprintUsuario =
                new com.avantt_backend.entity.SprintUsuario();

        sprintUsuario.setId(
                new com.avantt_backend.entity.SprintUsuarioId(3, 11)
        );

        when(sprintUsuarioRepository.findByIdSprintId(3))
                .thenReturn(List.of(sprintUsuario));

        com.avantt_backend.entity.Usuario usuario =
                new com.avantt_backend.entity.Usuario();

        usuario.setId(11);
        usuario.setNome("Alice");
        usuario.setEmail("a@x");

        when(usuarioRepository.findById(11))
                .thenReturn(Optional.of(usuario));

        var list = tarefaService.listUsersForTask(7);

        assertEquals(1, list.size());
        assertEquals(11, list.get(0).get("id"));
    }
}
