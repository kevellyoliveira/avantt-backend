package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.*;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    TarefaRepository tarefaRepository;
    @Mock
    ProjetoRepository projetoRepository;
    @Mock
    SprintRepository sprintRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    PrioridadeRepository prioridadeRepository;
    @Mock
    StatusTarefaRepository statusTarefaRepository;

    @InjectMocks
    TarefaService tarefaService;

    @Test
    void create_throwsWhenNoTitle() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        assertThrows(IllegalArgumentException.class, () -> tarefaService.create(dto));
    }

    @Test
    void create_throwsWhenProjectNotFound() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("T1");
        dto.setProject("P1");
        when(projetoRepository.findByName("P1")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> tarefaService.create(dto));
    }

    @Test
    void create_success_basicFlow() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("Task title");
        dto.setProject("MyProj");
        dto.setSprint("S1");
        dto.setPriority(null);
        dto.setStatus(null);
        dto.setTags(List.of("a","b"));

        Projeto p = new Projeto(); p.setId(7); p.setName("MyProj");
        Sprint s = new Sprint(); s.setId(9); s.setNome("S1");

        when(projetoRepository.findByName("MyProj")).thenReturn(Optional.of(p));
        when(sprintRepository.findByNome("S1")).thenReturn(Optional.of(s));

        when(tarefaRepository.save(any(Tarefa.class))).thenAnswer(invocation -> {
            Tarefa t = invocation.getArgument(0);
            t.setId(55);
            return t;
        });

        TarefaResponseDTO res = tarefaService.create(dto);

        assertEquals("55", res.getId());
        assertEquals("Task title", res.getTitle());
        assertEquals("MyProj", res.getProject());
        assertEquals("S1", res.getSprint());
        assertEquals("média", res.getPriority()); // default
        assertEquals("planejada", res.getStatus()); // default
        assertEquals(2, res.getTags().size());
    }

    @Test
    void listAll_filtersByNumericProjectIdAndStatusIdResolution() {
        Tarefa t1 = new Tarefa(); t1.setId(1); t1.setProjetoId(10); t1.setStatus(null); t1.setStatusId(3);
        Tarefa t2 = new Tarefa(); t2.setId(2); t2.setProjetoId(20); t2.setStatus("planejada");

        when(tarefaRepository.findAll()).thenReturn(List.of(t1, t2));

        // statusTarefaRepository should resolve id=3 -> "done"
        StatusTarefa st = new StatusTarefa(); st.setId(3); st.setNome("done");
        when(statusTarefaRepository.findById(3)).thenReturn(Optional.of(st));

        var results = tarefaService.listAll("10", null, "done");
        assertEquals(1, results.size());
        assertEquals("1", results.get(0).getId());
        assertEquals("concluída", results.get(0).getStatus());
    }
}
