package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersSmokeTest {

    @Mock UsuarioService usuarioService;
    @InjectMocks UsuarioController usuarioController;

    @Mock TarefaService tarefaService;
    @InjectMocks TarefaController tarefaController;

    @Mock ProjetoService projetoService;
    @InjectMocks ProjetoController projetoController;

    @Mock SprintService sprintService;
    @InjectMocks SprintController sprintController;

    @Mock MetricsService metricsService;
    @InjectMocks MetricsController metricsController;

    @Test
    void usuarioController_create_and_list() {
        UsuarioRequestDTO req = new UsuarioRequestDTO(); req.setName("A"); req.setEmail("a@b.com");
        UsuarioResponseDTO res = new UsuarioResponseDTO(); res.setId("1"); res.setName("A");
        when(usuarioService.create(req)).thenReturn(res);
        ResponseEntity<?> r = usuarioController.createUsuario(req);
        assertEquals(201, r.getStatusCode().value());

        when(usuarioService.listAll(null)).thenReturn(List.of(res));
        ResponseEntity<?> l = usuarioController.listUsuarios(null);
        assertEquals(200, l.getStatusCode().value());
    }

    @Test
    void tarefaController_create_and_list() {
        TarefaRequestDTO req = new TarefaRequestDTO(); req.setTitle("T"); req.setProject("P"); req.setSprint("S");
        TarefaResponseDTO res = new TarefaResponseDTO(); res.setId("1"); res.setTitle("T");
        when(tarefaService.create(req)).thenReturn(res);
        ResponseEntity<?> r = tarefaController.create(req);
        assertEquals(201, r.getStatusCode().value());

        when(tarefaService.listAll(null, null, null)).thenReturn(List.of(res));
        ResponseEntity<?> l = tarefaController.list(null, null, null);
        assertEquals(200, l.getStatusCode().value());
    }

    @Test
    void projetoController_create_and_list() {
        ProjetoRequestDTO req = new ProjetoRequestDTO(); req.setName("P");
        ProjetoResponseDTO res = new ProjetoResponseDTO(); res.setId("1"); res.setName("P");
        when(projetoService.create(req)).thenReturn(res);
        ResponseEntity<?> r = projetoController.createProjeto(req);
        assertEquals(201, r.getStatusCode().value());

        when(projetoService.listAll()).thenReturn(List.of(res));
        ResponseEntity<?> l = projetoController.listProjetos();
        assertEquals(200, l.getStatusCode().value());
    }

    @Test
    void sprintController_create_and_list() {
        SprintRequestDTO req = new SprintRequestDTO(); req.setProject("P"); req.setName("S");
        com.avantt_backend.dto.SprintResponseDTO res = new com.avantt_backend.dto.SprintResponseDTO(); res.setId(2);
        when(sprintService.create(req)).thenReturn(res);
        ResponseEntity<?> r = sprintController.createSprint(req);
        assertEquals(201, r.getStatusCode().value());

        when(sprintService.listAll(null)).thenReturn(List.of(res));
        ResponseEntity<?> l = sprintController.listSprints(null);
        assertEquals(200, l.getStatusCode().value());
    }

    @Test
    void metricsController_endpoints() {
        when(metricsService.getStatusDistribution(1)).thenReturn(List.of(new com.avantt_backend.dto.MetricStatusCountDTO()));
        var r = metricsController.statusDistribution(1);
        assertEquals(200, r.getStatusCode().value());

        when(metricsService.getCompletionRate(1)).thenReturn(new com.avantt_backend.dto.MetricCompletionRateDTO());
        when(metricsService.getOverdueCount(1)).thenReturn(2);
        when(metricsService.getHoursEstimated(1)).thenReturn(10.0);
        var o = metricsController.overview(1);
        assertEquals(200, o.getStatusCode().value());

        when(metricsService.getTopOverdue(1, 10)).thenReturn(List.of(new com.avantt_backend.dto.MetricOverdueTaskDTO()));
        var t = metricsController.topOverdue(1, 10);
        assertEquals(200, t.getStatusCode().value());

        when(metricsService.getTasksByMember(1)).thenReturn(List.of(new com.avantt_backend.dto.MetricTasksByMemberDTO()));
        var m = metricsController.tasksByMember(1);
        assertEquals(200, m.getStatusCode().value());
    }
}
