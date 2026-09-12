package com.avantt_backend.controller;

import com.avantt_backend.dto.*;
import com.avantt_backend.service.ProjetoService;
import com.avantt_backend.service.SprintService;
import com.avantt_backend.service.TarefaService;
import com.avantt_backend.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ControllersTest {

    private MockMvc mockMvc;
    private UsuarioService usuarioService;
    private ProjetoService projetoService;
    private SprintService sprintService;
    private TarefaService tarefaService;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        usuarioService = Mockito.mock(UsuarioService.class);
        projetoService = Mockito.mock(ProjetoService.class);
        sprintService = Mockito.mock(SprintService.class);
        tarefaService = Mockito.mock(TarefaService.class);

        UsuarioController usuarioController = new UsuarioController(usuarioService);
        ProjetoController projetoController = new ProjetoController(projetoService);
        SprintController sprintController = new SprintController(sprintService);
        TarefaController tarefaController = new TarefaController(tarefaService);

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController, projetoController, sprintController, tarefaController).build();
    }

    @Test
    void getUsuariosReturns200() throws Exception {
        Mockito.when(usuarioService.listAllForFrontend()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/usuarios")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void postUsuarioReturns201() throws Exception {
        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("João Silva");
        req.setEmail("joao@email.com");
        req.setRole("Desenvolvedor");

        UsuarioFrontendDTO resp = new UsuarioFrontendDTO();
        resp.setId("1");
        resp.setName(req.getName());
        resp.setEmail(req.getEmail());
        resp.setRole(req.getRole());

        Mockito.when(usuarioService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/usuarios").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("João Silva"));
    }

    @Test
    void patchUsuarioReturns200() throws Exception {
        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setEmail("novo@email.com");

        UsuarioFrontendDTO resp = new UsuarioFrontendDTO();
        resp.setId("1");
        resp.setEmail(req.getEmail());

        Mockito.when(usuarioService.update(anyInt(), any())).thenReturn(resp);

        mockMvc.perform(patch("/api/usuarios/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("novo@email.com"));
    }

    @Test
    void getProjetosReturns200() throws Exception {
        Mockito.when(projetoService.listAllForFrontend()).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/projetos")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void postProjetosReturns201() throws Exception {
        ProjetoRequestDTO req = new ProjetoRequestDTO();
        req.setName("Projeto Alpha");

        ProjetoFrontendDTO resp = new ProjetoFrontendDTO();
        resp.setId("1");
        resp.setName(req.getName());

        Mockito.when(projetoService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/projetos").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Projeto Alpha"));
    }

    @Test
    void getSprintsReturns200() throws Exception {
        Mockito.when(sprintService.listAll(null)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/sprints")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void postSprintReturns201() throws Exception {
        SprintRequestDTO req = new SprintRequestDTO();
        req.setName("Sprint 1");

        SprintResponseDTO resp = new SprintResponseDTO();
        resp.setId("1");
        resp.setName(req.getName());

        Mockito.when(sprintService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/sprints").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Sprint 1"));
    }

    @Test
    void getTarefasReturns200() throws Exception {
        Mockito.when(tarefaService.listAll(null, null, null)).thenReturn(new ArrayList<>());
        mockMvc.perform(get("/api/tarefas")).andExpect(status().isOk()).andExpect(content().json("[]"));
    }

    @Test
    void postTarefaReturns201() throws Exception {
        TarefaRequestDTO req = new TarefaRequestDTO();
        req.setTitle("Criar tela de login");

        TarefaResponseDTO resp = new TarefaResponseDTO();
        resp.setId("1");
        resp.setTitle(req.getTitle());

        Mockito.when(tarefaService.create(any())).thenReturn(resp);

        mockMvc.perform(post("/api/tarefas").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Criar tela de login"));
    }
}
