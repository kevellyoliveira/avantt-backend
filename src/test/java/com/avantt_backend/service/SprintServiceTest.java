
package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Status;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.StatusTarefaRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.UsuarioRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
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
class SprintServiceTest {

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private ProjetoUsuarioRepository projetoUsuarioRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private SprintUsuarioRepository sprintUsuarioRepository;

    @Mock
    private StatusTarefaRepository statusTarefaRepository;

    @InjectMocks
    private SprintService sprintService;

    @BeforeEach
    void stubSprintStatuses() {
        lenient().when(statusTarefaRepository.findById(1))
                .thenReturn(Optional.of(status(1, "Planejada")));
        lenient().when(statusTarefaRepository.findById(2))
                .thenReturn(Optional.of(status(2, "Em andamento")));
        lenient().when(statusTarefaRepository.findById(3))
                .thenReturn(Optional.of(status(3, "Concluída")));
    }

    private Status status(Integer id, String name) {
        Status status = new Status();
        status.setId(id);
        status.setNome(name);
        return status;
    }

    private SprintRequestDTO buildValidDto() {

        SprintRequestDTO dto = new SprintRequestDTO();

        dto.setProjectId(1);
        dto.setName("S1");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setStatusId(1);
        dto.setTeam(List.of(2));

        return dto;
    }

    @Test
    void create_throwsWhenProjectNotFound() {

        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setProjectId(99);
        dto.setStatusId(1);

        when(projetoRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> sprintService.create(dto)
        );
    }

    @Test
    void create_rejectsStatusOutsideSprintOptions() {
        SprintRequestDTO dto = buildValidDto();
        dto.setStatusId(4);
        when(statusTarefaRepository.findById(4))
                .thenReturn(Optional.of(status(4, "Bloqueada")));

        ApiException exception = assertThrows(
                ApiException.class,
                () -> sprintService.create(dto)
        );

        assertTrue(exception.getMessage().contains("Status de sprint inválido"));
    }

    @Test
    void create_success_computesCountsAndProgress() {

        SprintRequestDTO dto = new SprintRequestDTO();

        dto.setProjectId(3);
        dto.setName("S1");
        dto.setStatusId(2);
        dto.setStartDate(LocalDate.now().minusDays(5));
        dto.setEndDate(LocalDate.now().plusDays(15));

        Projeto projeto = new Projeto();

        projeto.setId(3);
        projeto.setName("P");

        when(projetoRepository.findById(3))
                .thenReturn(Optional.of(projeto));

        Sprint saved = new Sprint();

        saved.setId(99);
        saved.setProjetoId(3);
        saved.setNome("S1");
        saved.setDataInicio(dto.getStartDate());
        saved.setDataFim(dto.getEndDate());
        saved.setStatus("Em andamento");

        when(sprintRepository.save(any(Sprint.class)))
                .thenReturn(saved);

        when(sprintRepository.findById(99))
                .thenReturn(Optional.of(saved));

        when(tarefaRepository.findBySprintId(99))
                .thenReturn(List.of());

        when(tarefaRepository.countBySprintId(99))
                .thenReturn(10);

        when(tarefaRepository.countBySprintIdAndStatusName(99, "Done"))
                .thenReturn(5);
        when(sprintUsuarioRepository.findByIdSprintId(99))
                .thenReturn(List.of());

        SprintResponseDTO result = sprintService.create(dto);

        assertEquals(99, result.getId());
        assertEquals(10, result.getTotalTasks());
        assertEquals(5, result.getDoneTasks());
        assertEquals(50, result.getProgress());
    }

