package com.avantt_backend.controller;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.TarefaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TarefaControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private TarefaService tarefaService;

    @InjectMocks
    private com.avantt_backend.controller.TarefaController tarefaController;

    @BeforeEach
    void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(tarefaController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {
        TarefaRequestDTO req = new TarefaRequestDTO();
        req.setTitle("Fix"); req.setProjectId(1); req.setSprintId(2); req.setStatusId(1); req.setPlannedEnd(LocalDate.now().plusDays(5));

        TarefaResponseDTO resp = new TarefaResponseDTO(); resp.setId("20"); resp.setTitle("Fix");
        when(tarefaService.create(any(TarefaRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/tarefas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("20"));

        verify(tarefaService).create(any(TarefaRequestDTO.class));
    }

    @Test
    void postCreate_missingFields_shouldReturn400() throws Exception {
        TarefaRequestDTO req = new TarefaRequestDTO();
        req.setTitle("X");

        mockMvc.perform(post("/api/tarefas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(tarefaService);
    }

    @Test
    void patchAssignee_shouldReturn200() throws Exception {
        String json = "{\"assigneeId\": 3}";
        when(tarefaService.updateAssignee(eq(5), eq(3))).thenReturn(new TarefaResponseDTO());

        mockMvc.perform(patch("/api/tarefas/5/assignee")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());

        verify(tarefaService).updateAssignee(eq(5), eq(3));
    }

    @Test
    void getList_shouldReturn200() throws Exception {
        TarefaResponseDTO t1 = new TarefaResponseDTO(); t1.setId("1"); t1.setTitle("A");
        TarefaResponseDTO t2 = new TarefaResponseDTO(); t2.setId("2"); t2.setTitle("B");
        when(tarefaService.listAll(any(), any(), any())).thenReturn(List.of(t1,t2));

        mockMvc.perform(get("/api/tarefas").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(2)));

        verify(tarefaService).listAll(null, null, null);
    }
}
