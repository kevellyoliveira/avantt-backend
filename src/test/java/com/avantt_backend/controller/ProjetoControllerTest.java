
package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ProjetoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProjetoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String TOKEN = "token-teste";
    private static final String AUTHORIZATION = "Bearer " + TOKEN;
    private static final Integer ORGANIZACAO_ID = 1;
    private static final Integer USUARIO_ID = 10;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ProjetoController projetoController;

    @BeforeEach
    void setup() {

        LocalValidatorFactoryBean validator =
                new LocalValidatorFactoryBean();

        validator.afterPropertiesSet();

        this.mockMvc = MockMvcBuilders.standaloneSetup(projetoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    private void configurarAutenticacao() {

        Usuario usuario = new Usuario();

        usuario.setId(USUARIO_ID);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@example.com");
        usuario.setOrganizacaoId(ORGANIZACAO_ID);

        when(authService.obterUsuarioAutenticado(TOKEN))
                .thenReturn(usuario);
    }

    private String criarJsonProjeto() {

        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(21).toString();

        return "{"
                + "\"name\":\"Portal\","
                + "\"description\":\"Projeto de teste\","
                + "\"color\":\"#FF5733\","
                + "\"status\":\"Planejada\","
                + "\"startDate\":\"" + start + "\","
                + "\"endDate\":\"" + end + "\","
                + "\"progress\":0"
                + "}";
    }

    // ==========================================
    // CADASTRAR PROJETO
    // ==========================================

    @Test
    void postCreate_shouldReturn201() throws Exception {

        configurarAutenticacao();

        ProjetoResponseDTO resp = new ProjetoResponseDTO();

        resp.setId("10");
        resp.setName("Portal");
        resp.setOrganizacaoId(ORGANIZACAO_ID);

        when(projetoService.create(
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        )).thenReturn(resp);

        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonProjeto()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("10"))
                .andExpect(jsonPath("$.name").value("Portal"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).create(
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        );
    }

    @Test
    void postCreate_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(post("/api/projetos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonProjeto()))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projetoService);
        verifyNoInteractions(authService);
    }

    @Test
    void postCreate_businessConflict_shouldReturn409() throws Exception {

        configurarAutenticacao();

        when(projetoService.create(
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        )).thenThrow(
                new com.avantt_backend.exception.ConflictException("Conflito")
        );

        mockMvc.perform(post("/api/projetos")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(criarJsonProjeto()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).create(
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        );
    }

    // ==========================================
    // ATUALIZAR PROJETO
    // ==========================================

    @Test
    void putUpdate_existing_shouldReturn200() throws Exception {

        configurarAutenticacao();

        ProjetoResponseDTO resp = new ProjetoResponseDTO();

        resp.setId("5");
        resp.setName("Updated");
        resp.setOrganizacaoId(ORGANIZACAO_ID);

        when(projetoService.update(
                eq(5),
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        )).thenReturn(resp);

        String json = "{"
                + "\"name\":\"Updated\","
                + "\"status\":\"Em andamento\""
                + "}";

        mockMvc.perform(put("/api/projetos/5")
                        .header("Authorization", AUTHORIZATION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).update(
                eq(5),
                any(ProjetoRequestDTO.class),
                eq(ORGANIZACAO_ID)
        );
    }

    @Test
    void putUpdate_withoutAuthentication_shouldReturn401() throws Exception {

        mockMvc.perform(put("/api/projetos/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated\"}"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(projetoService);
        verifyNoInteractions(authService);
    }

    // ==========================================
    // BUSCAR PROJETO POR ID
    // ==========================================

    @Test
    void getById_existing_shouldReturn200() throws Exception {

        configurarAutenticacao();

        ProjetoResponseDTO resp = new ProjetoResponseDTO();

        resp.setId("5");
        resp.setName("Portal");
        resp.setOrganizacaoId(ORGANIZACAO_ID);

        when(projetoService.buscarPorId(
                eq(5),
                eq(ORGANIZACAO_ID)
        )).thenReturn(resp);

        mockMvc.perform(get("/api/projetos/5")
                        .header("Authorization", AUTHORIZATION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.name").value("Portal"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).buscarPorId(
                eq(5),
                eq(ORGANIZACAO_ID)
        );
    }

    // ==========================================
    // LISTAR PROJETOS
    // ==========================================

    @Test
    void getList_empty_shouldReturn200WithEmptyArray() throws Exception {

        configurarAutenticacao();

        when(projetoService.listarPorOrganizacaoOuMembro(
                ORGANIZACAO_ID,
                USUARIO_ID
        )).thenReturn(List.of());

        mockMvc.perform(get("/api/projetos")
                        .header("Authorization", AUTHORIZATION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).listarPorOrganizacaoOuMembro(
                ORGANIZACAO_ID,
                USUARIO_ID
        );
    }

    @Test
    void getList_withProjects_shouldReturn200() throws Exception {

        configurarAutenticacao();

        ProjetoResponseDTO projeto = new ProjetoResponseDTO();

        projeto.setId("10");
        projeto.setName("Portal");
        projeto.setOrganizacaoId(ORGANIZACAO_ID);

        when(projetoService.listarPorOrganizacaoOuMembro(
                ORGANIZACAO_ID,
                USUARIO_ID
        )).thenReturn(List.of(projeto));

        mockMvc.perform(get("/api/projetos")
                        .header("Authorization", AUTHORIZATION)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("10"))
                .andExpect(jsonPath("$[0].name").value("Portal"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(projetoService).listarPorOrganizacaoOuMembro(
                ORGANIZACAO_ID,
                USUARIO_ID
        );
    }
}
