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
        List<Tag> all = tagRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping(path = TAREFAS + "/{tarefaId}/tags", produces = "application/json")
    public ResponseEntity<?> listByTarefa(@PathVariable Integer tarefaId) {
        var ttags = tarefaTagRepository.findByIdTarefaId(tarefaId);
        var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
        return ResponseEntity.ok(ids);
    }
}
