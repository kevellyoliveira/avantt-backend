package com.avantt_backend.controller;

import com.avantt_backend.dto.OrganizacaoRequestDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.OrganizacaoService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganizacaoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private OrganizacaoService organizacaoService;

    @InjectMocks
    private OrganizacaoController organizacaoController;

    @BeforeEach
    void setup() {
        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);
        this.mockMvc = MockMvcBuilders.standaloneSetup(organizacaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {
        OrganizacaoRequestDTO req = new OrganizacaoRequestDTO();
        req.setNomeFantasia("Org A");
        req.setCnpj("12.345.678/0001-90");

        OrganizacaoResponseDTO resp = new OrganizacaoResponseDTO();
        resp.setId("1");
        resp.setNomeFantasia("Org A");

        when(organizacaoService.create(any(OrganizacaoRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(post("/api/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.nomeFantasia").value("Org A"));

        verify(organizacaoService).create(any(OrganizacaoRequestDTO.class));
    }

    @Test
    void postCreate_missingRequiredFields_shouldReturn400() throws Exception {
        OrganizacaoRequestDTO req = new OrganizacaoRequestDTO();
        // missing nomeFantasia

        mockMvc.perform(post("/api/organizacoes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(organizacaoService);
    }

    @Test
    void putUpdate_existing_shouldReturn200() throws Exception {
        OrganizacaoRequestDTO req = new OrganizacaoRequestDTO();
        req.setNomeFantasia("Org Updated");

        OrganizacaoResponseDTO resp = new OrganizacaoResponseDTO();
        resp.setId("5");
        resp.setNomeFantasia("Org Updated");

        when(organizacaoService.update(eq(5), any(OrganizacaoRequestDTO.class))).thenReturn(resp);

        mockMvc.perform(put("/api/organizacoes/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.nomeFantasia").value("Org Updated"));

        verify(organizacaoService).update(eq(5), any(OrganizacaoRequestDTO.class));
    }

    @Test
    void putUpdate_nonExisting_shouldReturn404() throws Exception {
        OrganizacaoRequestDTO req = new OrganizacaoRequestDTO();
        req.setNomeFantasia("DoesNotExist");

        when(organizacaoService.update(eq(99), any(OrganizacaoRequestDTO.class))).thenThrow(new com.avantt_backend.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(put("/api/organizacoes/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verify(organizacaoService).update(eq(99), any(OrganizacaoRequestDTO.class));
    }

    @Test
    void putUpdate_idMismatch_shouldReturn400() throws Exception {
        OrganizacaoRequestDTO req = new OrganizacaoRequestDTO();
        req.setId(10);
        req.setNomeFantasia("Mismatch");

        mockMvc.perform(put("/api/organizacoes/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"));
    }

    @Test
    void getById_existing_shouldReturn200() throws Exception {
        OrganizacaoResponseDTO resp = new OrganizacaoResponseDTO();
        resp.setId("5");
        resp.setNomeFantasia("Org Five");

        when(organizacaoService.getById(5)).thenReturn(resp);

        mockMvc.perform(get("/api/organizacoes/5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.nomeFantasia").value("Org Five"));

        verify(organizacaoService).getById(5);
    }

    @Test
    void getById_nonExisting_shouldReturn404() throws Exception {
        when(organizacaoService.getById(99)).thenThrow(new com.avantt_backend.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/organizacoes/99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verify(organizacaoService).getById(99);
    }

    @Test
    void getById_queryParam_existing_shouldReturn200() throws Exception {
        OrganizacaoResponseDTO resp = new OrganizacaoResponseDTO();
        resp.setId("5");
        resp.setNomeFantasia("Org Five");

        when(organizacaoService.getById(5)).thenReturn(resp);

        mockMvc.perform(get("/api/organizacoes").param("id", "5").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.nomeFantasia").value("Org Five"));

        verify(organizacaoService).getById(5);
    }

    @Test
    void getById_queryParam_nonExisting_shouldReturn404() throws Exception {
        when(organizacaoService.getById(99)).thenThrow(new com.avantt_backend.exception.ResourceNotFoundException("Not found"));

        mockMvc.perform(get("/api/organizacoes").param("id", "99").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verify(organizacaoService).getById(99);
    }
}
