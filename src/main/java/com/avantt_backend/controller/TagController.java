package com.avantt_backend.controller;

import com.avantt_backend.entity.Tag;
import com.avantt_backend.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.TAREFAS;
import static com.avantt_backend.util.ApiPaths.API;
import static com.avantt_backend.util.Mensagens.MENSAGEM_ERRO_INTERNO_500;

@RestController
@RequestMapping(API)
@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "Gerenciamento de tags")
public class TagController {

    private final TagRepository tagRepository;
    private final com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository;

    public TagController(TagRepository tagRepository, com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository) {
        this.tagRepository = tagRepository;
        this.tarefaTagRepository = tarefaTagRepository;
    }

    @GetMapping(path = "/tags", produces = "application/json")
    public ResponseEntity<?> listAll() {
        try {
            List<Tag> all = tagRepository.findAll();
            return ResponseEntity.ok(all);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @GetMapping(path = TAREFAS + "/{tarefaId}/tags", produces = "application/json")
    public ResponseEntity<?> listByTarefa(@PathVariable Integer tarefaId) {
        try {
            var ttags = tarefaTagRepository.findByIdTarefaId(tarefaId);
            var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
            return ResponseEntity.ok(ids);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
