package com.avantt_backend.service;

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

    @Mock
    ProjetoRepository projetoRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock
    SprintService sprintService;
    @Mock
    TarefaService tarefaService;
    @Mock
    SprintRepository sprintRepository;
    @Mock
    TarefaRepository tarefaRepository;

    @InjectMocks
    ProjetoService projetoService;

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
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setTeam(List.of(10, 11));

        when(projetoRepository.save(any())).thenAnswer(invocation -> {
            Projeto pArg = invocation.getArgument(0);
            pArg.setId(100);
            return pArg;
        });

        Usuario user1 = new Usuario();
        user1.setId(10);
        user1.setNome("Jo");
        Usuario user2 = new Usuario();
        user2.setId(11);
        user2.setNome("Maria");
        when(usuarioRepository.findById(10)).thenReturn(Optional.of(user1));
        when(usuarioRepository.findById(11)).thenReturn(Optional.of(user2));

        when(projetoUsuarioRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        ProjetoResponseDTO out = projetoService.create(dto);

        assertNotNull(out);
        assertEquals("100", out.getId());
        assertEquals("P1", out.getName());
        // team should be loaded when listing but here creation saves associations; ensure save called twice
        verify(projetoUsuarioRepository, times(2)).save(any());
    }

    @Test
    void create_teamMemberNotFound_throwsApiException() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P2");
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setTeam(List.of(99));

        Projeto saved = new Projeto();
        saved.setId(200);
        saved.setName("P2");
        when(projetoRepository.save(any())).thenReturn(saved);
        when(usuarioRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(org.springframework.web.server.ResponseStatusException.class, () -> projetoService.create(dto));
    }

    @Test
    void listAll_convertsToFrontendUsingSprintAndTasksAndTeam() {
        Projeto p = new Projeto();
        p.setId(5);
        p.setName("ProjX");
        when(projetoRepository.findAll()).thenReturn(List.of(p));

        // sprintService.listAll should return one sprint with progress 100
        var sp = new com.avantt_backend.dto.SprintResponseDTO();
        sp.setProgress(100);
        sp.setDaysDelayed(0);
        when(sprintService.listAll("5")).thenReturn(List.of(sp));

        var tr = new com.avantt_backend.dto.TarefaResponseDTO();
        tr.setStatus("Concluído");
        tr.setDaysDelayed(0);
        tr.setBlockedBy("");
        when(tarefaService.listAll("5", null, null)).thenReturn(List.of(tr));

        ProjetoUsuario pu = new ProjetoUsuario();
        pu.setId(new ProjetoUsuarioId(5, 7));
        when(projetoUsuarioRepository.findByIdProjetoId(5)).thenReturn(List.of(pu));
        when(usuarioRepository.findById(7)).thenReturn(Optional.of(new Usuario() {
            {
                setNome("X");
            }
        }));

        var list = projetoService.listAll();
        assertEquals(1, list.size());
        var f = list.get(0);
        assertEquals("ProjX", f.getName());
        assertEquals(1, f.getSprints().getDone());
        assertEquals(1, f.getTasks().getDone());
        assertEquals(1, f.getTeam().size());
        // no milestones field in current DTO; verify team and aggregated fields instead
    }

}
