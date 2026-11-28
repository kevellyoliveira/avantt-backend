package com.avantt_backend.controller;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ProjetoService;
import com.avantt_backend.service.SprintService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
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
class SprintControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private SprintService sprintService;

    @Mock
    private ProjetoService projetoService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private SprintController sprintController;

    @BeforeEach
    void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(sprintController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {
        SprintRequestDTO req = new SprintRequestDTO();
        req.setProjectId(1);
        req.setName("S1");
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(20));
        req.setStatusId(1);

        SprintResponseDTO resp = new SprintResponseDTO();
        resp.setId(10); resp.setName("S1");

        when(sprintService.create(any(SprintRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/sprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));

        verify(sprintService).create(any(SprintRequestDTO.class));
    }

    @Test
    void postCreate_missingRequired_shouldReturn400() throws Exception {
        SprintRequestDTO req = new SprintRequestDTO();
        req.setName("S");
        // missing projectId/startDate/endDate/statusId

        mockMvc.perform(post("/api/sprints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(sprintService);
    }

    @Test
    void putUpdate_existing_shouldReturn200() throws Exception {
        SprintRequestDTO req = new SprintRequestDTO();
        req.setName("Updated");
        req.setProjectId(2);
        req.setStatusId(1);
        req.setStartDate(LocalDate.now());
        req.setEndDate(LocalDate.now().plusDays(20));
        req.setGoal("Atualizar planejamento");

        SprintResponseDTO resp = new SprintResponseDTO();
        resp.setId(5);
        resp.setName("Updated");
        resp.setGoal("Atualizar planejamento");

        when(sprintService.update(eq(5), any(SprintRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/sprints/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.goal").value("Atualizar planejamento"));

        verify(sprintService).update(eq(5), any(SprintRequestDTO.class));
    }

    @Test
    void getProgress_shouldReturn200() throws Exception {
        when(sprintService.recalculateAndPersistProgress(7)).thenReturn(new SprintService.ProgressInfo(3,1,33));

        mockMvc.perform(get("/api/sprints/7/progress").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progress").value(33));

        verify(sprintService).recalculateAndPersistProgress(7);
    }

    @Test
    void getSprints_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/sprints"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService, projetoService, sprintService);
    }

    @Test
    void getSprints_withoutProjects_shouldReturnEmptyList() throws Exception {
        Usuario usuario = authenticatedUser();
        when(authService.obterUsuarioAutenticado("valid-token")).thenReturn(usuario);
        when(projetoService.listarPorOrganizacaoOuMembro(8, 21)).thenReturn(List.of());

        mockMvc.perform(get("/api/sprints")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());

        verifyNoInteractions(sprintService);
    }

    @Test
    void getSprints_shouldOnlyListSprintsForAccessibleProjects() throws Exception {
        Usuario usuario = authenticatedUser();
        when(authService.obterUsuarioAutenticado("valid-token")).thenReturn(usuario);

        ProjetoResponseDTO projeto = new ProjetoResponseDTO();
        projeto.setId("17");
        projeto.setName("Projeto da organização");
        when(projetoService.listarPorOrganizacaoOuMembro(8, 21))
                .thenReturn(List.of(projeto));

        SprintResponseDTO sprint = new SprintResponseDTO();
        sprint.setId(41);
        when(sprintService.listAll("17")).thenReturn(List.of(sprint));

        mockMvc.perform(get("/api/sprints")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(41));

        verify(sprintService).listAll("17");
        verify(sprintService, never()).listAll(null);
    }

    private Usuario authenticatedUser() {
        Usuario usuario = new Usuario();
        usuario.setId(21);
        usuario.setOrganizacaoId(8);
        return usuario;
    }
}
