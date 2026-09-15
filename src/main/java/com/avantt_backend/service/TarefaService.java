package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.entity.Usuario;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.avantt_backend.util.StatusUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final SprintRepository sprintRepository;
    private final UsuarioRepository usuarioRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TarefaService(TarefaRepository tarefaRepository, ProjetoRepository projetoRepository, SprintRepository sprintRepository, UsuarioRepository usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.sprintRepository = sprintRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public TarefaResponseDTO create(TarefaRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new IllegalArgumentException("title is required");

        // resolve project
        var projOpt = projetoRepository.findByName(dto.getProject());
        if (projOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Projeto não encontrado: " + dto.getProject());
        var proj = projOpt.get();

        // resolve sprint by name
        var sprintOpt = sprintRepository.findByNome(dto.getSprint());
        if (sprintOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Sprint não encontrada: " + dto.getSprint());
        var sprint = sprintOpt.get();

        // resolve assignee (if provided)
        Integer assigneeId = null;
        if (dto.getAssignee() != null && !dto.getAssignee().isBlank()) {
            var userOpt = usuarioRepository.findByNomeIgnoreCase(dto.getAssignee());
            if (userOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Usuário não encontrado: " + dto.getAssignee());
            assigneeId = userOpt.get().getId();
        }

        Tarefa t = new Tarefa();
        // mandatory nome column
        t.setNome(dto.getTitle());
        t.setTitulo(dto.getTitle());

        // project/sprint linking
        t.setProjetoId(proj.getId());
        t.setProject(proj.getName());
        t.setSprintId(sprint.getId());
        t.setSprint(sprint.getNome());

        t.setAssignee(dto.getAssignee() == null ? "" : dto.getAssignee());
        if (assigneeId != null) t.setAtribuidoPara(assigneeId);

        t.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
        t.setAvatarColor(dto.getAvatarColor() == null ? "" : dto.getAvatarColor());

        String priority = StatusUtils.normalizePriority(dto.getPriority());
        if (priority == null) priority = "média";
        t.setPriority(priority);

        String statusNorm = StatusUtils.normalizeTaskStatus(dto.getStatus());
        if (statusNorm == null) statusNorm = "planejada";
        t.setStatus(statusNorm);

        t.setDaysDelayed(dto.getDaysDelayed() == null ? 0 : dto.getDaysDelayed());
        t.setPlannedEnd(dto.getPlannedEnd());
        t.setEstimatedHours(dto.getEstimatedHours() == null ? 0 : dto.getEstimatedHours());
        t.setBlockedBy(dto.getBlockedBy());
        try {
            t.setTags(dto.getTags() == null ? "[]" : MAPPER.writeValueAsString(dto.getTags()));
        } catch (Exception e) { throw new RuntimeException(e); }

        Tarefa saved = tarefaRepository.save(t);

        TarefaResponseDTO r = new TarefaResponseDTO();
        r.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        r.setTitle(saved.getNome() == null ? saved.getTitulo() : saved.getNome());
        r.setProject(saved.getProject() == null ? proj.getName() : saved.getProject());
        r.setSprint(saved.getSprint() == null ? sprint.getNome() : saved.getSprint());
        r.setAssignee(saved.getAssignee() == null ? "" : saved.getAssignee());
        r.setAvatar(saved.getAvatar() == null ? "" : saved.getAvatar());
        r.setAvatarColor(saved.getAvatarColor() == null ? "" : saved.getAvatarColor());
        r.setPriority(saved.getPriority() == null ? "média" : saved.getPriority());
        r.setStatus(saved.getStatus() == null ? "planejada" : saved.getStatus());
        r.setDaysDelayed(saved.getDaysDelayed() == null ? 0 : saved.getDaysDelayed());
        r.setPlannedEnd(saved.getPlannedEnd());
        r.setEstimatedHours(saved.getEstimatedHours() == null ? 0 : saved.getEstimatedHours());
        r.setBlockedBy(saved.getBlockedBy());
        try {
            if (saved.getTags() == null) r.setTags(java.util.Collections.emptyList());
            else r.setTags(MAPPER.readValue(saved.getTags(), new TypeReference<java.util.List<String>>(){}));
        } catch (Exception e) { r.setTags(java.util.Collections.emptyList()); }
        return r;
    }

    public List<TarefaResponseDTO> listAll(String projetoId, String sprintId, String status) {
        List<Tarefa> all = tarefaRepository.findAll();
        // filtro por projetoId -> accept numeric id
        if (projetoId != null) {
            Integer pid = null;
            try { pid = Integer.valueOf(projetoId.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            if (pid != null) {
                Integer finalPid = pid;
                all = all.stream().filter(t -> t.getProjetoId() != null && t.getProjetoId().equals(finalPid)).collect(Collectors.toList());
            } else {
                // not numeric: try resolve by name
                var projOpt = projetoRepository.findByName(projetoId);
                if (projOpt.isPresent()) {
                    String pname = projOpt.get().getName();
                    all = all.stream().filter(t -> t.getProject() != null && t.getProject().equals(pname)).collect(Collectors.toList());
                } else {
                    all = java.util.Collections.emptyList();
                }
            }
        }

        // filtro por sprintId
        if (sprintId != null) {
            Integer sid = null;
            try { sid = Integer.valueOf(sprintId.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}
            if (sid != null) {
                Integer finalSid = sid;
                all = all.stream().filter(t -> t.getSprintId() != null && t.getSprintId().equals(finalSid)).collect(Collectors.toList());
            } else {
                var sOpt = sprintRepository.findByNome(sprintId);
                if (sOpt.isPresent()) {
                    String sname = sOpt.get().getNome();
                    all = all.stream().filter(t -> t.getSprint() != null && t.getSprint().equals(sname)).collect(Collectors.toList());
                } else {
                    all = java.util.Collections.emptyList();
                }
            }
        }

        if (status != null) {
            String normFilter = StatusUtils.normalizeTaskStatus(status);
            all = all.stream().filter(t -> {
                String ts = t.getStatus();
                String norm = StatusUtils.normalizeTaskStatus(ts);
                return normFilter != null && normFilter.equals(norm);
            }).collect(Collectors.toList());
        }

        return all.stream().map(t -> {
            TarefaResponseDTO r = new TarefaResponseDTO();
            r.setId(t.getId() == null ? null : String.valueOf(t.getId()));
            r.setTitle(t.getNome() == null ? (t.getTitulo() == null ? "" : t.getTitulo()) : t.getNome());
            r.setProject(t.getProject() == null ? "" : t.getProject());
            r.setSprint(t.getSprint() == null ? "" : t.getSprint());
            r.setAssignee(t.getAssignee() == null ? "" : t.getAssignee());
            r.setAvatar(t.getAvatar() == null ? "" : t.getAvatar());
            r.setAvatarColor(t.getAvatarColor() == null ? "" : t.getAvatarColor());
            String pri = StatusUtils.normalizePriority(t.getPriority());
            r.setPriority(pri == null ? "média" : pri);
            String st = StatusUtils.normalizeTaskStatus(t.getStatus());
            r.setStatus(st == null ? "planejada" : st);
            r.setDaysDelayed(t.getDaysDelayed() == null ? 0 : t.getDaysDelayed());
            r.setPlannedEnd(t.getPlannedEnd());
            r.setEstimatedHours(t.getEstimatedHours() == null ? 0 : t.getEstimatedHours());
            r.setBlockedBy(t.getBlockedBy());
            try {
                if (t.getTags() == null) r.setTags(java.util.Collections.emptyList());
                else r.setTags(MAPPER.readValue(t.getTags(), new TypeReference<List<String>>(){}));
            } catch (Exception e) { r.setTags(java.util.Collections.emptyList()); }
            return r;
        }).collect(Collectors.toList());
    }
}
