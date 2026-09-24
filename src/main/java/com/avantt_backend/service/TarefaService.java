package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.repository.PrioridadeRepository;
import com.avantt_backend.repository.StatusTarefaRepository;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.Prioridade;
import com.avantt_backend.entity.StatusTarefa;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.avantt_backend.util.StatusUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final SprintRepository sprintRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final StatusTarefaRepository statusTarefaRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TarefaService(TarefaRepository tarefaRepository, ProjetoRepository projetoRepository, SprintRepository sprintRepository, UsuarioRepository usuarioRepository, PrioridadeRepository prioridadeRepository, StatusTarefaRepository statusTarefaRepository) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.sprintRepository = sprintRepository;
        this.usuarioRepository = usuarioRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.statusTarefaRepository = statusTarefaRepository;
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

        if (dto.getPlannedEnd().isAfter(sprint.getDataFim())) {
            throw new ApiException(
                    "A data de entrega deve ser até a data de fim da sprint"
            );
        }
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

            all = all.stream()
                .filter(t -> {
                    String taskStatus = t.getStatus();
                    if ((taskStatus == null || taskStatus.isBlank()) && t.getStatusId() != null) {
                        taskStatus = statusTarefaRepository.findById(t.getStatusId()).map(StatusTarefa::getNome).orElse(null);
                    }
                    String taskNorm = StatusUtils.normalizeTaskStatus(taskStatus);
                    return normFilter != null && normFilter.equals(taskNorm);
                })
                .collect(Collectors.toList());
        }

        return all.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private java.util.List<String> parseTags(String tagsJson) {
        try {
            if (tagsJson == null) return java.util.Collections.emptyList();
            return MAPPER.readValue(tagsJson, new TypeReference<java.util.List<String>>(){});
        } catch (Exception e) { return java.util.Collections.emptyList(); }
    }

    private TarefaResponseDTO toResponse(Tarefa t) {
        TarefaResponseDTO r = new TarefaResponseDTO();

        r.setId(String.valueOf(t.getId()));

        r.setTitle(
            t.getNome() != null && !t.getNome().isBlank()
                ? t.getNome()
                : (t.getTitulo() == null ? "" : t.getTitulo())
        );

        String project = t.getProject();
        if ((project == null || project.isBlank()) && t.getProjetoId() != null) {
            project = projetoRepository.findById(t.getProjetoId()).map(com.avantt_backend.entity.Projeto::getName).orElse("");
        }
        r.setProject(project == null ? "" : project);

        String sprint = t.getSprint();
        if ((sprint == null || sprint.isBlank()) && t.getSprintId() != null) {
            sprint = sprintRepository.findById(t.getSprintId()).map(com.avantt_backend.entity.Sprint::getNome).orElse("");
        }
        r.setSprint(sprint == null ? "" : sprint);

        String assignee = t.getAssignee();
        if ((assignee == null || assignee.isBlank()) && t.getAtribuidoPara() != null) {
            assignee = usuarioRepository.findById(t.getAtribuidoPara()).map(Usuario::getNome).orElse("");
        }
        r.setAssignee(assignee == null ? "" : assignee);

        r.setAvatar(t.getAvatar() == null ? "" : t.getAvatar());
        r.setAvatarColor(
            t.getAvatarColor() == null || t.getAvatarColor().isBlank()
                ? "#4B7BF5"
                : t.getAvatarColor()
        );

        String priority = t.getPriority();
        if ((priority == null || priority.isBlank()) && t.getPrioridadeId() != null) {
            priority = prioridadeRepository.findById(t.getPrioridadeId()).map(Prioridade::getNome).orElse(null);
        }
        String normPri = StatusUtils.normalizePriority(priority);
        r.setPriority(normPri == null ? "média" : normPri);

        String statusVal = t.getStatus();
        if ((statusVal == null || statusVal.isBlank()) && t.getStatusId() != null) {
            statusVal = statusTarefaRepository.findById(t.getStatusId()).map(StatusTarefa::getNome).orElse(null);
        }
        String normStatus = StatusUtils.normalizeTaskStatus(statusVal);
        r.setStatus(normStatus == null ? "planejada" : normStatus);

        r.setDaysDelayed(t.getDaysDelayed() == null ? 0 : t.getDaysDelayed());
        r.setPlannedEnd(t.getPlannedEnd());
        r.setEstimatedHours(t.getEstimatedHours() == null ? 0 : t.getEstimatedHours());
        r.setBlockedBy(t.getBlockedBy());

        r.setTags(parseTags(t.getTags()));

        return r;
    }
}
