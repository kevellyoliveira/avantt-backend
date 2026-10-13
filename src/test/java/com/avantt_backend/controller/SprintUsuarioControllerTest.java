package com.avantt_backend.controller;

import com.avantt_backend.dto.SprintUsuarioRequestDTO;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.SprintUsuario;
import com.avantt_backend.entity.SprintUsuarioId;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.UsuarioRepository;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SprintUsuarioControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private SprintUsuarioRepository sprintUsuarioRepository;

    @Mock
    private ProjetoUsuarioRepository projetoUsuarioRepository;

    @Mock
    private SprintRepository sprintRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private SprintUsuarioController controller;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addUserToSprint_success_returns201() throws Exception {
        int sprintId = 5;
        int usuarioId = 3;

        SprintUsuarioRequestDTO body = new SprintUsuarioRequestDTO();
        body.setUsuarioId(usuarioId);

        Sprint s = new Sprint(); s.setId(sprintId); s.setProjetoId(10);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(s));

        Usuario u = new Usuario(); u.setId(usuarioId); u.setNome("Joao");
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(u));

        ProjetoUsuario pu = new ProjetoUsuario(); pu.setId(new ProjetoUsuarioId(10, usuarioId));
        when(projetoUsuarioRepository.findByIdProjetoId(10)).thenReturn(List.of(pu));

        when(sprintUsuarioRepository.existsById(any(SprintUsuarioId.class))).thenReturn(false);

        mockMvc.perform(post("/api/sprints/" + sprintId + "/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Associado"));

        verify(sprintUsuarioRepository).save(any(SprintUsuario.class));
    }

    @Test
    void addUserToSprint_sprintNotFound_returns404() throws Exception {
        int sprintId = 5;
        SprintUsuarioRequestDTO body = new SprintUsuarioRequestDTO(); body.setUsuarioId(3);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/sprints/" + sprintId + "/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void addUserToSprint_userNotFound_returns404() throws Exception {
        int sprintId = 5;
        int usuarioId = 3;
        SprintUsuarioRequestDTO body = new SprintUsuarioRequestDTO(); body.setUsuarioId(usuarioId);
        Sprint s = new Sprint(); s.setId(sprintId); s.setProjetoId(10);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/sprints/" + sprintId + "/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void addUserToSprint_userNotBelongToProject_returns400() throws Exception {
        int sprintId = 5;
        int usuarioId = 3;
        SprintUsuarioRequestDTO body = new SprintUsuarioRequestDTO(); body.setUsuarioId(usuarioId);
        Sprint s = new Sprint(); s.setId(sprintId); s.setProjetoId(10);
        when(sprintRepository.findById(sprintId)).thenReturn(Optional.of(s));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(new Usuario(){ { setId(usuarioId); setNome("X"); } }));
        when(projetoUsuarioRepository.findByIdProjetoId(10)).thenReturn(List.of()); // user not in project

        mockMvc.perform(post("/api/sprints/" + sprintId + "/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(com.avantt_backend.util.Mensagens.MENSAGEM_USUARIO_NAO_PERTENCE_PROJETO_SPRINT));
    }

    @Test
    void listSprintUsers_returnsUsers() throws Exception {
        int sprintId = 8;
        com.avantt_backend.entity.SprintUsuario su = new com.avantt_backend.entity.SprintUsuario();
        su.setId(new com.avantt_backend.entity.SprintUsuarioId(sprintId, 2));
        when(sprintUsuarioRepository.findByIdSprintId(sprintId)).thenReturn(List.of(su));

        when(usuarioRepository.findById(2)).thenReturn(Optional.of(new Usuario(){ { setId(2); setNome("Bruno"); setEmail("b@x"); } }));

        mockMvc.perform(get("/api/sprints/" + sprintId + "/usuarios").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].nome").value("Bruno"));
    }
}
