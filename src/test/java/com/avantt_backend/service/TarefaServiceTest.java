package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class TarefaServiceTest {

    @Test
    public void create_shouldPersistNomeAndProjetoAndSprintAndAssignee() {
        TarefaRepository tarefaRepo = mock(TarefaRepository.class);
        ProjetoRepository projetoRepo = mock(ProjetoRepository.class);
        SprintRepository sprintRepo = mock(SprintRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);

        TarefaService svc = new TarefaService(tarefaRepo, projetoRepo, sprintRepo, usuarioRepo);

        Projeto p = new Projeto(); p.setId(1); p.setName("Portal Corporativo");
        when(projetoRepo.findByName("Portal Corporativo")).thenReturn(java.util.Optional.of(p));

        Sprint s = new Sprint(); s.setId(5); s.setNome("Sprint 01 - MVP");
        when(sprintRepo.findByNome("Sprint 01 - MVP")).thenReturn(java.util.Optional.of(s));

        Usuario u = new Usuario(); u.setId(7); u.setNome("João Silva");
        when(usuarioRepo.findByNomeIgnoreCase("João Silva")).thenReturn(java.util.Optional.of(u));

        when(tarefaRepo.save(any())).thenAnswer(inv -> { Tarefa t = inv.getArgument(0); t.setId(1); return t; });

        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("Criar tela de login");
        dto.setProject("Portal Corporativo");
        dto.setSprint("Sprint 01 - MVP");
        dto.setAssignee("João Silva");
        dto.setAvatar("JS");
        dto.setAvatarColor("#2563eb");
        dto.setPriority("alta");
        dto.setStatus("planejada");
        dto.setDaysDelayed(0);
        dto.setPlannedEnd(LocalDate.of(2026,9,20));
        dto.setEstimatedHours(8);
        dto.setBlockedBy(null);
        dto.setTags(java.util.Collections.emptyList());

        var res = svc.create(dto);

        ArgumentCaptor<Tarefa> cap = ArgumentCaptor.forClass(Tarefa.class);
        verify(tarefaRepo).save(cap.capture());
        Tarefa saved = cap.getValue();
        assertEquals("Criar tela de login", saved.getNome());
        assertEquals("Criar tela de login", saved.getTitulo());
        assertEquals(1, saved.getProjetoId());
        assertEquals(5, saved.getSprintId());
        assertEquals(7, saved.getAtribuidoPara());

        assertNotNull(res);
        assertEquals("1", res.getId());
        assertEquals("Criar tela de login", res.getTitle());
        assertEquals("Portal Corporativo", res.getProject());
        assertEquals("Sprint 01 - MVP", res.getSprint());
        assertEquals("João Silva", res.getAssignee());
    }
}
