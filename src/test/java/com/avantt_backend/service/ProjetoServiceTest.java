package com.avantt_backend.service;

import com.avantt_backend.dto.MilestoneDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.entity.*;
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

    @Mock ProjetoRepository projetoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock MarcoProjetoRepository marcoProjetoRepository;
    @Mock SprintService sprintService;
    @Mock TarefaService tarefaService;
    @Mock SprintRepository sprintRepository;
    @Mock TarefaRepository tarefaRepository;

    @InjectMocks ProjetoService projetoService;

    @BeforeEach
    void setup() {
        // nothing
    }

    @Test
    void create_success_withTeamAndMilestones() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P1");
        dto.setDescription("desc");
        dto.setColor("#fff");
        dto.setStatus("planned");
        dto.setRisks(List.of("r1","r2"));
        dto.setTeam(List.of("10","Maria"));
        MilestoneDTO m = new MilestoneDTO(); m.setName("M1"); m.setDate(LocalDate.of(2023,1,1)); m.setDone(true);
        dto.setMilestones(List.of(m));

        when(projetoRepository.save(any())).thenAnswer(invocation -> {
            Projeto pArg = invocation.getArgument(0);
            pArg.setId(100);
            return pArg;
        });

        Usuario user = new Usuario(); user.setId(10); user.setNome("Jo");
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(user));
        when(usuarioRepository.findByNomeIgnoreCase("Maria")).thenReturn(Optional.of(new Usuario(){ { setId(11); setNome("Maria"); } }));

        when(projetoUsuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(marcoProjetoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProjetoResponseDTO out = projetoService.create(dto);

        assertNotNull(out);
        assertEquals("100", out.getId());
        assertEquals("P1", out.getName());
        assertTrue(out.getRisks().contains("r1"));
        // team should be loaded when listing but here creation saves associations; ensure save called twice
        verify(projetoUsuarioRepository, times(2)).save(any());
        verify(marcoProjetoRepository, times(1)).save(any());
    }

    @Test
    void create_teamMemberNotFound_throwsApiException() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P2");
        dto.setTeam(List.of("notfound"));

        Projeto saved = new Projeto(); saved.setId(200); saved.setName("P2");
        when(projetoRepository.save(any())).thenReturn(saved);
        when(usuarioRepository.findByNomeIgnoreCase("notfound")).thenReturn(Optional.empty());

        assertThrows(ApiException.class, () -> projetoService.create(dto));
    }

    @Test
    void listAll_convertsToFrontendUsingSprintAndTasksAndTeam() {
        Projeto p = new Projeto(); p.setId(5); p.setName("ProjX"); p.setRisks("a; b");
        when(projetoRepository.findAll()).thenReturn(List.of(p));

        // sprintService.listAll should return one sprint with progress 100
        var sp = new com.avantt_backend.dto.SprintResponseDTO(); sp.setProgress(100); sp.setDaysDelayed(0);
        when(sprintService.listAll("5")).thenReturn(List.of(sp));

        var tr = new com.avantt_backend.dto.TarefaResponseDTO(); tr.setStatus("Concluído"); tr.setDaysDelayed(0); tr.setBlockedBy("");
        when(tarefaService.listAll("5", null, null)).thenReturn(List.of(tr));

        ProjetoUsuario pu = new ProjetoUsuario(); pu.setId(new ProjetoUsuarioId(5, 7));
        when(projetoUsuarioRepository.findByIdProjetoId(5)).thenReturn(List.of(pu));
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(new Usuario(){ { setNome("X"); } }));

        when(marcoProjetoRepository.findByProjetoId(5)).thenReturn(List.of(new MarcoProjeto(){ { setNome("M"); setData(LocalDate.now()); setConcluido(true); } }));

        var list = projetoService.listAll();
        assertEquals(1, list.size());
        var f = list.get(0);
        assertEquals("ProjX", f.getName());
        assertEquals(1, f.getSprints().getDone());
        assertEquals(1, f.getTasks().getDone());
        assertEquals(1, f.getTeam().size());
        assertEquals(1, f.getMilestones().size());
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
