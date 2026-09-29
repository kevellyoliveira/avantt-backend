package com.avantt_backend.service;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.Status;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
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

    @Mock
    private ProjetoRepository projetoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ProjetoUsuarioRepository projetoUsuarioRepository;

    @Mock
    private SprintService sprintService;

    @Mock
    private TarefaService tarefaService;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private StatusTarefaRepository statusTarefaRepository;

    @Mock
    private com.avantt_backend.repository.OrganizacaoRepository organizacaoRepository;

    @Mock
    private com.avantt_backend.repository.ClienteRepository clienteRepository;

    @InjectMocks
    private ProjetoService projetoService;

    private ProjetoRequestDTO buildValidDto() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P");
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setTeam(List.of(2));
        return dto;
    }

    @BeforeEach
    void setup() {
        // default mocks can be left empty; individual tests will stub behaviors
    }

    @Test
    void create_withValidData_savesAndAssociatesTeam() {
        ProjetoRequestDTO dto = buildValidDto();

        Status st = new Status(); st.setId(1); st.setNome("Em andamento");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(st));

        Projeto saved = new Projeto();
        saved.setId(10);
        saved.setName(dto.getName());
        when(projetoRepository.save(any(Projeto.class))).thenReturn(saved);

        when(usuarioRepository.findById(2)).thenReturn(Optional.ofNullable(new com.avantt_backend.entity.Usuario() {{ setId(2); setNome("U"); }}));

        ProjetoResponseDTO res = projetoService.create(dto);

        assertNotNull(res);
        assertEquals("10", res.getId());

        verify(projetoRepository).save(any(Projeto.class));
        // should have attempted to save ProjetoUsuario for member 2
        verify(projetoUsuarioRepository).save(any(ProjetoUsuario.class));
    }

    @Test
    void create_endDateTooShort_throwsApiException() {
        ProjetoRequestDTO dto = buildValidDto();
        dto.setEndDate(dto.getStartDate().plusDays(10));

        Status st = new Status(); st.setId(1); st.setNome("Em andamento");
        when(statusTarefaRepository.findById(1)).thenReturn(Optional.of(st));

        ApiException ex = assertThrows(ApiException.class, () -> projetoService.create(dto));
        assertTrue(ex.getMessage().contains("pelo menos 15 dias"));
    }

    @Test
    void update_nonExisting_throwsNotFound() {
        ProjetoRequestDTO dto = buildValidDto();
        when(projetoRepository.findById(99)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> projetoService.update(99, dto));
        assertEquals(org.springframework.http.HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void update_addTeam_userNotFound_throwsApiException() {
        Projeto p = new Projeto(); p.setId(5);
        when(projetoRepository.findById(5)).thenReturn(Optional.of(p));

        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setAddTeam(List.of(99));

        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> projetoService.update(5, dto));
        assertTrue(ex.getMessage().contains("Usuário não encontrado"));
    }

    @Test
    void update_removeTeam_whenAssigned_throwsApiException() {
        Projeto p = new Projeto(); p.setId(7);
        when(projetoRepository.findById(7)).thenReturn(Optional.of(p));

        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setRemoveTeam(List.of(3));

        when(projetoUsuarioRepository.existsById(new ProjetoUsuarioId(7,3))).thenReturn(true);
        when(tarefaRepository.countByProjetoIdAndAssignee(7, 3)).thenReturn(2);

        ApiException ex = assertThrows(ApiException.class, () -> projetoService.update(7, dto));
        assertTrue(ex.getMessage().contains("Não é possível remover"));
    }
}
