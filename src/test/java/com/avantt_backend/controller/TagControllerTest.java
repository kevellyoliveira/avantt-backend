package com.avantt_backend.controller;

import com.avantt_backend.entity.Tag;
import com.avantt_backend.exception.GlobalExceptionHandler;
import com.avantt_backend.repository.TarefaTagRepository;
import com.avantt_backend.repository.TagRepository;
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
class TagControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TarefaTagRepository tarefaTagRepository;

    @InjectMocks
    private TagController tagController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tagController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listAll_returnsTags() throws Exception {
        Tag a = new Tag(1, "Backend");
        Tag b = new Tag(2, "Frontend");
        when(tagRepository.findAll()).thenReturn(List.of(a,b));

        mockMvc.perform(get("/api/tags").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].nome").value("Frontend"));
    }

    @Test
    void listByTarefa_returnsIds() throws Exception {
        when(tarefaTagRepository.findByIdTarefaId(5)).thenReturn(List.of(new com.avantt_backend.entity.TarefaTag(5, 10), new com.avantt_backend.entity.TarefaTag(5,11)));

        mockMvc.perform(get("/api/tarefas/5/tags").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value(10))
                .andExpect(jsonPath("$[1]").value(11));
    }
}
