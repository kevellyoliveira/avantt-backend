package com.avantt_backend.controller;

import com.avantt_backend.dto.ClienteRequestDTO;
import com.avantt_backend.dto.ClienteResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClienteService clienteService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private ClienteController clienteController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(clienteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getClients_shouldReturnOnlyAuthenticatedOrganizationClients() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(21);
        usuario.setOrganizacaoId(8);
        when(authService.obterUsuarioAutenticado("valid-token")).thenReturn(usuario);

        ClienteResponseDTO cliente = new ClienteResponseDTO();
        cliente.setId(5);
        cliente.setName("Cliente da organização");
        when(clienteService.listAll(8)).thenReturn(List.of(cliente));

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5));

        verify(clienteService).listAll(8);
    }

    @Test
    void getClients_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(authService, clienteService);
    }

    @Test
    void postClient_mapsPhoneFromPortugueseRequestField() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(21);
        usuario.setOrganizacaoId(8);
        when(authService.obterUsuarioAutenticado("valid-token")).thenReturn(usuario);

        ClienteResponseDTO response = new ClienteResponseDTO();
        response.setId(5);
        response.setName("Novo cliente");
        response.setPhone("(11) 99999-1111");
        when(clienteService.create(any(ClienteRequestDTO.class), eq(8)))
                .thenReturn(response);

        mockMvc.perform(post("/api/clientes")
                        .header("Authorization", "Bearer valid-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo cliente\",\"telefone\":\"(11) 99999-1111\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.phone").value("(11) 99999-1111"));

        ArgumentCaptor<ClienteRequestDTO> requestCaptor =
                ArgumentCaptor.forClass(ClienteRequestDTO.class);
        verify(clienteService).create(requestCaptor.capture(), eq(8));
        assertEquals("(11) 99999-1111", requestCaptor.getValue().getPhone());
    }
}
