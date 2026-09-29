package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
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
    private com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository;

    @Mock
    private com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository;

    @InjectMocks
    private SprintService sprintService;

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
    void create_withValidData_savesAndAssociatesTeam() {
        SprintRequestDTO dto = buildValidDto();

        Projeto proj = new Projeto(); proj.setId(1); proj.setName("P"); proj.setStartDate(LocalDate.now()); proj.setEndDate(LocalDate.now().plusDays(100));
        when(projetoRepository.findById(1)).thenReturn(Optional.of(proj));

        Status st = new Status(); st.setId(1); st.setNome("Em andamento");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(st));

        Sprint saved = new Sprint(); saved.setId(10); saved.setProjetoId(1); saved.setNome(dto.getName());
        when(sprintRepository.save(any(Sprint.class))).thenReturn(saved);

        // project members
        com.avantt_backend.entity.ProjetoUsuario pu = new com.avantt_backend.entity.ProjetoUsuario();
        com.avantt_backend.entity.ProjetoUsuarioId puid = new com.avantt_backend.entity.ProjetoUsuarioId(proj.getId(), 2);
        pu.setId(puid);
        when(projetoUsuarioRepository.findByIdProjetoId(proj.getId())).thenReturn(List.of(pu));

        when(usuarioRepository.findById(2)).thenReturn(Optional.of(new com.avantt_backend.entity.Usuario() {{ setId(2); setNome("U"); }}));

        when(sprintUsuarioRepository.existsById(any())).thenReturn(false);

        SprintResponseDTO res = sprintService.create(dto);

        assertNotNull(res);
        assertEquals(10, res.getId());

        verify(sprintRepository).save(any(Sprint.class));
        verify(sprintUsuarioRepository).save(any());
    }

    @Test
    void create_startBeforeProjectStart_throwsApiException() {
        SprintRequestDTO dto = buildValidDto();
        dto.setStartDate(LocalDate.now().minusDays(5));

        Projeto proj = new Projeto(); proj.setId(1); proj.setStartDate(LocalDate.now()); proj.setEndDate(LocalDate.now().plusDays(100));
        when(projetoRepository.findById(1)).thenReturn(Optional.of(proj));

        ApiException ex = assertThrows(ApiException.class, () -> sprintService.create(dto));
        assertTrue(ex.getMessage().contains("não pode ser anterior"));
    }

    @Test
    void create_endDateTooShort_throwsApiException() {
        SprintRequestDTO dto = buildValidDto();
        dto.setEndDate(dto.getStartDate().plusDays(10));

        Projeto proj = new Projeto(); proj.setId(1); proj.setStartDate(LocalDate.now()); proj.setEndDate(LocalDate.now().plusDays(100));
        when(projetoRepository.findById(1)).thenReturn(Optional.of(proj));

        ApiException ex = assertThrows(ApiException.class, () -> sprintService.create(dto));
        assertTrue(ex.getMessage().contains("15 dias"));
    }

    @Test
    void update_nonExisting_throwsNotFound() {
        SprintRequestDTO dto = buildValidDto();
        when(sprintRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> sprintService.update(99, dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void update_addTeam_userNotFound_throwsApiException() {
        Sprint s = new Sprint(); s.setId(5); s.setProjetoId(2); s.setDataInicio(LocalDate.now()); s.setDataFim(LocalDate.now().plusDays(20));
        when(sprintRepository.findById(5)).thenReturn(Optional.of(s));

        Projeto proj = new Projeto(); proj.setId(2); proj.setName("P"); proj.setStartDate(LocalDate.now()); proj.setEndDate(LocalDate.now().plusDays(50));
        when(projetoRepository.findById(2)).thenReturn(Optional.of(proj));

        SprintRequestDTO dto = new SprintRequestDTO(); dto.setAddTeam(List.of(99));

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());
        when(projetoUsuarioRepository.findByIdProjetoId(2)).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> sprintService.update(5, dto));
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }

    @Test
    void update_removeTeam_whenAssigned_throwsApiException() {
        Sprint s = new Sprint(); s.setId(7); s.setProjetoId(3); s.setDataInicio(LocalDate.now()); s.setDataFim(LocalDate.now().plusDays(20));
        when(sprintRepository.findById(7)).thenReturn(Optional.of(s));
        when(sprintRepository.save(any(Sprint.class))).thenReturn(s);

        Projeto proj = new Projeto(); proj.setId(3); proj.setStartDate(LocalDate.now()); proj.setEndDate(LocalDate.now().plusDays(100));
        when(projetoRepository.findById(3)).thenReturn(Optional.of(proj));

        SprintRequestDTO dto = new SprintRequestDTO(); dto.setRemoveTeam(List.of(3));

        when(sprintUsuarioRepository.existsById(new com.avantt_backend.entity.SprintUsuarioId(7,3))).thenReturn(true);
        when(tarefaRepository.countBySprintIdAndAssignee(7, 3)).thenReturn(2);

        ApiException ex = assertThrows(ApiException.class, () -> sprintService.update(7, dto));
        assertTrue(ex.getMessage().contains("Não é possível remover"));
    }
}
