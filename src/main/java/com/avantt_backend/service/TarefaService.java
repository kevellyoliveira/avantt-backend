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
import com.avantt_backend.entity.Status;
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
    private final PrioridadeRepository prioridadeRepository;
    private final StatusTarefaRepository statusTarefaRepository;
    private final com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository;
    private final com.avantt_backend.repository.ProjetoUsuarioRepository projetoUsuarioRepository;
    private final com.avantt_backend.repository.TagRepository tagRepository;
    private final com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TarefaService(TarefaRepository tarefaRepository, ProjetoRepository projetoRepository, SprintRepository sprintRepository, UsuarioRepository usuarioRepository, PrioridadeRepository prioridadeRepository, StatusTarefaRepository statusTarefaRepository, com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository, com.avantt_backend.repository.ProjetoUsuarioRepository projetoUsuarioRepository, com.avantt_backend.repository.TagRepository tagRepository, com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.sprintRepository = sprintRepository;
        this.usuarioRepository = usuarioRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.statusTarefaRepository = statusTarefaRepository;
        this.sprintUsuarioRepository = sprintUsuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.tagRepository = tagRepository;
        this.tarefaTagRepository = tarefaTagRepository;
    }

    @Transactional
    public TarefaResponseDTO create(TarefaRequestDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new IllegalArgumentException("title is required");

        // resolve project by id
        if (dto.getProjectId() == null) throw new com.avantt_backend.exception.ApiException("O campo projectId é obrigatório");
        var projOpt = projetoRepository.findById(dto.getProjectId());
        if (projOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Projeto não encontrado: id=" + dto.getProjectId());
        var proj = projOpt.get();

        // resolve sprint by id
        if (dto.getSprintId() == null) throw new com.avantt_backend.exception.ApiException("O campo sprintId é obrigatório");
        var sprintOpt = sprintRepository.findById(dto.getSprintId());
        if (sprintOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Sprint não encontrada: id=" + dto.getSprintId());
        var sprint = sprintOpt.get();

        // resolve assignee by id if provided; ensure the user is member of the sprint
        Integer assigneeId = null;
        if (dto.getAssigneeId() != null) {
            var userOpt = usuarioRepository.findById(dto.getAssigneeId());
            if (userOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Usuário não encontrado: id=" + dto.getAssigneeId());
            assigneeId = dto.getAssigneeId();
        }
        final Integer resolvedAssigneeId = assigneeId;

        Tarefa t = new Tarefa();
        // mandatory nome column
        t.setNome(dto.getTitle());
        t.setTitulo(dto.getTitle());

        // project/sprint linking
        t.setProjetoId(proj.getId());
        t.setProject(proj.getName());
        t.setSprintId(sprint.getId());
        t.setSprint(sprint.getNome());

        // store assignee id in DB column 'atribuido_para'
        if (resolvedAssigneeId != null) {
            // ensure user is member of the sprint
            Integer sprintId = sprint.getId();
            boolean memberSprint = sprintUsuarioRepository.findByIdSprintId(sprintId).stream().anyMatch(su -> su.getId().getUsuarioId().equals(resolvedAssigneeId));
            if (!memberSprint) throw new com.avantt_backend.exception.ApiException("Usuário não está associado à sprint");

            // ensure user is member of the project
            Integer projetoId = proj.getId();
            boolean memberProject = projetoUsuarioRepository.findByIdProjetoId(projetoId).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(resolvedAssigneeId));
            if (!memberProject) throw new com.avantt_backend.exception.ApiException("Usuário não está associado ao projeto");

            t.setAssignee(resolvedAssigneeId);
        } else {
            t.setAssignee(null);
        }

        t.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
        t.setAvatarColor(dto.getAvatarColor() == null ? "" : dto.getAvatarColor());

        String priority = StatusUtils.normalizePriority(dto.getPriority());
        if (priority == null) priority = "média";
        t.setPriority(priority);

        // require statusId and resolve name
        if (dto.getStatusId() == null) throw new IllegalArgumentException("statusId is required");
        var stOpt = statusTarefaRepository.findById(dto.getStatusId());
        if (stOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Status não encontrado: id=" + dto.getStatusId());
        t.setStatusId(dto.getStatusId());
        // store normalized status string as before
        String statusNorm = StatusUtils.normalizeTaskStatus(stOpt.get().getNome());
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
        Tarefa saved = tarefaRepository.save(t);

        // persist tag relations: delete existing and insert provided tag ids
        try {
            tarefaTagRepository.deleteByIdTarefaId(saved.getId());
            if (dto.getTagIds() != null) {
                for (Integer tagId : dto.getTagIds()) {
                    if (tagId == null) continue;
                    // validate tag exists
                    if (tagRepository.findById(tagId).isEmpty()) {
                        throw new ApiException("Tag não encontrada: id=" + tagId);
                    }
                    com.avantt_backend.entity.TarefaTag tt = new com.avantt_backend.entity.TarefaTag(saved.getId(), tagId);
                    tarefaTagRepository.save(tt);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }

        TarefaResponseDTO r = new TarefaResponseDTO();
        r.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        r.setTitle(saved.getNome() == null ? saved.getTitulo() : saved.getNome());
        r.setProject(saved.getProject() == null ? proj.getName() : saved.getProject());
        r.setSprint(saved.getSprint() == null ? sprint.getNome() : saved.getSprint());
        // resolve assignee name from stored id
        if (saved.getAssignee() == null) r.setAssignee("");
        else r.setAssignee(usuarioRepository.findById(saved.getAssignee()).map(Usuario::getNome).orElse(""));
        r.setAvatar(saved.getAvatar() == null ? "" : saved.getAvatar());
        r.setAvatarColor(saved.getAvatarColor() == null ? "" : saved.getAvatarColor());
        r.setPriority(saved.getPriority() == null ? "média" : saved.getPriority());
        r.setStatus(saved.getStatus() == null ? "planejada" : saved.getStatus());
        r.setDaysDelayed(saved.getDaysDelayed() == null ? 0 : saved.getDaysDelayed());
        r.setPlannedEnd(saved.getPlannedEnd());
        r.setEstimatedHours(saved.getEstimatedHours() == null ? 0 : saved.getEstimatedHours());
        r.setBlockedBy(saved.getBlockedBy());
        // load tag ids from tarefa_tag
        try {
            var ttags = tarefaTagRepository.findByIdTarefaId(saved.getId());
            var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
            r.setTagIds(ids);
        } catch (Exception e) { r.setTagIds(java.util.Collections.emptyList()); }
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
            // if status param is numeric, filter by statusId
            Integer statusIdFilter = null;
            try { statusIdFilter = Integer.valueOf(status.replaceAll("[^0-9]", "")); } catch (Exception ignored) {}

            if (statusIdFilter != null) {
                Integer finalSid = statusIdFilter;
                all = all.stream().filter(t -> t.getStatusId() != null && t.getStatusId().equals(finalSid)).collect(Collectors.toList());
            } else {
                String normFilter = StatusUtils.normalizeTaskStatus(status);
                all = all.stream()
                    .filter(t -> {
                        String taskStatus = t.getStatus();
                        if ((taskStatus == null || taskStatus.isBlank()) && t.getStatusId() != null) {
                            taskStatus = statusTarefaRepository.findById(t.getStatusId()).map(Status::getNome).orElse(null);
                        }
                        String taskNorm = StatusUtils.normalizeTaskStatus(taskStatus);
                        return normFilter != null && normFilter.equals(taskNorm);
                    })
                    .collect(Collectors.toList());
            }
        }

        return all.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<java.util.Map<String,Object>> listUsersForTask(Integer tarefaId) {
        var tOpt = tarefaRepository.findById(tarefaId);
        if (tOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Tarefa não encontrada: id=" + tarefaId);
        var t = tOpt.get();
        if (t.getSprintId() == null) return java.util.Collections.emptyList();
        var sus = sprintUsuarioRepository.findByIdSprintId(t.getSprintId());
        List<java.util.Map<String,Object>> out = new java.util.ArrayList<>();
        for (var su : sus) {
            Integer uid = su.getId().getUsuarioId();
            usuarioRepository.findById(uid).ifPresent(u -> {
                var m = new java.util.HashMap<String,Object>();
                m.put("id", u.getId());
                m.put("nome", u.getNome());
                m.put("email", u.getEmail());
                out.add(m);
            });
        }
        return out;
    }

    @Transactional
    public TarefaResponseDTO updateAssignee(Integer tarefaId, Integer assigneeId) {
        var tOpt = tarefaRepository.findById(tarefaId);
        if (tOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Tarefa não encontrada: id=" + tarefaId);
        var t = tOpt.get();

        if (assigneeId == null) {
            // unassign
            t.setAssignee(null);
            Tarefa saved = tarefaRepository.save(t);
            return toResponse(saved);
        }

        var userOpt = usuarioRepository.findById(assigneeId);
        if (userOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Usuário não encontrado: id=" + assigneeId);

        if (t.getSprintId() == null) throw new com.avantt_backend.exception.ApiException("Tarefa não está associada a uma sprint");
        boolean memberSprint = sprintUsuarioRepository.findByIdSprintId(t.getSprintId()).stream().anyMatch(su -> su.getId().getUsuarioId().equals(assigneeId));
        if (!memberSprint) throw new com.avantt_backend.exception.ApiException("Usuário não está associado à sprint");

        if (t.getProjetoId() == null) throw new com.avantt_backend.exception.ApiException("Tarefa não está associada a um projeto");
        boolean memberProject = projetoUsuarioRepository.findByIdProjetoId(t.getProjetoId()).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(assigneeId));
        if (!memberProject) throw new com.avantt_backend.exception.ApiException("Usuário não está associado ao projeto");

        t.setAssignee(assigneeId);
        Tarefa saved = tarefaRepository.save(t);
        return toResponse(saved);
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

        Integer assigneeId = t.getAssignee();
        String assigneeName = "";
        if (assigneeId != null) {
            assigneeName = usuarioRepository.findById(assigneeId).map(Usuario::getNome).orElse("");
        }
        r.setAssignee(assigneeName);

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

        // expose both statusId and statusName while keeping `status` as the normalized frontend value
        r.setStatusId(t.getStatusId());
        String statusVal = t.getStatus();
        if ((statusVal == null || statusVal.isBlank()) && t.getStatusId() != null) {
            statusVal = statusTarefaRepository.findById(t.getStatusId()).map(Status::getNome).orElse(null);
        }
        r.setStatusName(statusVal);
        String normStatus = StatusUtils.normalizeTaskStatus(statusVal);
        r.setStatus(normStatus == null ? "planejada" : normStatus);

        r.setDaysDelayed(t.getDaysDelayed() == null ? 0 : t.getDaysDelayed());
        r.setPlannedEnd(t.getPlannedEnd());
        r.setEstimatedHours(t.getEstimatedHours() == null ? 0 : t.getEstimatedHours());
        r.setBlockedBy(t.getBlockedBy());

        // load tag ids from tarefa_tag for each task
        try {
            var ttags = tarefaTagRepository.findByIdTarefaId(t.getId());
            var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
            r.setTagIds(ids);
        } catch (Exception e) { r.setTagIds(java.util.Collections.emptyList()); }

        return r;
    }
}
