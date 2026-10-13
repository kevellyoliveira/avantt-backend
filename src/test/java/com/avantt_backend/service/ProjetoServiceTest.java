package com.avantt_backend.service;

import com.avantt_backend.dto.MilestoneDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.entity.*;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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
    }
}
