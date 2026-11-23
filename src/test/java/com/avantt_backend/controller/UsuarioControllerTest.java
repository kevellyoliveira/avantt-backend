
package com.avantt_backend.controller;

import com.avantt_backend.dto.UsuarioRequestDTO;
import com.avantt_backend.dto.UsuarioResponseDTO;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.ConflictException;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.service.UsuarioService;
import com.avantt_backend.service.AuthService;

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

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private static final String TOKEN = "token-teste";
    private static final Integer ORGANIZACAO_ID = 1;

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setup() {

        org.springframework.http.converter.json.MappingJackson2HttpMessageConverter converter =
                new org.springframework.http.converter.json.MappingJackson2HttpMessageConverter(objectMapper);

        this.mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    private Usuario criarUsuarioAutenticado() {

        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setNome("Administrador");
        usuario.setEmail("admin@example.com");
        usuario.setOrganizacaoId(ORGANIZACAO_ID);

        return usuario;
    }

    private void configurarAutenticacao() {

        when(authService.obterUsuarioAutenticado(TOKEN))
                .thenReturn(criarUsuarioAutenticado());
    }

    @Test
    void postCreate_shouldReturn201() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("Bruno");
        req.setEmail("bruno@example.com");
        req.setPerfilId(2);

        UsuarioResponseDTO resp = new UsuarioResponseDTO();
        resp.setId("1");
        resp.setName("Bruno");
        resp.setEmail("bruno@example.com");

        configurarAutenticacao();

        when(usuarioService.create(any(UsuarioRequestDTO.class), eq(ORGANIZACAO_ID)))
                .thenReturn(resp);

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Bruno"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(usuarioService).create(
                any(UsuarioRequestDTO.class),
                eq(ORGANIZACAO_ID));
    }

    @Test
    void postCreate_missingRequiredFields_shouldReturn400() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("NoPerfil");
        req.setEmail("noperfil@example.com");

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(usuarioService);
        verifyNoInteractions(authService);
    }

    @Test
    void postCreate_withoutToken_shouldReturn401() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("Bruno");
        req.setEmail("bruno@example.com");
        req.setPerfilId(2);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(usuarioService);
        verifyNoInteractions(authService);
    }

    @Test
    void getList_shouldReturn200AndList() throws Exception {

        UsuarioResponseDTO u1 = new UsuarioResponseDTO();
        u1.setId("1");
        u1.setName("A");

        UsuarioResponseDTO u2 = new UsuarioResponseDTO();
        u2.setId("2");
        u2.setName("B");

        when(usuarioService.listAll(null))
                .thenReturn(Arrays.asList(u1, u2));

        mockMvc.perform(get("/api/usuarios")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(usuarioService).listAll(null);
    }

    @Test
    void patchUpdate_existing_shouldReturn200() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("Updated");

        UsuarioResponseDTO resp = new UsuarioResponseDTO();
        resp.setId("5");
        resp.setName("Updated");

        when(usuarioService.update(eq(5), any(UsuarioRequestDTO.class)))
                .thenReturn(resp);

        mockMvc.perform(patch("/api/usuarios/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.name").value("Updated"));

        verify(usuarioService).update(eq(5), any(UsuarioRequestDTO.class));
    }

    @Test
    void patchUpdate_nonExisting_shouldReturn404() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("DoesNotExist");

        when(usuarioService.update(eq(99), any(UsuarioRequestDTO.class)))
                .thenReturn(null);

        mockMvc.perform(patch("/api/usuarios/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));

        verify(usuarioService).update(eq(99), any(UsuarioRequestDTO.class));
    }

    @Test
    void postCreate_businessConflict_shouldReturn409() throws Exception {

        UsuarioRequestDTO req = new UsuarioRequestDTO();
        req.setName("Dup");
        req.setEmail("dup@example.com");
        req.setPerfilId(1);

        configurarAutenticacao();

        when(usuarioService.create(any(UsuarioRequestDTO.class), eq(ORGANIZACAO_ID)))
                .thenThrow(new ConflictException("Email já cadastrado"));

        mockMvc.perform(post("/api/usuarios")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CONFLICT"));

        verify(authService).obterUsuarioAutenticado(TOKEN);

        verify(usuarioService).create(
                any(UsuarioRequestDTO.class),
                eq(ORGANIZACAO_ID));
    }
}
