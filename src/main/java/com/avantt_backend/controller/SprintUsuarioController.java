package com.avantt_backend.controller;

import com.avantt_backend.entity.SprintUsuario;
import com.avantt_backend.entity.SprintUsuarioId;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Usuario;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

import static com.avantt_backend.util.ApiPaths.SPRINTS;
import static com.avantt_backend.util.Mensagens.*;

@RestController
@RequestMapping(SPRINTS)
@Tag(name = "SprintUsuarios", description = "Gerenciamento de usuários de sprint")
public class SprintUsuarioController {

    private final SprintUsuarioRepository sprintUsuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final SprintRepository sprintRepository;
    private final UsuarioRepository usuarioRepository;

    public SprintUsuarioController(SprintUsuarioRepository sprintUsuarioRepository,
                                   ProjetoUsuarioRepository projetoUsuarioRepository,
                                   SprintRepository sprintRepository,
                                   UsuarioRepository usuarioRepository) {
        this.sprintUsuarioRepository = sprintUsuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintRepository = sprintRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Operation(summary = "Associar usuário à sprint", description = "Associa um usuário à sprint (usuário deve pertencer ao projeto do sprint).")
    @PostMapping(path = "/{sprintId}/usuarios", consumes = "application/json", produces = "application/json")
    @ApiResponse(responseCode = "201", description = "Usuário associado à sprint",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Associate user example", value = "\"Associado\"")
        }))
    public ResponseEntity<?> addUserToSprint(@PathVariable Integer sprintId, @RequestBody(description = "Corpo: { \"usuarioId\": &lt;id&gt; }", required = true,
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.avantt_backend.dto.SprintUsuarioRequestDTO.class), examples = {
                @ExampleObject(name = "Add sprint user request example", value = "{\"usuarioId\": 3}")
            })) @Valid @org.springframework.web.bind.annotation.RequestBody com.avantt_backend.dto.SprintUsuarioRequestDTO body) {
        Integer usuarioId = body.getUsuarioId();

        // verify sprint exists
        Sprint s = sprintRepository.findById(sprintId).orElseThrow(() -> new com.avantt_backend.exception.ResourceNotFoundException("Sprint não encontrada: id=" + sprintId));

        // verify user exists
        Usuario u = usuarioRepository.findById(usuarioId).orElseThrow(() -> new com.avantt_backend.exception.ResourceNotFoundException("Usuario não encontrado: id=" + usuarioId));

        // verify user belongs to the project of the sprint
        Integer projetoId = s.getProjetoId();
        boolean belongs = projetoUsuarioRepository.findByIdProjetoId(projetoId).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(usuarioId));
        if (!belongs) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, MENSAGEM_USUARIO_NAO_PERTENCE_PROJETO_SPRINT);

        SprintUsuarioId id = new SprintUsuarioId(sprintId, usuarioId);
        if (!sprintUsuarioRepository.existsById(id)) {
            SprintUsuario su = new SprintUsuario();
            su.setId(id);
            su.setPapel(null);
            sprintUsuarioRepository.save(su);
        }

        return ResponseEntity.status(201).body("Associado");
    }

    @Operation(summary = "Listar usuários da sprint", description = "Lista os usuários associados à sprint especificada.")
    @GetMapping(path = "/{sprintId}/usuarios", produces = "application/json")
    @ApiResponse(responseCode = "200", description = "Lista de usuários da sprint",
        content = @Content(mediaType = "application/json", examples = {
            @ExampleObject(name = "Sprint users example", value = "[{\"id\":2,\"nome\":\"Bruno Henrique Lima\",\"email\":\"bruno.lima@email.com\"}]")
        }))
    public ResponseEntity<?> listSprintUsers(@PathVariable Integer sprintId) {
        List<SprintUsuario> list = sprintUsuarioRepository.findByIdSprintId(sprintId);
        List<java.util.Map<String,Object>> out = new ArrayList<>();
        for (var su : list) {
            Integer uid = su.getId().getUsuarioId();
            usuarioRepository.findById(uid).ifPresent(u -> {
                var m = new java.util.HashMap<String,Object>();
                m.put("id", u.getId());
                m.put("nome", u.getNome());
                m.put("email", u.getEmail());
                out.add(m);
            });
        }
        return ResponseEntity.ok(out);
    }
}
