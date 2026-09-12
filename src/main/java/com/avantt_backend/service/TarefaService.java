package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final SprintRepository sprintRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TarefaService(TarefaRepository tarefaRepository, ProjetoRepository projetoRepository, SprintRepository sprintRepository) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.sprintRepository = sprintRepository;
    }

    @Transactional
    public TarefaResponseDTO create(TarefaRequestDTO dto) {
        Tarefa t = new Tarefa();
        t.setTitulo(dto.getTitle());
        t.setProject(dto.getProject());
        t.setSprint(dto.getSprint());
        t.setAssignee(dto.getAssignee());
        t.setAvatar(dto.getAvatar());
        t.setAvatarColor(dto.getAvatarColor());
        t.setPriority(dto.getPriority());
        t.setStatus(dto.getStatus());
        t.setDaysDelayed(dto.getDaysDelayed());
        t.setPlannedEnd(dto.getPlannedEnd());
        t.setEstimatedHours(dto.getEstimatedHours());
        t.setBlockedBy(dto.getBlockedBy());
        try {
            t.setTags(dto.getTags() == null ? "[]" : MAPPER.writeValueAsString(dto.getTags()));
        } catch (Exception e) { throw new RuntimeException(e); }

        Tarefa saved = tarefaRepository.save(t);
        TarefaResponseDTO r = new TarefaResponseDTO();
        r.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        r.setTitle(saved.getTitulo());
        r.setProject(saved.getProject());
        r.setSprint(saved.getSprint());
        r.setAssignee(saved.getAssignee());
        r.setAvatar(saved.getAvatar());
        r.setAvatarColor(saved.getAvatarColor());
        r.setPriority(saved.getPriority());
        r.setStatus(saved.getStatus());
        r.setDaysDelayed(saved.getDaysDelayed() == null ? 0 : saved.getDaysDelayed());
        r.setPlannedEnd(saved.getPlannedEnd());
        r.setEstimatedHours(saved.getEstimatedHours());
        r.setBlockedBy(saved.getBlockedBy());
        try {
            if (saved.getTags() == null) r.setTags(java.util.Collections.emptyList());
            else r.setTags(MAPPER.readValue(saved.getTags(), new TypeReference<java.util.List<String>>(){}));
        } catch (Exception e) { r.setTags(java.util.Collections.emptyList()); }
        return r;
    }

    public List<TarefaResponseDTO> listAll(String projetoId, String sprintId, String status) {
        List<Tarefa> all = tarefaRepository.findAll();
        // filtro por projetoId -> resolve para nome do projeto e filtra por t.getProject() == project.name
        if (projetoId != null) {
            String projectName = null;
            try {
                Integer pid = Integer.valueOf(projetoId.replaceAll("[^0-9]", ""));
                projectName = projetoRepository.findById(pid).map(com.avantt_backend.entity.Projeto::getName).orElse(null);
            } catch (Exception ignored) {}
            if (projectName == null) {
                projectName = projetoRepository.findByName(projetoId).map(com.avantt_backend.entity.Projeto::getName).orElse(null);
            }
            if (projectName != null) {
                String finalProjectName = projectName;
                all = all.stream().filter(t -> t.getProject() != null && t.getProject().equals(finalProjectName)).collect(Collectors.toList());
            } else {
                // no project found, return empty
                all = java.util.Collections.emptyList();
            }
        }

        // filtro por sprintId -> resolve para nome da sprint
        if (sprintId != null) {
            String sprintName = null;
            try {
                Integer sid = Integer.valueOf(sprintId.replaceAll("[^0-9]", ""));
                sprintName = sprintRepository.findById(sid).map(com.avantt_backend.entity.Sprint::getNome).orElse(null);
            } catch (Exception ignored) {}
            if (sprintName == null) {
                sprintName = sprintRepository.findByNome(sprintId).map(com.avantt_backend.entity.Sprint::getNome).orElse(null);
            }
            if (sprintName != null) {
                String finalSprintName = sprintName;
                all = all.stream().filter(t -> t.getSprint() != null && t.getSprint().equals(finalSprintName)).collect(Collectors.toList());
            } else {
                all = java.util.Collections.emptyList();
            }
        }

        if (status != null) {
            all = all.stream().filter(t -> t.getStatus() != null && t.getStatus().equals(status)).collect(Collectors.toList());
        }

        return all.stream().map(t -> {
            TarefaResponseDTO r = new TarefaResponseDTO();
            r.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            r.setTitle(t.getTitulo());
            r.setProject(t.getProject());
            r.setSprint(t.getSprint());
            r.setAssignee(t.getAssignee());
            r.setAvatar(t.getAvatar());
            r.setAvatarColor(t.getAvatarColor());
            r.setPriority(t.getPriority());
            r.setStatus(t.getStatus());
            r.setDaysDelayed(t.getDaysDelayed() == null ? 0 : t.getDaysDelayed());
            r.setPlannedEnd(t.getPlannedEnd());
            r.setEstimatedHours(t.getEstimatedHours());
            r.setBlockedBy(t.getBlockedBy());
            try {
                if (t.getTags() == null) r.setTags(java.util.Collections.emptyList());
                else r.setTags(MAPPER.readValue(t.getTags(), new TypeReference<List<String>>(){}));
            } catch (Exception e) { r.setTags(java.util.Collections.emptyList()); }
            return r;
        }).collect(Collectors.toList());
    }
}
