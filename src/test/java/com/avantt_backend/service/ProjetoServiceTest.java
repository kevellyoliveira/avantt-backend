package com.avantt_backend.service;

import com.avantt_backend.dto.ProjetoMarcoDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoMarco;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.repository.ClienteRepository;
import com.avantt_backend.repository.OrganizacaoRepository;
import com.avantt_backend.repository.ProjetoMarcoRepository;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.ProjetoRiscoRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.exception.ApiException;

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
    private OrganizacaoRepository organizacaoRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SprintService sprintService;

    @Mock
    private TarefaService tarefaService;

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private ProjetoMarcoRepository projetoMarcoRepository;

    @Mock
    private ProjetoRiscoRepository projetoRiscoRepository;

    @InjectMocks
    private ProjetoService projetoService;

    @Test
    void create_success_withTeamAndMilestones() {

        Integer organizacaoId = 1;

        when(organizacaoRepository.existsById(organizacaoId))
                .thenReturn(true);

        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P1");
        dto.setDescription("desc");
        dto.setColor("#fff");
        dto.setStatus("Planejada");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setTeam(List.of(10, 11));

        ProjetoMarcoDTO marcoDTO = new ProjetoMarcoDTO();
        marcoDTO.setName("Primeiro marco");
        marcoDTO.setDate(LocalDate.now().plusDays(5));
        marcoDTO.setDone(false);

        dto.setMilestones(List.of(marcoDTO));

        when(projetoRepository.save(any(Projeto.class)))
                .thenAnswer(invocation -> {
                    Projeto projeto = invocation.getArgument(0);
                    projeto.setId(100);
                    return projeto;
                });

        // Os usuários precisam existir para serem associados à equipe.
        when(usuarioRepository.existsById(10)).thenReturn(true);
        when(usuarioRepository.existsById(11)).thenReturn(true);

        Usuario user1 = new Usuario();
        user1.setId(10);
        user1.setNome("Jo");

        Usuario user2 = new Usuario();
        user2.setId(11);
        user2.setNome("Maria");

        when(usuarioRepository.findById(10))
                .thenReturn(Optional.of(user1));

        when(usuarioRepository.findById(11))
                .thenReturn(Optional.of(user2));

        when(projetoUsuarioRepository.existsById(any(ProjetoUsuarioId.class)))
                .thenReturn(false);

        when(projetoUsuarioRepository.save(any(ProjetoUsuario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ProjetoUsuario pu1 = new ProjetoUsuario();
        pu1.setId(new ProjetoUsuarioId(100, 10));

        ProjetoUsuario pu2 = new ProjetoUsuario();
        pu2.setId(new ProjetoUsuarioId(100, 11));

        when(projetoUsuarioRepository.findByIdProjetoId(100))
                .thenReturn(List.of(pu1, pu2));

        when(sprintService.listAll("100"))
                .thenReturn(List.of());

        when(tarefaService.listAll("100", null, null))
                .thenReturn(List.of());

        when(projetoMarcoRepository.findByProjeto_IdOrderByDateAsc(100))
                .thenReturn(List.of());

        when(projetoRiscoRepository.findByProjeto_Id(100))
                .thenReturn(List.of());

        when(organizacaoRepository.findById(organizacaoId))
                .thenReturn(Optional.empty());

        ProjetoResponseDTO out = projetoService.create(dto, organizacaoId);

        assertNotNull(out);
        assertEquals("100", out.getId());
        assertEquals("P1", out.getName());
        assertEquals("Planejada", out.getStatus());

        verify(projetoUsuarioRepository, times(2))
                .save(any(ProjetoUsuario.class));

        verify(projetoMarcoRepository)
                .deleteByProjeto_Id(100);

        verify(projetoMarcoRepository)
                .saveAll(anyList());
    }

    @Test
    void create_teamMemberNotFound_throwsApiException() {

        Integer organizacaoId = 1;

        when(organizacaoRepository.existsById(organizacaoId))
                .thenReturn(true);

        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P2");
        dto.setStatus("Planejada");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setTeam(List.of(99));

        Projeto saved = new Projeto();
        saved.setId(200);
        saved.setName("P2");

        when(projetoRepository.save(any(Projeto.class)))
                .thenReturn(saved);

        // O serviço utiliza existsById(), não findById().
        when(usuarioRepository.existsById(99))
                .thenReturn(false);

        ApiException exception = assertThrows(
                ApiException.class,
                () -> projetoService.create(dto, organizacaoId)
        );

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
    }

    @Test
    void listAll_convertsToFrontendUsingSprintAndTasksAndTeam() {

        Projeto projeto = new Projeto();
        projeto.setId(5);
        projeto.setName("ProjX");

        when(projetoRepository.findAll())
                .thenReturn(List.of(projeto));

        // Sprints
        SprintResponseDTO sprint = new SprintResponseDTO();
        sprint.setProgress(100);
        sprint.setDaysDelayed(0);

        when(sprintService.listAll("5"))
                .thenReturn(List.of(sprint));

        // Tarefas
        TarefaResponseDTO tarefa = new TarefaResponseDTO();
        tarefa.setStatus("Concluído");
        tarefa.setDaysDelayed(0);
        tarefa.setBlockedBy("");

        when(tarefaService.listAll("5", null, null))
                .thenReturn(List.of(tarefa));

        // Equipe
        ProjetoUsuario projetoUsuario = new ProjetoUsuario();
        projetoUsuario.setId(new ProjetoUsuarioId(5, 7));

        when(projetoUsuarioRepository.findByIdProjetoId(5))
                .thenReturn(List.of(projetoUsuario));

        Usuario usuario = new Usuario();
        usuario.setId(7);
        usuario.setNome("X");

        when(usuarioRepository.findById(7))
                .thenReturn(Optional.of(usuario));

        // Marcos
        when(projetoMarcoRepository.findByProjeto_IdOrderByDateAsc(5))
                .thenReturn(List.of());

        // Riscos
        when(projetoRiscoRepository.findByProjeto_Id(5))
                .thenReturn(List.of());

        List<ProjetoResponseDTO> list = projetoService.listAll();

        assertNotNull(list);
        assertEquals(1, list.size());

        ProjetoResponseDTO response = list.get(0);

        assertEquals("ProjX", response.getName());
        assertEquals(1, response.getSprints().getDone());
        assertEquals(1, response.getTasks().getDone());
        assertEquals(1, response.getTeam().size());
        assertEquals("X", response.getTeam().get(0));
        assertEquals(1, response.getTeamIds().size());
        assertEquals(7, response.getTeamIds().get(0));
        assertNotNull(response.getMilestones());
        assertTrue(response.getMilestones().isEmpty());
        assertNotNull(response.getRisks());
        assertTrue(response.getRisks().isEmpty());
    }
}