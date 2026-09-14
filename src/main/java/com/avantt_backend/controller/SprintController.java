package com.avantt_backend.controller;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.avantt_backend.util.ApiPaths.SPRINTS;

@RestController
@RequestMapping
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping(path = SPRINTS, consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> createSprint(@RequestBody SprintRequestDTO request) {
        var created = sprintService.create(request);
        return ResponseEntity.status(201).body(created);
    }

    @GetMapping(path = SPRINTS, produces = "application/json")
    public ResponseEntity<List<SprintResponseDTO>> listSprints(@RequestParam(value = "projeto_id", required = false) String projetoId) {
        List<SprintResponseDTO> list = sprintService.listAll(projetoId);
        return ResponseEntity.ok(list);
    }
}
