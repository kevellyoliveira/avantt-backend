package com.avantt_backend.controller;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.ProjetoService;
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
class ProjetoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private ProjetoService projetoService;

    @InjectMocks
    private ProjetoController projetoController;

    @BeforeEach
    void setup() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(projetoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {
        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(21).toString();
        String json = "{\"name\": \"Portal\", \"statusId\": 1, \"startDate\": \"" + start + "\", \"endDate\": \"" + end + "\"}";

        ProjetoResponseDTO resp = new ProjetoResponseDTO();
        resp.setId("10"); resp.setName("Portal");
        when(projetoService.create(any(ProjetoRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("10"));

        verify(projetoService).create(any(ProjetoRequestDTO.class));
    }

    @Test
    void postCreate_missingRequiredFields_shouldReturn400() throws Exception {
        ProjetoRequestDTO req = new ProjetoRequestDTO();
        req.setName("X");
        // missing statusId and dates

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(projetoService);
    }

    @Test
    void putUpdate_existing_shouldReturn200() throws Exception {
        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(21).toString();
        String json = "{\"name\": \"Updated\", \"statusId\": 1, \"startDate\": \"" + start + "\", \"endDate\": \"" + end + "\"}";

        ProjetoResponseDTO resp = new ProjetoResponseDTO();
        resp.setId("5"); resp.setName("Updated");
        when(projetoService.update(eq(5), any(ProjetoRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/projetos/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"));

        verify(projetoService).update(eq(5), any(ProjetoRequestDTO.class));
    }

    @Test
    void getList_empty_shouldReturn404() throws Exception {
        when(projetoService.listAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/projetos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verify(projetoService).listAll();
    }

    @Test
    void postCreate_businessConflict_shouldReturn409() throws Exception {
        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(21).toString();
        String json = "{\"name\": \"Dup\", \"statusId\": 1, \"startDate\": \"" + start + "\", \"endDate\": \"" + end + "\"}";

        when(projetoService.create(any(ProjetoRequestDTO.class))).thenThrow(new com.avantt_backend.exception.ConflictException("Conflito"));

        mockMvc.perform(post("/api/projetos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        verify(projetoService).create(any(ProjetoRequestDTO.class));
    }
}
