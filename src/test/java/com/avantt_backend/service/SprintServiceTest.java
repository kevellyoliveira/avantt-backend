package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class SprintServiceTest {

    @Test
    public void create_shouldSaveSprintWithProjetoIdAndDates() {
        SprintRepository sprintRepo = mock(SprintRepository.class);
        ProjetoRepository projetoRepo = mock(ProjetoRepository.class);
        TarefaRepository tarefaRepo = mock(TarefaRepository.class);
        ProjetoUsuarioRepository puRepo = mock(ProjetoUsuarioRepository.class);
        UsuarioRepository usuarioRepo = mock(UsuarioRepository.class);

        SprintService svc = new SprintService(sprintRepo, projetoRepo, tarefaRepo, puRepo, usuarioRepo);

        Projeto p = new Projeto(); p.setId(10); p.setName("Plataforma de Pagamentos");
        when(projetoRepo.findByName("Plataforma de Pagamentos")).thenReturn(java.util.Optional.of(p));

        ArgumentCaptor<Sprint> cap = ArgumentCaptor.forClass(Sprint.class);
        when(sprintRepo.save(any())).thenAnswer(inv -> { Sprint s = inv.getArgument(0); s.setId(5); return s; });

        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setName("Sprint 02 - Integração");
        dto.setProject("Plataforma de Pagamentos");
        dto.setStartDate(LocalDate.of(2026,7,30));
        dto.setEndDate(LocalDate.of(2026,8,13));

        var res = svc.create(dto);

        verify(sprintRepo).save(cap.capture());
        Sprint saved = cap.getValue();
        assertEquals(10, saved.getProjetoId());
        assertEquals("Sprint 02 - Integração", saved.getNome());
        assertEquals(LocalDate.of(2026,7,30), saved.getDataInicio());
        assertEquals(LocalDate.of(2026,8,13), saved.getDataFim());
        assertEquals("Planejada", saved.getStatus());

        assertNotNull(res);
        assertEquals(5, res.getId());
        assertEquals("Sprint 02 - Integração", res.getName());
        assertEquals("Plataforma de Pagamentos", res.getProject());
    }
}