    @Test
    void listAll_resolvesProjectNameAndCounts() {

        Sprint sprint = new Sprint();

        sprint.setId(1);
        sprint.setProjetoId(4);
        sprint.setNome("Sx");
        sprint.setDataFim(LocalDate.now().minusDays(2));

        when(sprintRepository.findAll())
                .thenReturn(List.of(sprint));

        Projeto projeto = new Projeto();

        projeto.setId(4);
        projeto.setName("Proj");

        when(projetoRepository.findById(4))
                .thenReturn(Optional.of(projeto));

        when(tarefaRepository.findBySprintId(1))
                .thenReturn(List.of());

        when(tarefaRepository.countBySprintId(1))
                .thenReturn(2);

        when(tarefaRepository.countBySprintIdAndStatusName(1, "Done"))
                .thenReturn(1);

        when(sprintUsuarioRepository.findByIdSprintId(1))
                .thenReturn(List.of());

        var list = sprintService.listAll(null);

        assertEquals(1, list.size());
        assertEquals("Proj", list.get(0).getProject());
        assertEquals(2, list.get(0).getTotalTasks());
        assertEquals(1, list.get(0).getDoneTasks());
        assertEquals(50, list.get(0).getProgress());
    }

    @Test
    void create_withValidData_savesAndAssociatesTeam() {

        SprintRequestDTO dto = buildValidDto();

        Projeto projeto = new Projeto();

        projeto.setId(1);
        projeto.setName("P");
        projeto.setStartDate(LocalDate.now());
        projeto.setEndDate(LocalDate.now().plusDays(100));

        when(projetoRepository.findById(1))
                .thenReturn(Optional.of(projeto));

        Sprint saved = new Sprint();

        saved.setId(10);
        saved.setProjetoId(1);
        saved.setNome(dto.getName());
        saved.setDataInicio(dto.getStartDate());
        saved.setDataFim(dto.getEndDate());
        saved.setStatus("Planejada");

        when(sprintRepository.save(any(Sprint.class)))
                .thenReturn(saved);

        when(sprintRepository.findById(10))
                .thenReturn(Optional.of(saved));

        when(tarefaRepository.findBySprintId(10))
                .thenReturn(List.of());

        com.avantt_backend.entity.ProjetoUsuario projetoUsuario =
                new com.avantt_backend.entity.ProjetoUsuario();

        projetoUsuario.setId(
                new com.avantt_backend.entity.ProjetoUsuarioId(1, 2)
        );

        when(projetoUsuarioRepository.findByIdProjetoId(1))
                .thenReturn(List.of(projetoUsuario));

        com.avantt_backend.entity.Usuario usuario =
                new com.avantt_backend.entity.Usuario();

        usuario.setId(2);
        usuario.setNome("U");

        when(usuarioRepository.findById(2))
                .thenReturn(Optional.of(usuario));

        when(sprintUsuarioRepository.existsById(any()))
                .thenReturn(false);
        when(sprintUsuarioRepository.findByIdSprintId(10))
                .thenReturn(List.of());

        SprintResponseDTO result = sprintService.create(dto);

        assertNotNull(result);
        assertEquals(10, result.getId());

        // O serviço salva a sprint na criação e novamente ao recalcular o progresso.
        verify(sprintRepository, times(2)).save(any(Sprint.class));

        verify(sprintUsuarioRepository).save(any());
    }

    @Test
    void create_startBeforeProjectStart_throwsApiException() {

        SprintRequestDTO dto = buildValidDto();

        dto.setStartDate(LocalDate.now().minusDays(5));

        Projeto projeto = new Projeto();

        projeto.setId(1);
        projeto.setStartDate(LocalDate.now());
        projeto.setEndDate(LocalDate.now().plusDays(100));

        when(projetoRepository.findById(1))
                .thenReturn(Optional.of(projeto));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> sprintService.create(dto)
        );

