package com.avantt_backend.controller;

import com.avantt_backend.config.AuthInterceptor;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ControllersSmokeTest {
    @Mock UsuarioService usuarioService;
    @Mock TarefaService tarefaService;
    @Mock ProjetoService projetoService;
    @Mock SprintService sprintService;
    @Mock MetricsService metricsService;
    @Mock com.avantt_backend.repository.ProjetoRepository projetoRepository;
    @Mock HttpServletRequest request;
    @InjectMocks UsuarioController usuarioController;
    @InjectMocks TarefaController tarefaController;
    @InjectMocks ProjetoController projetoController;
    @InjectMocks SprintController sprintController;
    @InjectMocks MetricsController metricsController;

    @Test
    void controllersReceiveAuthenticatedRequest() {
        Usuario user = new Usuario();
        user.setId(1);
        user.setOrganizacaoId(7);
        when(request.getAttribute(AuthInterceptor.AUTHENTICATED_USER_ATTRIBUTE)).thenReturn(user);
        when(usuarioService.listAll(null, 7)).thenReturn(java.util.List.of());
        when(projetoService.listAll(7)).thenReturn(java.util.List.of());
        when(tarefaService.listAll(null, null, null)).thenReturn(java.util.List.of());
        when(sprintService.listAll(null, 7)).thenReturn(java.util.List.of());

        org.junit.jupiter.api.Assertions.assertEquals(200, usuarioController.listUsuarios(null, request).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals(200, projetoController.listProjetos(request).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals(200, tarefaController.list(null, null, null, request).getStatusCode().value());
        org.junit.jupiter.api.Assertions.assertEquals(200, sprintController.listSprints(null, request).getStatusCode().value());
    }
}
