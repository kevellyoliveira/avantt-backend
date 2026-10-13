package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.*;
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
class SprintServiceTest {

    @Mock SprintRepository sprintRepository;
    @Mock ProjetoRepository projetoRepository;
    @Mock TarefaRepository tarefaRepository;
    @Mock ProjetoUsuarioRepository projetoUsuarioRepository;
    @Mock UsuarioRepository usuarioRepository;

    @InjectMocks SprintService sprintService;

    @Test
    void create_throwsWhenProjectNotFound() {
        SprintRequestDTO dto = new SprintRequestDTO(); dto.setProject("Nope");
        when(projetoRepository.findByName("Nope")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> sprintService.create(dto));
    }

    @Test
    void create_success_computesCountsAndProgress() {
        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setProject("P"); dto.setName("S1"); dto.setStartDate(LocalDate.now().minusDays(5)); dto.setEndDate(LocalDate.now().plusDays(5));

        Projeto p = new Projeto(); p.setId(3); p.setName("P");
        when(projetoRepository.findByName("P")).thenReturn(Optional.of(p));

        Sprint saved = new Sprint(); saved.setId(99); saved.setNome("S1"); saved.setDataInicio(dto.getStartDate()); saved.setDataFim(dto.getEndDate());
        when(sprintRepository.save(any())).thenReturn(saved);

        when(tarefaRepository.countBySprintId(99)).thenReturn(10);
        when(tarefaRepository.countBySprintIdAndStatusName(99, "Done")).thenReturn(5);

        var r = sprintService.create(dto);
        assertEquals(99, r.getId());
        assertEquals(10, r.getTotalTasks());
        assertEquals(5, r.getDoneTasks());
        assertEquals(50, r.getProgress());
    }

    @Test
    void listAll_resolvesProjectNameAndCounts() {
        Sprint s = new Sprint(); s.setId(1); s.setProjetoId(4); s.setNome("Sx"); s.setDataFim(LocalDate.now().minusDays(2));
        when(sprintRepository.findAll()).thenReturn(List.of(s));
        Projeto p = new Projeto(); p.setId(4); p.setName("Proj");
        when(projetoRepository.findById(4)).thenReturn(Optional.of(p));
        when(tarefaRepository.countBySprintId(1)).thenReturn(2);
        when(tarefaRepository.countBySprintIdAndStatusName(1, "Done")).thenReturn(1);
        when(projetoUsuarioRepository.findByIdProjetoId(4)).thenReturn(List.of());

        var list = sprintService.listAll(null);
        assertEquals(1, list.size());
        var r = list.get(0);
        assertEquals("Proj", r.getProject());
        assertEquals(2, r.getTotalTasks());
    }
}
