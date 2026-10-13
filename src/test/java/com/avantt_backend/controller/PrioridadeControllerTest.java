package com.avantt_backend.controller;

import com.avantt_backend.dto.PrioridadeDTO;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.PrioridadeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PrioridadeControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PrioridadeService service;

    @InjectMocks
    private PrioridadeController prioridadeController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(prioridadeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsPrioridades() throws Exception {
        PrioridadeDTO p1 = new PrioridadeDTO(1, "Baixa");
        PrioridadeDTO p2 = new PrioridadeDTO(2, "Média");
        when(service.listAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/prioridades").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].nome").value("Média"));
    }
}
