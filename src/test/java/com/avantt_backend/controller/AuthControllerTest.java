package com.avantt_backend.controller;

import com.avantt_backend.dto.LoginResponseDTO;
import com.avantt_backend.dto.OrganizacaoResponseDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.AuthService;
import com.avantt_backend.service.ConviteService;
import com.avantt_backend.service.OrganizacaoService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @Mock
    private OrganizacaoService organizacaoService;

    @Mock
    private ConviteService conviteService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void registerOrganizacao_returnsAuthenticatedSession() throws Exception {
        OrganizacaoResponseDTO organization = new OrganizacaoResponseDTO();
        organization.setId("42");
        organization.setName("Acme");

        UsuarioResponseDTO user = new UsuarioResponseDTO();
        user.setId("7");
        user.setName("Admin");
        user.setEmail("admin@example.com");
        user.setRole("Administrador");
        user.setOrganizacaoId(42);

        when(organizacaoService.findOrCreateByCnpj(any()))
                .thenReturn(organization);
        when(authService.register(any(), eq(42)))
                .thenReturn(user);
        when(authService.login(any()))
                .thenReturn(LoginResponseDTO.loginConcluido("session-token", user));

        mockMvc.perform(post("/api/auth/register-organizacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"name\":\"Admin\","
                                + "\"email\":\"admin@example.com\","
                                + "\"password\":\"Secure123!\","
                                + "\"organizationName\":\"Acme\","
                                + "\"cnpj\":\"12.345.678/0001-90\""
                                + "}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(
                        "Authorization",
                        "Bearer session-token"))
                .andExpect(jsonPath("$.token").value("session-token"))
                .andExpect(jsonPath("$.user.id").value("7"))
                .andExpect(jsonPath("$.organization.id").value("42"));

        verify(authService).register(any(), eq(42));
        verify(authService).login(any());
    }
}
