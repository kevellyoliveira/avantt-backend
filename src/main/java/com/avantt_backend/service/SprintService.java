package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.entity.ProjetoUsuario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import com.avantt_backend.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import com.avantt_backend.entity.Tarefa;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository;
    private final com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository;

    public SprintService(SprintRepository sprintRepository,
                         ProjetoRepository projetoRepository,
                         TarefaRepository tarefaRepository,
                         ProjetoUsuarioRepository projetoUsuarioRepository,
                         UsuarioRepository usuarioRepository,
                         com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository,
                         com.avantt_backend.repository.SprintUsuarioRepository sprintUsuarioRepository) {
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.tarefaRepository = tarefaRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.statusTarefaRepository = statusTarefaRepository;
        this.sprintUsuarioRepository = sprintUsuarioRepository;
    }

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);

    // Regra de cálculo do progresso (explicação):
    // - Seleciona todas as tarefas com tarefa.sprint_id = sprintId.
    // - Exclui do denominador todas as tarefas cujo status é considerado "Cancelado" (StatusUtils.isCancelled).
    // - Conta como "concluída" qualquer tarefa cujo status é considerado concluído (StatusUtils.isDone).
    // - progress% = round(100 * done_count / total_count), onde total_count é o número de tarefas
    //   consideradas (ou seja, sem as canceladas). Se total_count == 0, progress = 0.
    // - O nome do status é obtido preferencialmente do campo Tarefa.status; se estiver vazio e houver
    //   status_id, resolve-se o nome via StatusTarefaRepository (batch lookup para evitar N+1).
    // - computeProgressInfo apenas calcula os valores (total, done, progress) em memória;
    //   recalculateAndPersistProgress grava o resultado em sprint.progresso.
    // - Esta função é chamada automaticamente após criação/atualização de tarefas e também está
    //   disponível via endpoint GET /api/sprints/{id}/progress para recalculo sob demanda.
    // - Resultado persistido: sprint.progresso (INT 0..100).
    // Observação: não usamos story points; o cálculo é por contagem de tarefas, excluindo canceladas.
    // simple holder for progress computation results
    public static class ProgressInfo {
        public final int total;
        public final int done;
        public final int progress; // 0..100
        public ProgressInfo(int total, int done, int progress) { this.total = total; this.done = done; this.progress = progress; }
    }

    // compute progress info for a sprint without persisting
    private ProgressInfo computeProgressInfo(Integer sprintId) {
        if (sprintId == null) return new ProgressInfo(0,0,0);
        log.debug("computeProgressInfo: sprintId={}", sprintId);
        java.util.List<Tarefa> tasks = tarefaRepository.findBySprintId(sprintId);
        if (tasks == null || tasks.isEmpty()) {
            log.debug("computeProgressInfo: sprintId={} no tasks found", sprintId);
            return new ProgressInfo(0,0,0);
        }

        // collect status ids for batch lookup
        java.util.Set<Integer> statusIds = new java.util.HashSet<>();
        for (var t : tasks) { if (t.getStatusId() != null) statusIds.add(t.getStatusId()); }

        java.util.Map<Integer, String> statusNamesById = new java.util.HashMap<>();
        if (!statusIds.isEmpty()) {
            statusTarefaRepository.findAllById(statusIds).forEach(s -> statusNamesById.put(s.getId(), s.getNome()));
        }

        // precompute status flags by id to avoid relying only on string matching
        java.util.Map<Integer, Boolean> statusIsDoneById = new java.util.HashMap<>();
        java.util.Map<Integer, Boolean> statusIsCancelledById = new java.util.HashMap<>();
        for (var entry : statusNamesById.entrySet()) {
            Integer id = entry.getKey();
            String name = entry.getValue();
            statusIsDoneById.put(id, StatusUtils.isDone(name));
            statusIsCancelledById.put(id, StatusUtils.isCancelled(name));
        }

        int total = 0;
        int done = 0;
        for (var t : tasks) {
            String statusName = t.getStatus();
            Integer sid = t.getStatusId();
            if ((statusName == null || statusName.isBlank()) && sid != null) {
                statusName = statusNamesById.get(sid);
            }

            boolean cancelled = false;
            boolean isDone = false;
            if (sid != null && statusNamesById.containsKey(sid)) {
                cancelled = statusIsCancelledById.getOrDefault(sid, false);
                isDone = statusIsDoneById.getOrDefault(sid, false);
            } else {
                cancelled = StatusUtils.isCancelled(statusName);
                isDone = StatusUtils.isDone(statusName);
            }

            log.debug("task id={} status='{}' statusId={} resolvedStatus='{}' cancelled={} done={}", t.getId(), t.getStatus(), sid, statusName, cancelled, isDone);
            // exclude cancelled from denominator
            if (cancelled) continue;
            total++;
            if (isDone) done++;
        }
        int progress = (total == 0) ? 0 : (int) Math.round((done * 100.0) / total);
        log.info("computeProgressInfo: sprintId={} total={} done={} progress={}", sprintId, total, done, progress);
        return new ProgressInfo(total, done, progress);
    }

    @Transactional
    public ProgressInfo recalculateAndPersistProgress(Integer sprintId) {
        if (sprintId == null) return new ProgressInfo(0,0,0);
        var spOpt = sprintRepository.findById(sprintId);
        if (spOpt.isEmpty()) return new ProgressInfo(0,0,0);
        ProgressInfo info = computeProgressInfo(sprintId);
        var sprint = spOpt.get();
        sprint.setProgresso(info.progress);
        sprintRepository.save(sprint);
        log.info("recalculateAndPersistProgress: sprintId={} persistedProgress={}", sprintId, info.progress);
        // also update project-level progress based on sprints
        try {
            Integer projetoId = sprint.getProjetoId();
            if (projetoId != null) {
                recalculateAndPersistProjectProgress(projetoId);
            }
        } catch (Exception e) {
            log.warn("recalculateAndPersistProgress: failed to recalculate project progress for sprintId={}: {}", sprintId, e.getMessage());
        }
        return info;
    }

    // holder for project-level progress
    public static class ProjectProgressInfo {
        public final int totalTasks;
        public final int doneTasks;
        public final int progress; // 0..100
        public ProjectProgressInfo(int totalTasks, int doneTasks, int progress) { this.totalTasks = totalTasks; this.doneTasks = doneTasks; this.progress = progress; }
    }

    @Transactional
    // Regra de cálculo do progresso do projeto (explicação):
    // - Unidade de cálculo: SPRINT. O progresso do projeto é calculado a partir do status
    //   das sprints que pertencem ao projeto (não considera tarefas diretamente).
    // - Para cada sprint do projeto usamos apenas o status da sprint (status_id -> status.nome):
    //     * Sprints com status considerado "Cancelado" (StatusUtils.isCancelled) são EXCLUÍDAS do denominador.
    //     * Sprints com status considerado "Concluído" (StatusUtils.isDone) são contadas como concluídas.
    // - totalTasks no retorno = número de sprints consideradas (ou seja, sprints não canceladas).
    // - doneTasks no retorno = número de sprints consideradas cujo status é "Concluído".
    // - progress% = round(100 * doneTasks / totalTasks). Se totalTasks == 0, progress = 0.
    // - O valor é persistido em projeto.progresso (INT 0..100).
    // - Esta função é @Transactional e é chamada automaticamente após recálculo de uma sprint
    //   (recalculateAndPersistProgress) para manter o projeto em sincronia.
    public ProjectProgressInfo recalculateAndPersistProjectProgress(Integer projetoId) {
        if (projetoId == null) return new ProjectProgressInfo(0,0,0);
        var sprints = sprintRepository.findByProjetoId(projetoId);
        if (sprints == null || sprints.isEmpty()) {
            // persist zero progress
            projetoRepository.findById(projetoId).ifPresent(p -> { p.setProgress(0); projetoRepository.save(p); });
            return new ProjectProgressInfo(0,0,0);
        }

        // Calculate project progress based on sprint progress values (each sprint counts equally)
        // collect status ids for batch lookup
        java.util.Set<Integer> statusIds = new java.util.HashSet<>();
        for (var s : sprints) { if (s.getStatusId() != null) statusIds.add(s.getStatusId()); }

        java.util.Map<Integer, String> statusNamesById = new java.util.HashMap<>();
        if (!statusIds.isEmpty()) {
            statusTarefaRepository.findAllById(statusIds).forEach(st -> statusNamesById.put(st.getId(), st.getNome()));
        }

        int total = 0;
        int done = 0;
        for (var s : sprints) {
            String statusName = null;
            Integer sid = s.getStatusId();
            if (sid != null) statusName = statusNamesById.get(sid);

            // exclude cancelled sprints from denominator
            if (StatusUtils.isCancelled(statusName)) continue;
            total++;
            if (StatusUtils.isDone(statusName)) done++;
        }

        int progress = (total == 0) ? 0 : (int) Math.round((done * 100.0) / total);

        projetoRepository.findById(projetoId).ifPresent(p -> {
            p.setProgress(progress);
            projetoRepository.save(p);
        });

        log.info("recalculateAndPersistProjectProgress: projetoId={} totalSprints={} doneSprints={} progress={}", projetoId, total, done, progress);
        return new ProjectProgressInfo(total, done, progress);
    }

    @Transactional
    public SprintResponseDTO update(Integer sprintId, SprintRequestDTO dto) {
        var spOpt = sprintRepository.findById(sprintId);
        if (spOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sprint não encontrada: id=" + sprintId);
        var s = spOpt.get();

        // cannot change project
        if (dto.getProjectId() != null && !dto.getProjectId().equals(s.getProjetoId())) {
            throw new ApiException("Não é permitido alterar o projeto da sprint");
        }

        // update name
        if (dto.getName() != null) s.setNome(dto.getName());

        // resolve project for date validations
        var projOpt = projetoRepository.findById(s.getProjetoId());
        if (projOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Projeto não encontrado: id=" + s.getProjetoId());
        var projeto = projOpt.get();

        // dates: if provided, validate same rules as create
        LocalDate newStart = (dto.getStartDate() == null) ? s.getDataInicio() : dto.getStartDate();
        LocalDate newEnd = (dto.getEndDate() == null) ? s.getDataFim() : dto.getEndDate();

        if (newStart != null && newStart.isBefore(projeto.getStartDate())) {
            throw new ApiException("A data de início da sprint não pode ser anterior à data de início do projeto");
        }
        if (newEnd != null && newEnd.isBefore(newStart.plusDays(15))) {
            throw new ApiException("A data de fim da Sprint deve ser pelo menos 15 dias após a data de início");
        }
        if (newEnd != null && newEnd.isAfter(projeto.getEndDate())) {
            throw new ApiException("A data de fim da sprint não pode ser posterior à data de fim do projeto");
        }

        s.setDataInicio(newStart);
        s.setDataFim(newEnd);

        // status update
        if (dto.getStatusId() != null) {
            var stOpt = statusTarefaRepository.findById(dto.getStatusId());
            if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
            s.setStatusId(dto.getStatusId());
        }

        Sprint saved = sprintRepository.save(s);

        // handle addTeam
        if (dto.getAddTeam() != null && !dto.getAddTeam().isEmpty()) {
            // load project members ids
            var puList = projetoUsuarioRepository.findByIdProjetoId(s.getProjetoId());
            java.util.Set<Integer> projectMemberIds = new java.util.HashSet<>();
            for (var pu : puList) projectMemberIds.add(pu.getId().getUsuarioId());

            for (Integer usuarioId : dto.getAddTeam()) {
                if (usuarioId == null) throw new ApiException("Usuário inválido na lista de team");
                if (!usuarioRepository.findById(usuarioId).isPresent()) throw new ApiException("Usuário não encontrado: id=" + usuarioId);
                if (!projectMemberIds.contains(usuarioId)) throw new ApiException("Usuário não pertence ao projeto: id=" + usuarioId);

                var id = new com.avantt_backend.entity.SprintUsuarioId(saved.getId(), usuarioId);
                if (!sprintUsuarioRepository.existsById(id)) {
                    var su = new com.avantt_backend.entity.SprintUsuario();
                    su.setId(id);
                    su.setPapel(null);
                    sprintUsuarioRepository.save(su);
                }
            }
        }

        // handle removeTeam
        if (dto.getRemoveTeam() != null && !dto.getRemoveTeam().isEmpty()) {
            for (Integer usuarioId : dto.getRemoveTeam()) {
                if (usuarioId == null) continue;
                var id = new com.avantt_backend.entity.SprintUsuarioId(saved.getId(), usuarioId);
                if (!sprintUsuarioRepository.existsById(id)) continue; // nothing to remove

                // ensure user not assigned to any task in this sprint
                int assigned = tarefaRepository.countBySprintIdAndAssignee(saved.getId(), usuarioId);
                if (assigned > 0) throw new ApiException("Não é possível remover o usuário: ele está associado a tarefas desta sprint: id=" + usuarioId);

                sprintUsuarioRepository.deleteById(id);
            }
        }

        // build response similar to create
        SprintResponseDTO r = new SprintResponseDTO();
        r.setId(saved.getId());
        r.setName(saved.getNome());
        r.setProject(projeto.getName());
        r.setStartDate(saved.getDataInicio());
        r.setEndDate(saved.getDataFim());
        // compute and persist latest progress for this sprint
        ProgressInfo info = recalculateAndPersistProgress(saved.getId());
        int blocked = 0;
        int daysDelayed = 0;
        if (saved.getDataFim() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(saved.getDataFim(), today);
            }
        }
        r.setDaysDelayed(daysDelayed);
        r.setProgress(info.progress);
        r.setTotalTasks(info.total);
        r.setDoneTasks(info.done);
        r.setBlockedTasks(blocked);

        // team members from sprint_usuario
        var sus = sprintUsuarioRepository.findByIdSprintId(saved.getId());
        var names = new java.util.ArrayList<String>();
        for (var su : sus) {
            Integer uid = su.getId().getUsuarioId();
            usuarioRepository.findById(uid).ifPresent(u -> names.add(u.getNome()));
        }
        r.setTeam(names);

        r.setStatusId(saved.getStatusId());
        if (saved.getStatusId() != null) {
            statusTarefaRepository.findById(saved.getStatusId()).ifPresent(st -> r.setStatusName(st.getNome()));
        }

        return r;
    }

    @Transactional
    public SprintResponseDTO updateStatus(Integer sprintId, Integer statusId) {
        var spOpt = sprintRepository.findById(sprintId);
        if (spOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Sprint não encontrada: id=" + sprintId);
        if (statusId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Status inválido: id=null");
        var stOpt = statusTarefaRepository.findById(statusId);
        if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + statusId);

        var s = spOpt.get();
        s.setStatusId(statusId);
        Sprint saved = sprintRepository.save(s);

        // compute progress for response and persist
        ProgressInfo info = recalculateAndPersistProgress(saved.getId());

        SprintResponseDTO r = new SprintResponseDTO();
        r.setId(saved.getId());
        r.setName(saved.getNome());
        AtomicReference<String> projectName = new AtomicReference<>();
        if (saved.getProjetoId() != null) projetoRepository.findById(saved.getProjetoId()).ifPresent(p -> projectName.set(p.getName()));
        r.setProject(projectName.get());
        r.setStartDate(saved.getDataInicio());
        r.setEndDate(saved.getDataFim());
        int blocked = 0;
        int daysDelayed = 0;
        if (saved.getDataFim() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(saved.getDataFim(), today);
            }
        }
        r.setDaysDelayed(daysDelayed);
        r.setProgress(info.progress);
        r.setTotalTasks(info.total);
        r.setDoneTasks(info.done);
        r.setBlockedTasks(blocked);

        // team members
        var sus = sprintUsuarioRepository.findByIdSprintId(saved.getId());
        var names = new java.util.ArrayList<String>();
        for (var su : sus) {
            Integer uid = su.getId().getUsuarioId();
            usuarioRepository.findById(uid).ifPresent(u -> names.add(u.getNome()));
        }
        r.setTeam(names);

        r.setStatusId(saved.getStatusId());
        if (saved.getStatusId() != null) {
            statusTarefaRepository.findById(saved.getStatusId()).ifPresent(st -> r.setStatusName(st.getNome()));
        }

        return r;
    }

    @Transactional
    public SprintResponseDTO create(SprintRequestDTO dto) {
        // Find project by id (avoid ambiguous names) - DTO validation ensures projectId presence
        var projetoOpt = projetoRepository.findById(dto.getProjectId());
        if (projetoOpt.isEmpty())
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Projeto não encontrado: id=" + dto.getProjectId());
        var projeto = projetoOpt.get();

        Sprint s = new Sprint();
        // persist only the columns that exist on sprint table
        s.setProjetoId(projeto.getId());
        s.setNome(dto.getName());
        s.setDataInicio(dto.getStartDate());


        if (dto.getStartDate().isBefore(projeto.getStartDate())) {
            throw new ApiException(
                    "A data de início da sprint não pode ser anterior à data de início do projeto"
            );
        }


        if (dto.getEndDate().isBefore(dto.getStartDate().plusDays(15))) {
            throw new ApiException(
                    "A data de fim da Sprint deve ser pelo menos 15 dias após a data de início"
            );
        }

        if (dto.getEndDate().isAfter(projeto.getEndDate())) {
            throw new ApiException(
                    "A data de fim da sprint não pode ser posterior à data de fim do projeto"
            );
        }


        s.setDataFim(dto.getEndDate());
        // status resolution: dto validation ensures statusId not null
        var stOpt = statusTarefaRepository.findById(dto.getStatusId());
        if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
        s.setStatusId(dto.getStatusId());

        Sprint saved = sprintRepository.save(s);

        // Build response DTO: include non-persisted fields from the incoming DTO or defaults
        SprintResponseDTO r = new SprintResponseDTO();
        r.setId(saved.getId());
        r.setName(saved.getNome());
        r.setProject(projeto.getName());
        r.setStartDate(saved.getDataInicio());
        r.setEndDate(saved.getDataFim());
        // compute and persist initial progress for this sprint
        ProgressInfo info = recalculateAndPersistProgress(saved.getId());
        int blocked = 0; // no explicit blocked flag in schema -> default 0
        int daysDelayed = 0;
        if (saved.getDataFim() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(saved.getDataFim(), today);
            }
        }
        r.setDaysDelayed(dto.getDaysDelayed() == null ? daysDelayed : dto.getDaysDelayed());
        r.setProgress(dto.getProgress() == null ? info.progress : dto.getProgress());
        r.setTotalTasks(dto.getTotalTasks() == null ? info.total : dto.getTotalTasks());
        r.setDoneTasks(dto.getDoneTasks() == null ? info.done : dto.getDoneTasks());
        r.setBlockedTasks(dto.getBlockedTasks() == null ? blocked : dto.getBlockedTasks());
        // team: prefer provided list of ids (resolve to names), otherwise load from projeto_usuario
        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {
            var names = new java.util.ArrayList<String>();
            for (Integer uid : dto.getTeam()) {
                if (uid == null) continue;
                usuarioRepository.findById(uid).ifPresent(u -> names.add(u.getNome()));
            }
            r.setTeam(names);
        } else {
            r.setTeam(loadTeamForProject(projeto.getId()));
        }
        // associate team members to sprint if provided: members must belong to the project
        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {
            // load project members ids
            var puList = projetoUsuarioRepository.findByIdProjetoId(projeto.getId());
            java.util.Set<Integer> projectMemberIds = new java.util.HashSet<>();
            for (var pu : puList) projectMemberIds.add(pu.getId().getUsuarioId());

            for (Integer usuarioId : dto.getTeam()) {
                if (usuarioId == null) throw new ApiException("Usuário inválido na lista de team");
                if (!usuarioRepository.findById(usuarioId).isPresent()) throw new ApiException("Usuário não encontrado: id=" + usuarioId);
                if (!projectMemberIds.contains(usuarioId)) throw new ApiException("Usuário não pertence ao projeto: id=" + usuarioId);

                // create sprint-usuario association if not exists
                var id = new com.avantt_backend.entity.SprintUsuarioId(saved.getId(), usuarioId);
                if (!sprintUsuarioRepository.existsById(id)) {
                    var su = new com.avantt_backend.entity.SprintUsuario();
                    su.setId(id);
                    su.setPapel(null);
                    sprintUsuarioRepository.save(su);
                }
            }
        }

        return r;
    }

    public List<SprintResponseDTO> listAll(String projetoId) {
        List<Sprint> all;
        if (projetoId == null) {
            all = sprintRepository.findAll();
        } else {
            // try to resolve projetoId to numeric id; if it's a name, resolve name->id
            Integer pid = null;
            try {
                pid = Integer.valueOf(projetoId.replaceAll("[^0-9]", ""));
            } catch (Exception ignored) {
            }
            if (pid == null) {
                // not numeric, try to find by name
                projetoRepository.findByName(projetoId).ifPresent(p -> {
                    // set pid via local variable hack
                });
                // try again to resolve by name -> id
                var projOpt = projetoRepository.findByName(projetoId);
                if (projOpt.isPresent()) pid = projOpt.get().getId();
            }
            if (pid != null) all = sprintRepository.findByProjetoId(pid);
            else all = java.util.Collections.emptyList();
        }

            return all.stream().map(s -> {
                SprintResponseDTO r = new SprintResponseDTO();
                r.setId(s.getId());
                r.setName(s.getNome());
            // resolve project name from projeto_id
            String projectName = null;
            if (s.getProjetoId() != null) {
                var p = projetoRepository.findById(s.getProjetoId());
                if (p.isPresent()) projectName = p.get().getName();
            }
            r.setProject(projectName);
            r.setStartDate(s.getDataInicio());
            r.setEndDate(s.getDataFim());
            // status fields: sprint stores statusId in DB; resolve name for response
            r.setStatusId(s.getStatusId());
            if (s.getStatusId() != null) {
                statusTarefaRepository.findById(s.getStatusId()).ifPresent(st -> r.setStatusName(st.getNome()));
            }
            // compute totals (do not persist when listing)
            ProgressInfo info = (s.getId() == null) ? new ProgressInfo(0,0,0) : computeProgressInfo(s.getId());
            int blocked = 0;
            int daysDelayed = 0;
            if (s.getDataFim() != null) {
                LocalDate today = LocalDate.now();
                if (today.isAfter(s.getDataFim())) {
                    daysDelayed = (int) ChronoUnit.DAYS.between(s.getDataFim(), today);
                }
            }
            r.setDaysDelayed(daysDelayed);
            r.setProgress(info.progress);
            r.setTotalTasks(info.total);
            r.setDoneTasks(info.done);
            r.setBlockedTasks(blocked);
            // team: prefer sprint members (sprint_usuario). If none, fall back to project members
            if (s.getId() != null) {
                var sus = sprintUsuarioRepository.findByIdSprintId(s.getId());
                if (sus != null && !sus.isEmpty()) {
                    var names = new java.util.ArrayList<String>();
                    for (var su : sus) {
                        Integer uid = su.getId().getUsuarioId();
                        usuarioRepository.findById(uid).ifPresent(u -> names.add(u.getNome()));
                    }
                    r.setTeam(names);
                } else if (s.getProjetoId() != null) {
                    r.setTeam(loadTeamForProject(s.getProjetoId()));
                } else {
                    r.setTeam(java.util.Collections.emptyList());
                }
            } else if (s.getProjetoId() != null) {
                r.setTeam(loadTeamForProject(s.getProjetoId()));
            } else {
                r.setTeam(java.util.Collections.emptyList());
            }
            return r;
        }).collect(Collectors.toList());
    }

    private java.util.List<String> loadTeamForProject(Integer projetoId) {
        var puList = projetoUsuarioRepository.findByIdProjetoId(projetoId);
        var names = new ArrayList<String>();
        for (ProjetoUsuario pu : puList) {
            Integer uid = pu.getId().getUsuarioId();
            usuarioRepository.findById(uid).ifPresent(u -> names.add(u.getNome()));
        }
        return names;
    }
}
