package com.avantt_backend.controller;

import com.avantt_backend.entity.Perfil;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.repository.PerfilRepository;
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
class PerfilControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private PerfilRepository perfilRepository;

    @InjectMocks
    private PerfilController perfilController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(perfilController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void list_returnsPerfis() throws Exception {
        Perfil p1 = new Perfil(); p1.setId(1); p1.setNome("Admin");
        Perfil p2 = new Perfil(); p2.setId(2); p2.setNome("Colaborador");
        when(perfilRepository.findAll()).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/perfis").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].nome").value("Colaborador"));
    }
}