        assertTrue(ex.getMessage().contains("não pode ser anterior"));
    }

    @Test
    void create_endDateTooShort_throwsApiException() {

        SprintRequestDTO dto = buildValidDto();

        dto.setEndDate(dto.getStartDate().plusDays(10));

        Projeto projeto = new Projeto();

        projeto.setId(1);
        projeto.setStartDate(LocalDate.now());
        projeto.setEndDate(LocalDate.now().plusDays(100));

        when(projetoRepository.findById(1))
                .thenReturn(Optional.of(projeto));

        ApiException ex = assertThrows(
                ApiException.class,
                () -> sprintService.create(dto)
        );

        assertTrue(ex.getMessage().contains("15 dias"));
    }

    @Test
    void update_nonExisting_throwsNotFound() {

        SprintRequestDTO dto = buildValidDto();

        when(sprintRepository.findById(99))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> sprintService.update(99, dto)
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void update_persistsGoalAndSprintStatus() {
        Sprint sprint = new Sprint();
        sprint.setId(5);
        sprint.setProjetoId(2);
        sprint.setNome("S1");
        sprint.setObjetivo("Objetivo anterior");
        sprint.setStatus("Planejada");
        sprint.setDataInicio(LocalDate.now().minusDays(30));
        sprint.setDataFim(LocalDate.now().minusDays(15));

        Projeto projeto = new Projeto();
        projeto.setId(2);
        projeto.setName("Projeto");

        when(sprintRepository.findById(5)).thenReturn(Optional.of(sprint));
        when(projetoRepository.findById(2)).thenReturn(Optional.of(projeto));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(sprint);
        when(sprintUsuarioRepository.findByIdSprintId(5)).thenReturn(List.of());
        when(tarefaRepository.findBySprintId(5)).thenReturn(List.of());

        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setGoal("Novo objetivo");
        dto.setStatusId(3);

        SprintResponseDTO result = sprintService.update(5, dto);

        assertEquals("Novo objetivo", result.getGoal());
        assertEquals("Finalizada", result.getStatus());
        assertEquals("Novo objetivo", sprint.getObjetivo());
        assertEquals("Finalizada", sprint.getStatus());
    }

    @Test
    void update_addTeam_userNotFound_throwsApiException() {

        Sprint sprint = new Sprint();

        sprint.setId(5);
        sprint.setProjetoId(2);
        sprint.setDataInicio(LocalDate.now());
        sprint.setDataFim(LocalDate.now().plusDays(20));

        when(sprintRepository.findById(5))
                .thenReturn(Optional.of(sprint));

        Projeto projeto = new Projeto();

        projeto.setId(2);
        projeto.setName("P");
        projeto.setStartDate(LocalDate.now());
        projeto.setEndDate(LocalDate.now().plusDays(50));

        when(projetoRepository.findById(2))
                .thenReturn(Optional.of(projeto));

        SprintRequestDTO dto = new SprintRequestDTO();

        dto.setStatusId(1);
        dto.setAddTeam(List.of(99));

        when(sprintRepository.save(any(Sprint.class)))
                .thenReturn(sprint);

        when(projetoUsuarioRepository.findByIdProjetoId(2))
                .thenReturn(List.of());

        when(usuarioRepository.findById(99))
                .thenReturn(Optional.empty());

        ApiException ex = assertThrows(
                ApiException.class,
                () -> sprintService.update(5, dto)
        );

        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }

    @Test
    void update_removeTeam_whenAssigned_throwsApiException() {

        Sprint sprint = new Sprint();

        sprint.setId(7);
        sprint.setProjetoId(3);
        sprint.setDataInicio(LocalDate.now());
        sprint.setDataFim(LocalDate.now().plusDays(20));

        when(sprintRepository.findById(7))
                .thenReturn(Optional.of(sprint));

        Projeto projeto = new Projeto();

        projeto.setId(3);
        projeto.setStartDate(LocalDate.now());
        projeto.setEndDate(LocalDate.now().plusDays(100));

        when(projetoRepository.findById(3))
                .thenReturn(Optional.of(projeto));

        SprintRequestDTO dto = new SprintRequestDTO();

        dto.setStatusId(1);
        dto.setRemoveTeam(List.of(3));

        when(sprintRepository.save(any(Sprint.class)))
                .thenReturn(sprint);

        when(sprintUsuarioRepository.existsById(
                new com.avantt_backend.entity.SprintUsuarioId(7, 3)
        )).thenReturn(true);

        when(tarefaRepository.countBySprintIdAndAssignee(7, 3))
                .thenReturn(2);

        ApiException ex = assertThrows(
                ApiException.class,
                () -> sprintService.update(7, dto)
        );

        assertTrue(ex.getMessage().contains("Não é possível remover"));
    }
}
