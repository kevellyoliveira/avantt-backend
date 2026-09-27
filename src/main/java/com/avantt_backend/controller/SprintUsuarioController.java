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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.avantt_backend.util.ApiPaths.SPRINTS;
import static com.avantt_backend.util.Mensagens.MENSAGEM_ERRO_INTERNO_500;

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

    @PostMapping(path = "/{sprintId}/usuarios", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> addUserToSprint(@PathVariable Integer sprintId, @RequestBody java.util.Map<String,Integer> body) {
        try {
            Integer usuarioId = body.get("usuarioId");
            if (usuarioId == null) return ResponseEntity.status(400).body("usuarioId é obrigatório");

            // verify sprint exists
            Sprint s = sprintRepository.findById(sprintId).orElse(null);
            if (s == null) return ResponseEntity.status(404).body("Sprint não encontrada: id=" + sprintId);

            // verify user exists
            Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
            if (u == null) return ResponseEntity.status(404).body("Usuario não encontrado: id=" + usuarioId);

            // verify user belongs to the project of the sprint
            Integer projetoId = s.getProjetoId();
            boolean belongs = projetoUsuarioRepository.findByIdProjetoId(projetoId).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(usuarioId));
            if (!belongs) return ResponseEntity.status(400).body("Usuario não pertence ao projeto da sprint");

            SprintUsuarioId id = new SprintUsuarioId(sprintId, usuarioId);
            if (!sprintUsuarioRepository.existsById(id)) {
                SprintUsuario su = new SprintUsuario();
                su.setId(id);
                su.setPapel(null);
                sprintUsuarioRepository.save(su);
            }

            return ResponseEntity.status(201).body("Associado");
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }

    @GetMapping(path = "/{sprintId}/usuarios", produces = "application/json")
    public ResponseEntity<?> listSprintUsers(@PathVariable Integer sprintId) {
        try {
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
        } catch (Exception e) {
            return ResponseEntity.status(500).body(MENSAGEM_ERRO_INTERNO_500);
        }
    }
}
