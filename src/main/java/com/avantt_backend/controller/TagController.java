package com.avantt_backend.controller;

import com.avantt_backend.entity.Tag;
import com.avantt_backend.repository.TagRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import io.swagger.v3.oas.annotations.Operation;
// keep entity Tag import; use fully-qualified Swagger Tag to avoid name clash with entity
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

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

    @Operation(summary = "Listar tags", description = "Retorna todas as tags disponíveis no sistema.")
    @GetMapping(path = "/tags", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Lista de tags",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Tags example", value = "[{\"id\":1,\"nome\":\"Backend\"},{\"id\":2,\"nome\":\"Frontend\"}]")
        }))
    public ResponseEntity<?> listAll() {
        List<Tag> all = tagRepository.findAll();
        return ResponseEntity.ok(all);
    }

    @Operation(summary = "Listar tags de tarefa", description = "Retorna os IDs das tags associadas a uma tarefa.")
    @GetMapping(path = TAREFAS + "/{tarefaId}/tags", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "IDs das tags da tarefa",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Tarefa tags example", value = "[1,8]")
        }))
    public ResponseEntity<?> listByTarefa(@PathVariable Integer tarefaId) {
        var ttags = tarefaTagRepository.findByIdTarefaId(tarefaId);
        var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
        return ResponseEntity.ok(ids);
    }
}
