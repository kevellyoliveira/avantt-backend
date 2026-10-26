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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final SprintRepository sprintRepository;
    private final com.avantt_backend.service.SprintService sprintService;
    private final UsuarioRepository usuarioRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final StatusTarefaRepository statusTarefaRepository;
    private final com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository;
    private final com.avantt_backend.repository.ProjetoUsuarioRepository projetoUsuarioRepository;
    private final com.avantt_backend.repository.TagRepository tagRepository;
    private final com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public TarefaService(TarefaRepository tarefaRepository, ProjetoRepository projetoRepository, SprintRepository sprintRepository, com.avantt_backend.service.SprintService sprintService, UsuarioRepository usuarioRepository, PrioridadeRepository prioridadeRepository, StatusTarefaRepository statusTarefaRepository, com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository, com.avantt_backend.repository.ProjetoUsuarioRepository projetoUsuarioRepository, com.avantt_backend.repository.TagRepository tagRepository, com.avantt_backend.repository.TarefaTagRepository tarefaTagRepository) {
        this.tarefaRepository = tarefaRepository;
        this.projetoRepository = projetoRepository;
        this.sprintRepository = sprintRepository;
        this.sprintService = sprintService;
        this.usuarioRepository = usuarioRepository;
        this.prioridadeRepository = prioridadeRepository;
        this.statusTarefaRepository = statusTarefaRepository;
        this.sprintUsuarioRepository = sprintUsuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.tagRepository = tagRepository;
        this.tarefaTagRepository = tarefaTagRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(TarefaService.class);

    // Ensure pending changes are flushed so the recalculation sees the updated task state,
    // then run the recalculation. This runs in the current transaction context so the
    // sprint progress update will be committed together with the task change.
    private void scheduleSprintProgressRecalculation(Integer sprintId) {
        if (sprintId == null) return;
        try {
            log.debug("scheduleSprintProgressRecalculation: flushing changes for sprintId={}", sprintId);
            // flush pending task changes so queries used by the recalculation see them
            tarefaRepository.flush();
        } catch (Exception e) {
            log.warn("scheduleSprintProgressRecalculation: flush failed for sprintId={}: {}", sprintId, e.getMessage());
        }
        try {
            log.debug("scheduleSprintProgressRecalculation: running recalc for sprintId={}", sprintId);
            sprintService.recalculateAndPersistProgress(sprintId);
            log.debug("scheduleSprintProgressRecalculation: recalc completed for sprintId={}", sprintId);
        } catch (Exception e) {
            log.warn("scheduleSprintProgressRecalculation: recalc failed for sprintId={}: {}", sprintId, e.getMessage());
        }
    }

    @Transactional
    public TarefaResponseDTO create(TarefaRequestDTO dto) {
        // basic field validations (not null/blank) are handled by DTO annotations
        // resolve project by id
        var projOpt = projetoRepository.findById(dto.getProjectId());
        if (projOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Projeto não encontrado: id=" + dto.getProjectId());
        var proj = projOpt.get();

        // resolve sprint by id
        var sprintOpt = sprintRepository.findById(dto.getSprintId());
        if (sprintOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sprint não encontrada: id=" + dto.getSprintId());
        var sprint = sprintOpt.get();

        // resolve assignee by id if provided; ensure the user is member of the sprint
        Integer assigneeId = null;
        if (dto.getAssigneeId() != null) {
            var userOpt = usuarioRepository.findById(dto.getAssigneeId());
            if (userOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Usuário não encontrado: id=" + dto.getAssigneeId());
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
            if (!memberSprint) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado à sprint");

            // ensure user is member of the project
            Integer projetoId = proj.getId();
            boolean memberProject = projetoUsuarioRepository.findByIdProjetoId(projetoId).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(resolvedAssigneeId));
            if (!memberProject) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado ao projeto");

            t.setAssignee(resolvedAssigneeId);
        } else {
            t.setAssignee(null);
        }

        t.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());
        t.setAvatarColor(dto.getAvatarColor() == null ? "" : dto.getAvatarColor());

        // resolve priority: prefer explicit prioridadeId when provided, otherwise normalize string
        String priority = null;
        if (dto.getPrioridadeId() != null) {
            var pOpt = prioridadeRepository.findById(dto.getPrioridadeId());
            if (pOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Prioridade não encontrada: id=" + dto.getPrioridadeId());
            t.setPrioridadeId(dto.getPrioridadeId());
            priority = StatusUtils.normalizePriority(pOpt.get().getNome());
        } else {
            priority = StatusUtils.normalizePriority(dto.getPriority());
        }
        if (priority == null) priority = "média";
        t.setPriority(priority);

        // require statusId and resolve name (dto validation ensures not null)
        var stOpt = statusTarefaRepository.findById(dto.getStatusId());
        if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
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
        t.setDescricao(dto.getDescription() == null ? null : dto.getDescription());
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

        // after creating a task, schedule recalculation of sprint progress after commit
        scheduleSprintProgressRecalculation(saved.getSprintId());

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
        r.setDescription(saved.getDescricao());
        return r;
    }

    @Transactional
    public TarefaResponseDTO update(Integer tarefaId, TarefaRequestDTO dto) {
        var tOpt = tarefaRepository.findById(tarefaId);
        if (tOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa não encontrada: id=" + tarefaId);
        var t = tOpt.get();

        // capture old sprint/status to know which sprints need recalculation after update
        Integer oldSprintId = t.getSprintId();
        Integer oldStatusId = t.getStatusId();

        // title/nome
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) t.setNome(dto.getTitle());

        // resolve project and sprint if provided
        if (dto.getProjectId() != null) {
            var projOpt = projetoRepository.findById(dto.getProjectId());
            if (projOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Projeto não encontrado: id=" + dto.getProjectId());
            t.setProjetoId(projOpt.get().getId());
            t.setProject(projOpt.get().getName());
        }
        if (dto.getSprintId() != null) {
            var sprintOpt = sprintRepository.findById(dto.getSprintId());
            if (sprintOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sprint não encontrada: id=" + dto.getSprintId());
            t.setSprintId(sprintOpt.get().getId());
            t.setSprint(sprintOpt.get().getNome());
        }

        // assignee resolution: if provided, validate membership
        if (dto.getAssigneeId() != null) {
            var userOpt = usuarioRepository.findById(dto.getAssigneeId());
            if (userOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Usuário não encontrado: id=" + dto.getAssigneeId());
            Integer sprintId = t.getSprintId();
        if (sprintId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Tarefa não está associada a uma sprint");
            boolean memberSprint = sprintUsuarioRepository.findByIdSprintId(sprintId).stream().anyMatch(su -> su.getId().getUsuarioId().equals(dto.getAssigneeId()));
            if (!memberSprint) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado à sprint");
            Integer projetoId = t.getProjetoId();
            boolean memberProject = projetoUsuarioRepository.findByIdProjetoId(projetoId).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(dto.getAssigneeId()));
            if (!memberProject) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado ao projeto");
            t.setAssignee(dto.getAssigneeId());
        }

        t.setAvatar(dto.getAvatar() == null ? t.getAvatar() : dto.getAvatar());
        t.setAvatarColor(dto.getAvatarColor() == null ? t.getAvatarColor() : dto.getAvatarColor());

        String priority = null;
        if (dto.getPrioridadeId() != null) {
            var pOpt = prioridadeRepository.findById(dto.getPrioridadeId());
            if (pOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Prioridade não encontrada: id=" + dto.getPrioridadeId());
            t.setPrioridadeId(dto.getPrioridadeId());
            priority = StatusUtils.normalizePriority(pOpt.get().getNome());
        } else {
            priority = StatusUtils.normalizePriority(dto.getPriority());
            if (priority == null) priority = t.getPriority();
        }
        t.setPriority(priority);

        if (dto.getStatusId() != null) {
            var stOpt = statusTarefaRepository.findById(dto.getStatusId());
            if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
            t.setStatusId(dto.getStatusId());
            String statusNorm = StatusUtils.normalizeTaskStatus(stOpt.get().getNome());
            t.setStatus(statusNorm == null ? t.getStatus() : statusNorm);
        }

        t.setDaysDelayed(dto.getDaysDelayed() == null ? (t.getDaysDelayed() == null ? 0 : t.getDaysDelayed()) : dto.getDaysDelayed());

        // plannedEnd: if provided, validate against sprint end date
        if (dto.getPlannedEnd() != null) {
            var sprintOpt = sprintRepository.findById(t.getSprintId());
            if (sprintOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sprint não encontrada: id=" + t.getSprintId());
            var sprint = sprintOpt.get();
            if (dto.getPlannedEnd().isAfter(sprint.getDataFim())) {
                throw new ApiException("A data de entrega deve ser até a data de fim da sprint");
            }
            t.setPlannedEnd(dto.getPlannedEnd());
        }

        t.setEstimatedHours(dto.getEstimatedHours() == null ? t.getEstimatedHours() : dto.getEstimatedHours());
        t.setBlockedBy(dto.getBlockedBy() == null ? t.getBlockedBy() : dto.getBlockedBy());

        // description
        if (dto.getDescription() != null) {
            if (dto.getDescription().length() > 1000) throw new ApiException("Descrição deve ter no máximo 1000 caracteres");
            t.setDescricao(dto.getDescription());
        }

        Tarefa saved = tarefaRepository.save(t);

        // tags: replace existing with provided
        try {
            tarefaTagRepository.deleteByIdTarefaId(saved.getId());
            if (dto.getTagIds() != null) {
                for (Integer tagId : dto.getTagIds()) {
                    if (tagId == null) continue;
                    if (tagRepository.findById(tagId).isEmpty()) throw new ApiException("Tag não encontrada: id=" + tagId);
                    com.avantt_backend.entity.TarefaTag tt = new com.avantt_backend.entity.TarefaTag(saved.getId(), tagId);
                    tarefaTagRepository.save(tt);
                }
            }
        } catch (Exception e) { throw new RuntimeException(e); }

        // build response
        TarefaResponseDTO r = new TarefaResponseDTO();
        r.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        r.setTitle(saved.getNome() == null ? saved.getTitulo() : saved.getNome());
        r.setProject(saved.getProject() == null ? projetoRepository.findById(saved.getProjetoId()).map(com.avantt_backend.entity.Projeto::getName).orElse("") : saved.getProject());
        r.setSprint(saved.getSprint() == null ? sprintRepository.findById(saved.getSprintId()).map(com.avantt_backend.entity.Sprint::getNome).orElse("") : saved.getSprint());
        if (saved.getAssignee() == null) r.setAssignee(""); else r.setAssignee(usuarioRepository.findById(saved.getAssignee()).map(Usuario::getNome).orElse(""));
        r.setAvatar(saved.getAvatar());
        r.setAvatarColor(saved.getAvatarColor());
        r.setPriority(saved.getPriority());
        r.setStatus(saved.getStatus());
        r.setStatusId(saved.getStatusId());
        r.setStatusName(statusTarefaRepository.findById(saved.getStatusId()).map(com.avantt_backend.entity.Status::getNome).orElse(null));
        r.setDaysDelayed(saved.getDaysDelayed() == null ? 0 : saved.getDaysDelayed());
        r.setPlannedEnd(saved.getPlannedEnd());
        r.setEstimatedHours(saved.getEstimatedHours() == null ? 0 : saved.getEstimatedHours());
        r.setBlockedBy(saved.getBlockedBy());
        try {
            var ttags = tarefaTagRepository.findByIdTarefaId(saved.getId());
            var ids = ttags.stream().map(tt -> tt.getId().getTagId()).toList();
            r.setTagIds(ids);
        } catch (Exception e) { r.setTagIds(java.util.Collections.emptyList()); }
        r.setDescription(saved.getDescricao());
        // schedule recalculation for affected sprints after commit
        Integer newSprintId = saved.getSprintId();
        if (oldSprintId != null && !oldSprintId.equals(newSprintId)) scheduleSprintProgressRecalculation(oldSprintId);
        if (newSprintId != null) {
            if (oldStatusId == null || !oldStatusId.equals(saved.getStatusId()) || oldSprintId == null || !oldSprintId.equals(newSprintId)) {
                scheduleSprintProgressRecalculation(newSprintId);
            }
        }

        return r;
    }

    @Transactional
    public TarefaResponseDTO updatePriority(Integer tarefaId, Integer prioridadeId) {
        var tOpt = tarefaRepository.findById(tarefaId);
        if (tOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa não encontrada: id=" + tarefaId);
        var t = tOpt.get();
        if (prioridadeId == null) {
            // unset priority
            t.setPrioridadeId(null);
            t.setPriority("média");
        } else {
            var pOpt = prioridadeRepository.findById(prioridadeId);
            if (pOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Prioridade não encontrada: id=" + prioridadeId);
            t.setPrioridadeId(prioridadeId);
            t.setPriority(StatusUtils.normalizePriority(pOpt.get().getNome()));
        }
        Tarefa saved = tarefaRepository.save(t);
        return toResponse(saved);
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
        if (tOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa não encontrada: id=" + tarefaId);
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
        if (tOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa não encontrada: id=" + tarefaId);
        var t = tOpt.get();

        if (assigneeId == null) {
            // unassign
            t.setAssignee(null);
            Tarefa saved = tarefaRepository.save(t);
            return toResponse(saved);
        }

        var userOpt = usuarioRepository.findById(assigneeId);
        if (userOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Usuário não encontrado: id=" + assigneeId);

        if (t.getSprintId() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Tarefa não está associada a uma sprint");
        boolean memberSprint = sprintUsuarioRepository.findByIdSprintId(t.getSprintId()).stream().anyMatch(su -> su.getId().getUsuarioId().equals(assigneeId));
        if (!memberSprint) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado à sprint");

        if (t.getProjetoId() == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Tarefa não está associada a um projeto");
        boolean memberProject = projetoUsuarioRepository.findByIdProjetoId(t.getProjetoId()).stream().anyMatch(pu -> pu.getId().getUsuarioId().equals(assigneeId));
        if (!memberProject) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não está associado ao projeto");

        t.setAssignee(assigneeId);
        Tarefa saved = tarefaRepository.save(t);
        return toResponse(saved);
    }

    @Transactional
    public TarefaResponseDTO updateStatus(Integer tarefaId, Integer statusId) {
        var tOpt = tarefaRepository.findById(tarefaId);
        if (tOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Tarefa não encontrada: id=" + tarefaId);
        if (statusId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Status inválido: id=null");

        var stOpt = statusTarefaRepository.findById(statusId);
        if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + statusId);

        var t = tOpt.get();
        t.setStatusId(statusId);
        String statusNorm = StatusUtils.normalizeTaskStatus(stOpt.get().getNome());
        t.setStatus(statusNorm == null ? t.getStatus() : statusNorm);

        Tarefa saved = tarefaRepository.save(t);

        // flush and immediately recalculate sprint progress so caller can get updated value
        try { tarefaRepository.flush(); } catch (Exception ignored) {}
        com.avantt_backend.service.SprintService.ProgressInfo info = sprintService.recalculateAndPersistProgress(saved.getSprintId());

        TarefaResponseDTO response = toResponse(saved);
        if (info != null) response.setSprintProgress(info.progress);
        return response;
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
