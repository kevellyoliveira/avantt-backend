
package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.StatusTarefaRepository;
import com.avantt_backend.util.StatusUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private static final Logger log = LoggerFactory.getLogger(SprintService.class);
    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final SprintUsuarioRepository sprintUsuarioRepository;
    private final StatusTarefaRepository statusTarefaRepository;

    private String validatedSprintStatus(Integer statusId) {
        if (statusId == null) {
            throw new ApiException("statusId é obrigatório");
        }

        var status = statusTarefaRepository.findById(statusId)
                .orElseThrow(() -> new ApiException("Status de sprint não encontrado: id=" + statusId));
        return validatedSprintStatus(status.getNome());
    }

    private String validatedSprintStatus(String status) {
        String normalized = status == null ? "" : status.trim().toLowerCase(Locale.ROOT);

        return switch (normalized) {
            case "planejada" -> "Planejada";
            case "em andamento" -> "Em andamento";
            case "concluída", "concluida", "finalizada" -> "Finalizada";
            default -> throw new ApiException("Status de sprint inválido: " + status);
        };
    }

    public SprintService(
            SprintRepository sprintRepository,
            ProjetoRepository projetoRepository,
            TarefaRepository tarefaRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository,
            UsuarioRepository usuarioRepository,
            StatusTarefaRepository statusTarefaRepository,
            SprintUsuarioRepository sprintUsuarioRepository) {

        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.tarefaRepository = tarefaRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.statusTarefaRepository = statusTarefaRepository;
        this.sprintUsuarioRepository = sprintUsuarioRepository;
    }

    public static class ProgressInfo {
        public final int total;
        public final int done;
        public final int progress;

        public ProgressInfo(int total, int done, int progress) {
            this.total = total;
            this.done = done;
            this.progress = progress;
        }
    }

    private double getSprintElapsedPercent(Integer sprintId) {
        if (sprintId == null) return 0;
        return sprintRepository.findById(sprintId)
                .map(sprint -> {
                    if (sprint.getDataInicio() == null || sprint.getDataFim() == null) return 0.0;
                    long duration = ChronoUnit.DAYS.between(sprint.getDataInicio(), sprint.getDataFim());
                    if (duration <= 0) return 100.0;
                    long elapsed = ChronoUnit.DAYS.between(sprint.getDataInicio(), LocalDate.now());
                    elapsed = Math.max(0, elapsed);
                    elapsed = Math.min(duration, elapsed);
                    return elapsed * 100.0 / duration;
                })
                .orElse(0.0);
    }

    /**
     * Calcula o progresso da sprint.
     *
     * Tarefas canceladas não entram no total.
     * Tarefas concluídas são contabilizadas como concluídas.
     * O percentual é calculado sobre as tarefas não canceladas.
     */
    private ProgressInfo computeProgressInfo(Integer sprintId) {

        if (sprintId == null) {
            return new ProgressInfo(0, 0, 0);
        }

        log.debug("computeProgressInfo: sprintId={}", sprintId);

        List<Tarefa> tasks = tarefaRepository.findBySprintId(sprintId);

        /*
         * Fallback para consultas que não retornaram tarefas.
         * Mantém compatibilidade com os métodos de contagem do repositório.
         */
        if (tasks == null || tasks.isEmpty()) {

            int total = Math.max(0, tarefaRepository.countBySprintId(sprintId));
            int done = Math.max(
                    0,
                    tarefaRepository.countBySprintIdAndStatusName(sprintId, "Done")
            );

            done = Math.min(done, total);

            int progress = total == 0
                    ? 0
                    : (int) Math.round((done * 100.0) / total);

            log.debug(
                    "computeProgressInfo: fallback sprintId={} total={} done={} progress={}",
                    sprintId,
                    total,
                    done,
                    progress
            );

            return new ProgressInfo(total, done, progress);
        }

        int total = 0;
        int started = 0;
        int done = 0;
        double effortProgress = 0;
        double totalEstimated = 0;

        java.util.Set<Integer> statusIds = new java.util.HashSet<>();
        tasks.forEach(task -> {
            if (task.getStatusId() != null) statusIds.add(task.getStatusId());
        });
        java.util.Map<Integer, String> statusNames = new java.util.HashMap<>();
        if (!statusIds.isEmpty()) {
            statusTarefaRepository.findAllById(statusIds)
                    .forEach(status -> statusNames.put(status.getId(), status.getNome()));
        }

        for (Tarefa tarefa : tasks) {
            String statusName = tarefa.getStatus();
            if (tarefa.getStatusId() != null) {
                statusName = statusNames.get(tarefa.getStatusId());
            }
            if (StatusUtils.isCancelled(statusName)) continue;

            total++;
            if (StatusUtils.isDone(statusName)) done++;
            if (tarefa.getDataInicio() != null
                    || (tarefa.getHoursSpent() != null && tarefa.getHoursSpent() > 0)) {
                started++;
            }

            int estimated = tarefa.getEstimatedHours() == null ? 0 : tarefa.getEstimatedHours();
            int spent = tarefa.getHoursSpent() == null ? 0 : tarefa.getHoursSpent();
            if (estimated > 0) {
                totalEstimated += estimated;
                effortProgress += Math.min(100, spent * 100.0 / estimated) * estimated;
            }
        }

        double effortFactor = totalEstimated == 0
                ? 0
                : effortProgress / totalEstimated;
        double quantityFactor = total == 0
                ? 0
                : started * 100.0 / total;
        double timeFactor = getSprintElapsedPercent(sprintId);

        int progress = total == 0
                ? 0
                : (int) Math.round(effortFactor * 0.70 + quantityFactor * 0.20 + timeFactor * 0.10);

        log.info(
                "computeProgressInfo: sprintId={} total={} done={} progress={}",
                sprintId,
                total,
                done,
                progress
        );

        return new ProgressInfo(total, done, progress);
    }

    @Transactional
    public ProgressInfo recalculateAndPersistProgress(Integer sprintId) {

        if (sprintId == null) {
            return new ProgressInfo(0, 0, 0);
        }

        var sprintOptional = sprintRepository.findById(sprintId);

        if (sprintOptional.isEmpty()) {
            return new ProgressInfo(0, 0, 0);
        }

        ProgressInfo info = computeProgressInfo(sprintId);

        Sprint sprint = sprintOptional.get();
        sprint.setProgresso(info.progress);

        sprintRepository.save(sprint);

        log.info(
                "recalculateAndPersistProgress: sprintId={} persistedProgress={}",
                sprintId,
                info.progress
        );

        try {

            Integer projetoId = sprint.getProjetoId();

            if (projetoId != null) {
                recalculateAndPersistProjectProgress(projetoId);
            }

        } catch (Exception e) {

            log.warn(
                    "Falha ao recalcular progresso do projeto da sprint {}: {}",
                    sprintId,
                    e.getMessage()
            );
        }

        return info;
    }

    public static class ProjectProgressInfo {

        public final int totalTasks;
        public final int doneTasks;
        public final int progress;

        public ProjectProgressInfo(int totalTasks, int doneTasks, int progress) {
            this.totalTasks = totalTasks;
            this.doneTasks = doneTasks;
            this.progress = progress;
        }
    }

    /**
     * Calcula o progresso do projeto considerando todas as tarefas,
     * independentemente de estarem vinculadas a uma sprint ou no backlog.
     *
     * Tarefas canceladas não entram no total.
     */
    @Transactional
    public ProjectProgressInfo recalculateAndPersistProjectProgress(Integer projetoId) {

        if (projetoId == null) {
            return new ProjectProgressInfo(0, 0, 0);
        }

        var projetoOptional = projetoRepository.findById(projetoId);

        if (projetoOptional.isEmpty()) {
            return new ProjectProgressInfo(0, 0, 0);
        }

        // Busca todas as tarefas do projeto, inclusive as do backlog.
        List<Tarefa> tarefas = tarefaRepository.findByProjetoId(projetoId);

        if (tarefas == null || tarefas.isEmpty()) {

            var projeto = projetoOptional.get();

            projeto.setProgress(0);
            projetoRepository.save(projeto);

            log.info(
                    "recalculateAndPersistProjectProgress: projetoId={} sem tarefas. progress=0",
                    projetoId
            );

            return new ProjectProgressInfo(0, 0, 0);
        }

        java.util.Set<Integer> statusIds = new java.util.HashSet<>();
        tarefas.forEach(task -> {
            if (task.getStatusId() != null) statusIds.add(task.getStatusId());
        });
        java.util.Map<Integer, String> statusNamesById = new java.util.HashMap<>();
        if (!statusIds.isEmpty()) {
            statusTarefaRepository.findAllById(statusIds)
                    .forEach(status -> statusNamesById.put(status.getId(), status.getNome()));
        }

        int total = 0;
        int done = 0;
        int started = 0;
        double effortProgress = 0;
        double totalEstimated = 0;

        for (Tarefa tarefa : tarefas) {
            String statusName = tarefa.getStatus();
            if (tarefa.getStatusId() != null) {
                statusName = statusNamesById.get(tarefa.getStatusId());
            }
            if (StatusUtils.isCancelled(statusName)) continue;

            total++;
            if (StatusUtils.isDone(statusName)) done++;
            if (tarefa.getDataInicio() != null
                    || (tarefa.getHoursSpent() != null && tarefa.getHoursSpent() > 0)) {
                started++;
            }

            int estimated = tarefa.getEstimatedHours() == null ? 0 : tarefa.getEstimatedHours();
            int spent = tarefa.getHoursSpent() == null ? 0 : tarefa.getHoursSpent();
            if (estimated > 0) {
                totalEstimated += estimated;
                effortProgress += Math.min(100, spent * 100.0 / estimated) * estimated;
            }
        }

        double taskEffort = totalEstimated == 0 ? 0 : effortProgress / totalEstimated;
        double taskQuantity = total == 0 ? 0 : started * 100.0 / total;

        var sprints = sprintRepository.findByProjetoId(projetoId);
        double sprintQuantity = 0;
        if (sprints != null && !sprints.isEmpty()) {
            double sprintSum = 0;
            for (Sprint sprint : sprints) {
                sprintSum += sprint.getProgresso() == null ? 0 : sprint.getProgresso();
            }
            sprintQuantity = sprintSum / sprints.size();
        }

        var projetoTempo = projetoOptional.get();
        double projectTime = 0;
        if (projetoTempo.getStartDate() != null && projetoTempo.getEndDate() != null) {
            long duration = ChronoUnit.DAYS.between(projetoTempo.getStartDate(), projetoTempo.getEndDate());
            long elapsed = duration <= 0 ? 100 : ChronoUnit.DAYS.between(projetoTempo.getStartDate(), LocalDate.now());
            elapsed = Math.max(0, elapsed);
            elapsed = duration <= 0 ? 100 : Math.min(duration, elapsed);
            projectTime = duration <= 0 ? 100 : elapsed * 100.0 / duration;
        }

        int progress = total == 0
                ? 0
                : (int) Math.round(taskEffort * 0.60 + taskQuantity * 0.20 + sprintQuantity * 0.10 + projectTime * 0.10);

        var projeto = projetoOptional.get();

        projeto.setProgress(progress);
        projetoRepository.save(projeto);

        log.info(
                "recalculateAndPersistProjectProgress: projetoId={} totalTasks={} doneTasks={} progress={}",
                projetoId,
                total,
                done,
                progress
        );

        return new ProjectProgressInfo(total, done, progress);
    }

    @Transactional
    public SprintResponseDTO update(Integer sprintId, SprintRequestDTO dto) {

        var sprintOptional = sprintRepository.findById(sprintId);

        if (sprintOptional.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Sprint não encontrada: id=" + sprintId
            );
        }

        Sprint sprint = sprintOptional.get();
        String sprintStatus = dto.getStatusId() == null
                ? sprint.getStatus()
                : validatedSprintStatus(dto.getStatusId());

        if (dto.getProjectId() != null
                && !dto.getProjectId().equals(sprint.getProjetoId())) {
            throw new ApiException("Não é permitido alterar o projeto da sprint");
        }

        if (dto.getName() != null) {
            sprint.setNome(dto.getName());
        }
        if (dto.getGoal() != null) {
            sprint.setObjetivo(dto.getGoal());
        }
        sprint.setStatus(sprintStatus);

        var projetoOptional = projetoRepository.findById(sprint.getProjetoId());

        if (projetoOptional.isEmpty()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.NOT_FOUND,
                    "Projeto não encontrado: id=" + sprint.getProjetoId()
            );
        }

        var projeto = projetoOptional.get();

        LocalDate newStart = dto.getStartDate() == null
                ? sprint.getDataInicio()
                : dto.getStartDate();

        LocalDate newEnd = dto.getEndDate() == null
                ? sprint.getDataFim()
                : dto.getEndDate();

        boolean periodChanged = !java.util.Objects.equals(newStart, sprint.getDataInicio())
                || !java.util.Objects.equals(newEnd, sprint.getDataFim());

        if (periodChanged && newStart != null
                && projeto.getStartDate() != null
                && newStart.isBefore(projeto.getStartDate())) {
            throw new ApiException(
                    "A data de início da sprint não pode ser anterior à data de início do projeto"
            );
        }

        if (periodChanged && newStart != null
                && newEnd != null
                && newEnd.isBefore(newStart.plusDays(15))) {
            throw new ApiException(
                    "A data de fim da Sprint deve ser pelo menos 15 dias após a data de início"
            );
        }

        if (periodChanged && newEnd != null
                && projeto.getEndDate() != null
                && newEnd.isAfter(projeto.getEndDate())) {
            throw new ApiException(
                    "A data de fim da sprint não pode ser posterior à data de fim do projeto"
            );
        }

        sprint.setDataInicio(newStart);
        sprint.setDataFim(newEnd);

        Sprint saved = sprintRepository.save(sprint);

        List<Integer> addTeam = dto.getAddTeam();
        List<Integer> removeTeam = dto.getRemoveTeam();
        if (dto.getTeam() != null) {
            Set<Integer> currentTeam = sprintUsuarioRepository.findByIdSprintId(saved.getId())
                    .stream()
                    .map(item -> item.getId().getUsuarioId())
                    .collect(java.util.stream.Collectors.toSet());
            Set<Integer> desiredTeam = new java.util.HashSet<>(dto.getTeam());
            addTeam = desiredTeam.stream().filter(id -> !currentTeam.contains(id)).collect(Collectors.toList());
            removeTeam = currentTeam.stream().filter(id -> !desiredTeam.contains(id)).collect(Collectors.toList());
        }

        if (addTeam != null && !addTeam.isEmpty()) {

            var projetoUsuarios =
                    projetoUsuarioRepository.findByIdProjetoId(sprint.getProjetoId());

            java.util.Set<Integer> projectMemberIds = new java.util.HashSet<>();

            for (var projetoUsuario : projetoUsuarios) {
                projectMemberIds.add(projetoUsuario.getId().getUsuarioId());
            }

            for (Integer usuarioId : addTeam) {

                if (usuarioId == null) {
                    throw new ApiException("Usuário inválido na lista de team");
                }

                if (usuarioRepository.findById(usuarioId).isEmpty()) {
                    throw new ApiException("Usuário não encontrado: id=" + usuarioId);
                }

                if (!projectMemberIds.contains(usuarioId)) {
                    throw new ApiException("Usuário não pertence ao projeto: id=" + usuarioId);
                }

                var id = new com.avantt_backend.entity.SprintUsuarioId(
                        saved.getId(),
                        usuarioId
                );

                if (!sprintUsuarioRepository.existsById(id)) {

                    var sprintUsuario = new com.avantt_backend.entity.SprintUsuario();
                    sprintUsuario.setId(id);
                    sprintUsuario.setPapel(null);

                    sprintUsuarioRepository.save(sprintUsuario);
                }
            }
        }

        if (removeTeam != null && !removeTeam.isEmpty()) {

            for (Integer usuarioId : removeTeam) {

                if (usuarioId == null) {
                    continue;
                }

                var id = new com.avantt_backend.entity.SprintUsuarioId(
                        saved.getId(),
                        usuarioId
                );

                if (!sprintUsuarioRepository.existsById(id)) {
                    continue;
                }

                int assigned = tarefaRepository.countBySprintIdAndAssignee(
                        saved.getId(),
                        usuarioId
                );

                if (assigned > 0) {
                    throw new ApiException(
                            "Não é possível remover o usuário: ele está associado a tarefas desta sprint: id="
                                    + usuarioId
                    );
                }

                sprintUsuarioRepository.deleteById(id);
            }
        }

        SprintResponseDTO response = new SprintResponseDTO();

        response.setId(saved.getId());
        response.setName(saved.getNome());
        response.setGoal(saved.getObjetivo());
        response.setStatus(validatedSprintStatus(saved.getStatus()));
        response.setProject(projeto.getName());
        response.setProjectId(projeto.getId());
        response.setStartDate(saved.getDataInicio());
        response.setEndDate(saved.getDataFim());

        ProgressInfo info = recalculateAndPersistProgress(saved.getId());

        int daysDelayed = 0;

        if (saved.getDataFim() != null) {

            LocalDate today = LocalDate.now();

            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(
                        saved.getDataFim(),
                        today
                );
            }
        }

        response.setDaysDelayed(daysDelayed);
        response.setProgress(info.progress);
        response.setTotalTasks(info.total);
        response.setDoneTasks(info.done);
        response.setBlockedTasks(0);

        var sprintUsuarios =
                sprintUsuarioRepository.findByIdSprintId(saved.getId());

        var names = new ArrayList<String>();

        for (var sprintUsuario : sprintUsuarios) {

            Integer usuarioId = sprintUsuario.getId().getUsuarioId();

            usuarioRepository.findById(usuarioId)
                    .ifPresent(usuario -> names.add(usuario.getNome()));
        }

        response.setTeam(names);
        response.setTeamIds(sprintUsuarios.stream()
                .map(item -> item.getId().getUsuarioId())
                .collect(Collectors.toList()));

        return response;
    }

    @Transactional
    public SprintResponseDTO updateStatus(Integer sprintId, Integer statusId) {
        SprintRequestDTO request = new SprintRequestDTO();
        request.setStatusId(statusId);
        return update(sprintId, request);
    }

    @Transactional
    public SprintResponseDTO create(SprintRequestDTO dto) {
        String sprintStatus = validatedSprintStatus(dto.getStatusId());

        if (dto.getProjectId() == null) {
            throw new ApiException("Projeto é obrigatório");
        }
        var projetoOptional = projetoRepository.findById(dto.getProjectId());

        if (projetoOptional.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Projeto não encontrado: id=" + dto.getProjectId()
            );
        }

        var projeto = projetoOptional.get();

        if (dto.getStartDate() == null || dto.getEndDate() == null) {
            throw new ApiException(
                    "As datas de início e fim da sprint são obrigatórias"
            );
        }

        if (projeto.getStartDate() != null
                && dto.getStartDate().isBefore(projeto.getStartDate())) {
            throw new ApiException(
                    "A data de início da sprint não pode ser anterior à data de início do projeto"
            );
        }

        if (dto.getEndDate().isBefore(dto.getStartDate().plusDays(15))) {
            throw new ApiException(
                    "A data de fim da Sprint deve ser pelo menos 15 dias após a data de início"
            );
        }

        if (projeto.getEndDate() != null
                && dto.getEndDate().isAfter(projeto.getEndDate())) {
            throw new ApiException(
                    "A data de fim da sprint não pode ser posterior à data de fim do projeto"
            );
        }

        Sprint sprint = new Sprint();

        sprint.setProjetoId(projeto.getId());
        sprint.setNome(dto.getName());
        sprint.setObjetivo(dto.getGoal());
        sprint.setStatus(sprintStatus);
        sprint.setDataInicio(dto.getStartDate());
        sprint.setDataFim(dto.getEndDate());

        // Inicializa o progresso para evitar NULL na coluna do banco.
        sprint.setProgresso(0);

        Sprint saved = sprintRepository.save(sprint);

        SprintResponseDTO response = new SprintResponseDTO();

        response.setId(saved.getId());
        response.setName(saved.getNome());
        response.setGoal(saved.getObjetivo());
        response.setStatus(validatedSprintStatus(saved.getStatus()));
        response.setProject(projeto.getName());
        response.setProjectId(projeto.getId());
        response.setStartDate(saved.getDataInicio());
        response.setEndDate(saved.getDataFim());

        ProgressInfo info = recalculateAndPersistProgress(saved.getId());

        int daysDelayed = 0;

        if (saved.getDataFim() != null) {

            LocalDate today = LocalDate.now();

            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(
                        saved.getDataFim(),
                        today
                );
            }
        }

        response.setDaysDelayed(
                dto.getDaysDelayed() == null
                        ? daysDelayed
                        : dto.getDaysDelayed()
        );

        response.setProgress(info.progress);

        response.setTotalTasks(
                dto.getTotalTasks() == null
                        ? info.total
                        : dto.getTotalTasks()
        );

        response.setDoneTasks(
                dto.getDoneTasks() == null
                        ? info.done
                        : dto.getDoneTasks()
        );

        response.setBlockedTasks(
                dto.getBlockedTasks() == null
                        ? 0
                        : dto.getBlockedTasks()
        );

        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {

            var names = new ArrayList<String>();

            for (Integer usuarioId : dto.getTeam()) {

                if (usuarioId == null) {
                    continue;
                }

                usuarioRepository.findById(usuarioId)
                        .ifPresent(usuario -> names.add(usuario.getNome()));
            }

            response.setTeam(names);

        } else {

            response.setTeam(new ArrayList<>());
        }

        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {

            var projetoUsuarios =
                    projetoUsuarioRepository.findByIdProjetoId(projeto.getId());

            java.util.Set<Integer> projectMemberIds = new java.util.HashSet<>();

            for (var projetoUsuario : projetoUsuarios) {
                projectMemberIds.add(projetoUsuario.getId().getUsuarioId());
            }

            for (Integer usuarioId : dto.getTeam()) {

                if (usuarioId == null) {
                    throw new ApiException("Usuário inválido na lista de team");
                }

                if (usuarioRepository.findById(usuarioId).isEmpty()) {
                    throw new ApiException("Usuário não encontrado: id=" + usuarioId);
                }

                if (!projectMemberIds.contains(usuarioId)) {
                    throw new ApiException("Usuário não pertence ao projeto: id=" + usuarioId);
                }

                var id = new com.avantt_backend.entity.SprintUsuarioId(
                        saved.getId(),
                        usuarioId
                );

                if (!sprintUsuarioRepository.existsById(id)) {

                    var sprintUsuario = new com.avantt_backend.entity.SprintUsuario();
                    sprintUsuario.setId(id);
                    sprintUsuario.setPapel(null);

                    sprintUsuarioRepository.save(sprintUsuario);
                }
            }
        }

        var savedSprintUsers = sprintUsuarioRepository.findByIdSprintId(saved.getId());
        response.setTeamIds(savedSprintUsers.stream()
                .map(item -> item.getId().getUsuarioId())
                .collect(Collectors.toList()));

        return response;
    }

    public List<SprintResponseDTO> listAll(String projetoId, Integer organizacaoId) {
        if (organizacaoId == null) return listAll(projetoId);
        return listAll(projetoId).stream()
                .filter(sprint -> sprint.getProject() != null
                        && projetoRepository.findByName(sprint.getProject())
                        .map(project -> organizacaoId.equals(project.getOrganizacaoId()))
                        .orElse(false))
                .collect(java.util.stream.Collectors.toList());
    }

    public List<SprintResponseDTO> listAll(String projetoId) {

        List<Sprint> all;

        if (projetoId == null) {

            all = sprintRepository.findAll();

        } else {

            Integer pid = null;

            try {
                String numericId = projetoId.replaceAll("[^0-9]", "");

                if (!numericId.isEmpty()) {
                    pid = Integer.valueOf(numericId);
                }

            } catch (Exception ignored) {
                // Tenta localizar pelo nome abaixo.
            }

            if (pid == null) {

                var projetoOptional = projetoRepository.findByName(projetoId);

                if (projetoOptional.isPresent()) {
                    pid = projetoOptional.get().getId();
                }
            }

            if (pid != null) {
                all = sprintRepository.findByProjetoId(pid);
            } else {
                all = java.util.Collections.emptyList();
            }
        }

        return all.stream().map(sprint -> {

            SprintResponseDTO response = new SprintResponseDTO();

            response.setId(sprint.getId());
            response.setName(sprint.getNome());
            response.setGoal(sprint.getObjetivo());
            response.setStatus(validatedSprintStatus(sprint.getStatus()));
            response.setProjectId(sprint.getProjetoId());

            String projectName = null;

            if (sprint.getProjetoId() != null) {

                var projetoOptional = projetoRepository.findById(sprint.getProjetoId());

                if (projetoOptional.isPresent()) {
                    projectName = projetoOptional.get().getName();
                }
            }

            response.setProject(projectName);
            response.setStartDate(sprint.getDataInicio());
            response.setEndDate(sprint.getDataFim());

            ProgressInfo info = sprint.getId() == null
                    ? new ProgressInfo(0, 0, 0)
                    : computeProgressInfo(sprint.getId());

            int daysDelayed = 0;

            if (sprint.getDataFim() != null) {

                LocalDate today = LocalDate.now();

                if (today.isAfter(sprint.getDataFim())) {
                    daysDelayed = (int) ChronoUnit.DAYS.between(
                            sprint.getDataFim(),
                            today
                    );
                }
            }

            response.setDaysDelayed(daysDelayed);
            response.setProgress(info.progress);
            response.setTotalTasks(info.total);
            response.setDoneTasks(info.done);
            response.setBlockedTasks(0);

            if (sprint.getId() != null) {

                var sprintUsuarios =
                        sprintUsuarioRepository.findByIdSprintId(sprint.getId());

                if (sprintUsuarios != null && !sprintUsuarios.isEmpty()) {

                    var names = new ArrayList<String>();
                    var ids = new ArrayList<Integer>();

                    for (var sprintUsuario : sprintUsuarios) {

                        Integer usuarioId = sprintUsuario.getId().getUsuarioId();
                        ids.add(usuarioId);

                        usuarioRepository.findById(usuarioId)
                                .ifPresent(usuario -> names.add(usuario.getNome()));
                    }

                    response.setTeam(names);
                    response.setTeamIds(ids);

                } else if (sprint.getProjetoId() != null) {

                    response.setTeam(java.util.Collections.emptyList());
                    response.setTeamIds(new ArrayList<>());

                } else {

                    response.setTeam(java.util.Collections.emptyList());
                    response.setTeamIds(new ArrayList<>());
                }

            } else if (sprint.getProjetoId() != null) {

                response.setTeam(java.util.Collections.emptyList());
                response.setTeamIds(new ArrayList<>());

            } else {

                response.setTeam(java.util.Collections.emptyList());
                response.setTeamIds(new ArrayList<>());
            }

            return response;

        }).collect(Collectors.toList());
    }

}
