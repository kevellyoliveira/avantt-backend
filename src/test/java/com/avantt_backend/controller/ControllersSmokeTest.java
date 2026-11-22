
package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.service.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersSmokeTest {

    @Mock
    UsuarioService usuarioService;

    @Mock
    AuthService authService;

    @InjectMocks
    UsuarioController usuarioController;

    @Mock
    TarefaService tarefaService;

    @InjectMocks
    TarefaController tarefaController;

    @Mock
    ProjetoService projetoService;

    @InjectMocks
    ProjetoController projetoController;

    @Mock
    SprintService sprintService;

    @InjectMocks
    SprintController sprintController;

    @Mock
    MetricsService metricsService;

    @InjectMocks
    MetricsController metricsController;

    // =========================================================
    // USUARIO CONTROLLER
    // =========================================================

    @Test
    void usuarioController_create_and_list() {

        String token = "token-smoke";
        Integer organizacaoId = 1;

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("A");
        req.setEmail("a@b.com");
        req.setPerfilId(1);

        UsuarioResponseDTO res = new UsuarioResponseDTO();
        res.setId("1");
        res.setName("A");

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(10);
        usuarioAutenticado.setNome("Administrador");
        usuarioAutenticado.setEmail("admin@example.com");
        usuarioAutenticado.setOrganizacaoId(organizacaoId);

        when(authService.obterUsuarioAutenticado(token))
                .thenReturn(usuarioAutenticado);

        when(usuarioService.create(req, organizacaoId))
                .thenReturn(res);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);

        ResponseEntity<?> r =
                usuarioController.createUsuario(req, request);

        assertEquals(201, r.getStatusCode().value());

        verify(authService).obterUsuarioAutenticado(token);
        verify(usuarioService).create(req, organizacaoId);

        when(usuarioService.listAll(null))
                .thenReturn(List.of(res));

        ResponseEntity<?> l = usuarioController.listUsuarios(null);

        assertEquals(200, l.getStatusCode().value());
    }

    // =========================================================
    // TAREFA CONTROLLER
    // =========================================================

    @Test
    void tarefaController_create_and_list() {

        TarefaRequestDTO req = new TarefaRequestDTO();
        req.setTitle("T");
        req.setProjectId(1);
        req.setSprintId(1);

        TarefaResponseDTO res = new TarefaResponseDTO();
        res.setId("1");
        res.setTitle("T");

        when(tarefaService.create(req)).thenReturn(res);

        ResponseEntity<?> r = tarefaController.create(req);

        assertEquals(201, r.getStatusCode().value());

        when(tarefaService.listAll(null, null, null))
                .thenReturn(List.of(res));

        ResponseEntity<?> l =
                tarefaController.list(null, null, null);

        assertEquals(200, l.getStatusCode().value());
    }

    // =========================================================
    // PROJETO CONTROLLER
    // =========================================================

    @Test
    void projetoController_create_and_list() {

        String token = "token-projeto-smoke";
        String authorization = "Bearer " + token;

        Integer organizacaoId = 1;
        Integer usuarioId = 10;

        ProjetoRequestDTO req = new ProjetoRequestDTO();
        req.setName("P");

        ProjetoResponseDTO res = new ProjetoResponseDTO();
        res.setId("1");
        res.setName("P");
        res.setOrganizacaoId(organizacaoId);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(usuarioId);
        usuarioAutenticado.setNome("Administrador");
        usuarioAutenticado.setEmail("admin@example.com");
        usuarioAutenticado.setOrganizacaoId(organizacaoId);

        when(authService.obterUsuarioAutenticado(token))
                .thenReturn(usuarioAutenticado);

        // Agora o método recebe também o ID do usuário criador.
        when(projetoService.create(req, organizacaoId, usuarioId))
                .thenReturn(res);

        // Cadastro
        ResponseEntity<?> r = projetoController.createProjeto(
                req,
                authorization
        );

        assertEquals(201, r.getStatusCode().value());

        verify(authService).obterUsuarioAutenticado(token);
        verify(projetoService).create(req, organizacaoId, usuarioId);

        // Listagem por organização ou membro
        when(projetoService.listarPorOrganizacaoOuMembro(
                organizacaoId,
                usuarioId
        )).thenReturn(List.of(res));

        ResponseEntity<?> l = projetoController.listProjetos(
                authorization
        );

        assertEquals(200, l.getStatusCode().value());

        verify(projetoService).listarPorOrganizacaoOuMembro(
                organizacaoId,
                usuarioId
        );

        // Busca por ID
        when(projetoService.buscarPorId(1, organizacaoId))
                .thenReturn(res);

        ResponseEntity<?> busca = projetoController.buscarProjetoPorId(
                1,
                authorization
        );

        assertEquals(200, busca.getStatusCode().value());

        verify(projetoService).buscarPorId(1, organizacaoId);
    }

    @Test
    void projetoController_update() {

        String token = "token-projeto-update";
        String authorization = "Bearer " + token;

        Integer organizacaoId = 1;
        Integer projetoId = 1;

        ProjetoRequestDTO req = new ProjetoRequestDTO();
        req.setName("Projeto atualizado");

        ProjetoResponseDTO res = new ProjetoResponseDTO();
        res.setId("1");
        res.setName("Projeto atualizado");
        res.setOrganizacaoId(organizacaoId);

        Usuario usuarioAutenticado = new Usuario();
        usuarioAutenticado.setId(10);
        usuarioAutenticado.setNome("Administrador");
        usuarioAutenticado.setEmail("admin@example.com");
        usuarioAutenticado.setOrganizacaoId(organizacaoId);

        when(authService.obterUsuarioAutenticado(token))
                .thenReturn(usuarioAutenticado);

        when(projetoService.update(
                projetoId,
                req,
                organizacaoId
        )).thenReturn(res);

        ResponseEntity<?> response = projetoController.updateProjeto(
                projetoId,
                req,
                authorization
        );

        assertEquals(200, response.getStatusCode().value());

        assertNotNull(response.getBody());

        verify(authService).obterUsuarioAutenticado(token);

        verify(projetoService).update(
                projetoId,
                req,
                organizacaoId
        );
    }

    // =========================================================
    // SPRINT CONTROLLER
    // =========================================================

    @Test
    void sprintController_create_and_list() {

        SprintRequestDTO req = new SprintRequestDTO();
        req.setProjectId(1);
        req.setName("S");

        SprintResponseDTO res = new SprintResponseDTO();
        res.setId(2);

        when(sprintService.create(req)).thenReturn(res);

        ResponseEntity<?> r = sprintController.createSprint(req);

        assertEquals(201, r.getStatusCode().value());

        when(sprintService.listAll(null))
                .thenReturn(List.of(res));

        ResponseEntity<?> l = sprintController.listSprints(null);

        assertEquals(200, l.getStatusCode().value());
    }

    // =========================================================
    // METRICS CONTROLLER
    // =========================================================

    @Test
    void metricsController_endpoints() {

        when(metricsService.getStatusDistribution(1))
                .thenReturn(List.of(new MetricStatusCountDTO()));

        var r = metricsController.statusDistribution(1);

        assertEquals(200, r.getStatusCode().value());

        when(metricsService.getCompletionRate(1))
                .thenReturn(new MetricCompletionRateDTO());

        when(metricsService.getOverdueCount(1))
                .thenReturn(2);

        when(metricsService.getHoursEstimated(1))
                .thenReturn(10.0);

        var o = metricsController.overview(1);

        assertEquals(200, o.getStatusCode().value());

        when(metricsService.getTopOverdue(1, 10))
                .thenReturn(List.of(new MetricOverdueTaskDTO()));

        var t = metricsController.topOverdue(1, 10);

        assertEquals(200, t.getStatusCode().value());

        when(metricsService.getTasksByMember(1))
                .thenReturn(List.of(new MetricTasksByMemberDTO()));

        var m = metricsController.tasksByMember(1);

        assertEquals(200, m.getStatusCode().value());
    }
}
