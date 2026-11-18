
package com.avantt_backend.service;

import com.avantt_backend.dto.TarefaRequestDTO;
import com.avantt_backend.dto.TarefaResponseDTO;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.entity.Status;
import com.avantt_backend.entity.Tarefa;
import com.avantt_backend.entity.Usuario;
import com.avantt_backend.entity.Prioridade;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.exception.ResourceNotFoundException;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.ProjetoUsuarioRepository;
import com.avantt_backend.repository.PrioridadeRepository;
import com.avantt_backend.repository.SprintRepository;
import com.avantt_backend.repository.SprintUsuarioRepository;
import com.avantt_backend.repository.StatusTarefaRepository;
import com.avantt_backend.repository.TagRepository;
import com.avantt_backend.repository.TarefaRepository;
import com.avantt_backend.repository.TarefaTagRepository;
import com.avantt_backend.repository.UsuarioRepository;
import com.avantt_backend.util.StatusUtils;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.stream.Collectors;

@Service
public class TarefaService {

    private static final Logger log =
            LoggerFactory.getLogger(TarefaService.class);

    private final TarefaRepository tarefaRepository;
    private final ProjetoRepository projetoRepository;
    private final SprintRepository sprintRepository;
    private final SprintService sprintService;
    private final UsuarioRepository usuarioRepository;
    private final PrioridadeRepository prioridadeRepository;
    private final StatusTarefaRepository statusTarefaRepository;
    private final SprintUsuarioRepository sprintUsuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final TagRepository tagRepository;
    private final TarefaTagRepository tarefaTagRepository;

    public TarefaService(
            TarefaRepository tarefaRepository,
            ProjetoRepository projetoRepository,
            SprintRepository sprintRepository,
            SprintService sprintService,
            UsuarioRepository usuarioRepository,
            PrioridadeRepository prioridadeRepository,
            StatusTarefaRepository statusTarefaRepository,
            SprintUsuarioRepository sprintUsuarioRepository,
            ProjetoUsuarioRepository projetoUsuarioRepository,
            TagRepository tagRepository,
            TarefaTagRepository tarefaTagRepository) {

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

    /*
     * Recalcula o progresso da sprint.
     */
    private void scheduleSprintProgressRecalculation(Integer sprintId) {

        if (sprintId == null) {
            return;
        }

        try {
            log.debug(
                    "Atualizando alterações antes do recálculo da sprint {}",
                    sprintId
            );

            tarefaRepository.flush();

        } catch (Exception e) {
            log.warn(
                    "Falha ao atualizar tarefas da sprint {}: {}",
                    sprintId,
                    e.getMessage()
            );
        }

        try {
            sprintService.recalculateAndPersistProgress(sprintId);

        } catch (Exception e) {
            log.warn(
                    "Falha ao recalcular progresso da sprint {}: {}",
                    sprintId,
                    e.getMessage()
            );
        }
    }

    /*
     * Recalcula o progresso do projeto.
     * Utilizado principalmente para tarefas que estão no backlog.
     */
    private void scheduleProjectProgressRecalculation(Integer projetoId) {

        if (projetoId == null) {
            return;
        }

        try {
            tarefaRepository.flush();

            sprintService.recalculateAndPersistProjectProgress(projetoId);

        } catch (Exception e) {
            log.warn(
                    "Falha ao recalcular progresso do projeto {}: {}",
                    projetoId,
                    e.getMessage()
            );
        }
    }

    /*
     * Criação de tarefa.
     */
    @Transactional
    public TarefaResponseDTO create(TarefaRequestDTO dto) {

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "O título da tarefa é obrigatório"
            );
        }

        log.info(
                "ASSIGNEE ID RECEBIDO NO BACKEND: {}",
                dto.getAssigneeId()
        );

        // Valida projeto.
        var projOpt = projetoRepository.findById(dto.getProjectId());

        if (projOpt.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Projeto não encontrado: id=" + dto.getProjectId()
            );
        }

        var proj = projOpt.get();

        /*
         * A sprint é opcional.
         * Quando sprintId for null, a tarefa será criada no backlog.
         */
        Sprint sprint = null;

        if (dto.getSprintId() != null) {

            var sprintOpt = sprintRepository.findById(dto.getSprintId());

            if (sprintOpt.isEmpty()) {
                throw new ResourceNotFoundException(
                        "Sprint não encontrada: id=" + dto.getSprintId()
                );
            }

            sprint = sprintOpt.get();

            if (!proj.getId().equals(sprint.getProjetoId())) {
                throw new ApiException(
                        "A sprint informada não pertence ao projeto selecionado"
                );
            }
        }

        // Resolve responsável.
        Integer assigneeId = null;

        if (dto.getAssigneeId() != null) {

            var userOpt = usuarioRepository.findById(dto.getAssigneeId());

            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado: id=" + dto.getAssigneeId()
                );
            }

            assigneeId = dto.getAssigneeId();

            final Integer resolvedAssigneeId = assigneeId;

            /*
             * OPÇÃO A:
             * O responsável precisa pertencer ao projeto,
             * mas não precisa estar associado à sprint.
             */
            Integer projetoId = proj.getId();

            boolean memberProject =
                    projetoUsuarioRepository.findByIdProjetoId(projetoId)
                            .stream()
                            .anyMatch(pu ->
                                    pu.getId().getUsuarioId()
                                            .equals(resolvedAssigneeId)
                            );

            if (!memberProject) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Usuário não está associado ao projeto"
                );
            }
        }

        // Cria tarefa.
        Tarefa t = new Tarefa();

        t.setTitulo(dto.getTitle());
        t.setNome(dto.getTitle());

        t.setProjetoId(proj.getId());
        t.setSprintId(sprint != null ? sprint.getId() : null);

        t.setProject(proj.getName());
        t.setSprint(sprint != null ? sprint.getNome() : null);

        // Responsável.
        t.setAssignee(assigneeId);

        t.setAvatar(dto.getAvatar() == null ? "" : dto.getAvatar());

        t.setAvatarColor(
                dto.getAvatarColor() == null ? "" : dto.getAvatarColor()
        );

        // Prioridade.
        if (dto.getPrioridadeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A prioridade da tarefa é obrigatória"
            );
        }

        var pOpt = prioridadeRepository.findById(dto.getPrioridadeId());

        if (pOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Prioridade não encontrada: id=" + dto.getPrioridadeId()
            );
        }

        t.setPrioridadeId(dto.getPrioridadeId());

        String priority =
                StatusUtils.normalizePriority(pOpt.get().getNome());

        t.setPriority(priority == null ? "média" : priority);

        // Status.
        if (dto.getStatusId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "O status da tarefa é obrigatório"
            );
        }

        var stOpt = statusTarefaRepository.findById(dto.getStatusId());

        if (stOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Status não encontrado: id=" + dto.getStatusId()
            );
        }

        t.setStatusId(dto.getStatusId());

        String statusNorm =
                StatusUtils.normalizeTaskStatus(stOpt.get().getNome());

        t.setStatus(
                statusNorm == null ? "planejada" : statusNorm
        );

        // Data de entrega.
        if (dto.getPlannedEnd() != null) {

            if (sprint != null
                    && sprint.getDataFim() != null
                    && dto.getPlannedEnd().isAfter(sprint.getDataFim())) {

                throw new ApiException(
                        "A data de entrega deve ser até a data de fim da sprint"
                );
            }

            t.setPlannedEnd(dto.getPlannedEnd());
        }

        // Horas estimadas.
        t.setEstimatedHours(
                dto.getEstimatedHours() == null
                        ? 0
                        : dto.getEstimatedHours()
        );

        // Dias de atraso.
        t.setDaysDelayed(
                dto.getDaysDelayed() == null
                        ? 0
                        : dto.getDaysDelayed()
        );

        t.setBlockedBy(dto.getBlockedBy());

        t.setDescricao(
                dto.getDescription() == null
                        ? null
                        : dto.getDescription()
        );

        // Persiste a tarefa.
        Tarefa saved = tarefaRepository.save(t);

        tarefaRepository.flush();

        log.info(
                "ASSIGNEE ID SALVO NA TAREFA {}: {}",
                saved.getId(),
                saved.getAssignee()
        );

        // Persiste tags.
        salvarTags(saved.getId(), dto.getTagIds());

        // Recalcula progresso da sprint ou do projeto.
        if (saved.getSprintId() != null) {
            scheduleSprintProgressRecalculation(saved.getSprintId());
        } else {
            scheduleProjectProgressRecalculation(saved.getProjetoId());
        }

        return toResponse(saved);
    }

    /*
     * Atualização de tarefa.
     */
    @Transactional
    public TarefaResponseDTO update(
            Integer tarefaId,
            TarefaRequestDTO dto) {

        var tOpt = tarefaRepository.findById(tarefaId);

        if (tOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada: id=" + tarefaId
            );
        }

        Tarefa t = tOpt.get();

        Integer oldSprintId = t.getSprintId();
        Integer oldStatusId = t.getStatusId();
        Integer oldProjetoId = t.getProjetoId();

        // Atualiza título.
        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            t.setTitulo(dto.getTitle());
            t.setNome(dto.getTitle());
        }

        // Atualiza projeto.
        if (dto.getProjectId() != null) {

            var projOpt = projetoRepository.findById(dto.getProjectId());

            if (projOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Projeto não encontrado: id=" + dto.getProjectId()
                );
            }

            t.setProjetoId(projOpt.get().getId());
            t.setProject(projOpt.get().getName());
        }

        // Atualiza sprint.
        if (dto.getSprintId() != null) {

            var sprintOpt = sprintRepository.findById(dto.getSprintId());

            if (sprintOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Sprint não encontrada: id=" + dto.getSprintId()
                );
            }

            Sprint sprint = sprintOpt.get();

            if (!t.getProjetoId().equals(sprint.getProjetoId())) {
                throw new ApiException(
                        "A sprint informada não pertence ao projeto da tarefa"
                );
            }

            t.setSprintId(sprint.getId());
            t.setSprint(sprint.getNome());
        }

        // Atualiza responsável.
        if (dto.getAssigneeId() != null) {

            var userOpt = usuarioRepository.findById(dto.getAssigneeId());

            if (userOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuário não encontrado: id=" + dto.getAssigneeId()
                );
            }

            /*
             * OPÇÃO A:
             * Não exige associação do responsável à sprint.
             * Exige somente que ele seja membro do projeto da tarefa.
             */
            Integer projetoId = t.getProjetoId();

            boolean memberProject =
                    projetoUsuarioRepository.findByIdProjetoId(projetoId)
                            .stream()
                            .anyMatch(pu ->
                                    pu.getId().getUsuarioId()
                                            .equals(dto.getAssigneeId())
                            );

            if (!memberProject) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Usuário não está associado ao projeto"
                );
            }

            t.setAssignee(dto.getAssigneeId());
        }

        // Avatar e cor.
        if (dto.getAvatar() != null) {
            t.setAvatar(dto.getAvatar());
        }

        if (dto.getAvatarColor() != null) {
            t.setAvatarColor(dto.getAvatarColor());
        }

        // Prioridade.
        if (dto.getPrioridadeId() != null) {

            var pOpt = prioridadeRepository.findById(dto.getPrioridadeId());

            if (pOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Prioridade não encontrada: id=" + dto.getPrioridadeId()
                );
            }

            t.setPrioridadeId(dto.getPrioridadeId());

            String priority =
                    StatusUtils.normalizePriority(pOpt.get().getNome());

            t.setPriority(priority == null ? "média" : priority);

        } else if (dto.getPriority() != null) {

            String priority =
                    StatusUtils.normalizePriority(dto.getPriority());

            t.setPriority(
                    priority == null ? t.getPriority() : priority
            );
        }

        // Status.
        if (dto.getStatusId() != null) {

            var stOpt = statusTarefaRepository.findById(dto.getStatusId());

            if (stOpt.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Status não encontrado: id=" + dto.getStatusId()
                );
            }

            t.setStatusId(dto.getStatusId());

            String statusNorm =
                    StatusUtils.normalizeTaskStatus(stOpt.get().getNome());

            t.setStatus(
                    statusNorm == null ? t.getStatus() : statusNorm
            );
        }

        // Dias de atraso.
        if (dto.getDaysDelayed() != null) {
            t.setDaysDelayed(dto.getDaysDelayed());
        }

        // Data de entrega.
        if (dto.getPlannedEnd() != null) {

            if (t.getSprintId() != null) {

                var sprintOpt = sprintRepository.findById(t.getSprintId());

                if (sprintOpt.isEmpty()) {
                    throw new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Sprint não encontrada: id=" + t.getSprintId()
                    );
                }

                if (sprintOpt.get().getDataFim() != null
                        && dto.getPlannedEnd().isAfter(
                        sprintOpt.get().getDataFim())) {

                    throw new ApiException(
                            "A data de entrega deve ser até a data de fim da sprint"
                    );
                }
            }

            t.setPlannedEnd(dto.getPlannedEnd());
        }

        // Horas estimadas.
        if (dto.getEstimatedHours() != null) {
            t.setEstimatedHours(dto.getEstimatedHours());
        }

        // Bloqueio.
        if (dto.getBlockedBy() != null) {
            t.setBlockedBy(dto.getBlockedBy());
        }

        // Descrição.
        if (dto.getDescription() != null) {

            if (dto.getDescription().length() > 1000) {
                throw new ApiException(
                        "Descrição deve ter no máximo 1000 caracteres"
                );
            }

            t.setDescricao(dto.getDescription());
        }

        Tarefa saved = tarefaRepository.save(t);

        tarefaRepository.flush();

        // Atualiza tags.
        if (dto.getTagIds() != null) {
            salvarTags(saved.getId(), dto.getTagIds());
        }

        Integer newSprintId = saved.getSprintId();
        Integer newProjetoId = saved.getProjetoId();

        boolean statusAlterado =
                oldStatusId == null
                        || !oldStatusId.equals(saved.getStatusId());

        boolean sprintAlterada =
                oldSprintId == null
                        ? newSprintId != null
                        : !oldSprintId.equals(newSprintId);

        boolean projetoAlterado =
                oldProjetoId == null
                        ? newProjetoId != null
                        : !oldProjetoId.equals(newProjetoId);

        if (oldSprintId != null
                && !oldSprintId.equals(newSprintId)) {

            scheduleSprintProgressRecalculation(oldSprintId);
        }

        if (newSprintId != null
                && (statusAlterado || sprintAlterada || projetoAlterado)) {

            scheduleSprintProgressRecalculation(newSprintId);
        }

        if (newSprintId == null
                && (statusAlterado || projetoAlterado || sprintAlterada)) {

            scheduleProjectProgressRecalculation(newProjetoId);
        }

        if (projetoAlterado && oldProjetoId != null) {
            scheduleProjectProgressRecalculation(oldProjetoId);
        }

        return toResponse(saved);
    }

    /*
     * Atualiza prioridade.
     */
    @Transactional
    public TarefaResponseDTO updatePriority(
            Integer tarefaId,
            Integer prioridadeId) {

        var tOpt = tarefaRepository.findById(tarefaId);

        if (tOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada: id=" + tarefaId
            );
        }

        if (prioridadeId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A prioridade da tarefa é obrigatória"
            );
        }

        var pOpt = prioridadeRepository.findById(prioridadeId);

        if (pOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Prioridade não encontrada: id=" + prioridadeId
            );
        }

        Tarefa t = tOpt.get();

        t.setPrioridadeId(prioridadeId);

        String priority =
                StatusUtils.normalizePriority(pOpt.get().getNome());

        t.setPriority(priority == null ? "média" : priority);

        Tarefa saved = tarefaRepository.save(t);

        return toResponse(saved);
    }

    /*
     * Lista tarefas com filtros opcionais.
     */
    public List<TarefaResponseDTO> listAll(
            String projetoId,
            String sprintId,
            String status) {

        List<Tarefa> all = tarefaRepository.findAll();

        // Filtro por projeto.
        if (projetoId != null) {

            Integer pid = null;

            try {
                pid = Integer.valueOf(
                        projetoId.replaceAll("[^0-9]", "")
                );
            } catch (Exception ignored) {
            }

            if (pid != null) {

                Integer finalPid = pid;

                all = all.stream()
                        .filter(t ->
                                t.getProjetoId() != null
                                        && t.getProjetoId().equals(finalPid)
                        )
                        .collect(Collectors.toList());

            } else {

                var projOpt = projetoRepository.findByName(projetoId);

                if (projOpt.isPresent()) {

                    Integer idProjeto = projOpt.get().getId();

                    all = all.stream()
                            .filter(t ->
                                    t.getProjetoId() != null
                                            && t.getProjetoId().equals(idProjeto)
                            )
                            .collect(Collectors.toList());

                } else {
                    all = Collections.emptyList();
                }
            }
        }

        // Filtro por sprint.
        if (sprintId != null) {

            Integer sid = null;

            try {
                sid = Integer.valueOf(
                        sprintId.replaceAll("[^0-9]", "")
                );
            } catch (Exception ignored) {
            }

            if (sid != null) {

                Integer finalSid = sid;

                all = all.stream()
                        .filter(t ->
                                t.getSprintId() != null
                                        && t.getSprintId().equals(finalSid)
                        )
                        .collect(Collectors.toList());

            } else {

                var sOpt = sprintRepository.findByNome(sprintId);

                if (sOpt.isPresent()) {

                    Integer idSprint = sOpt.get().getId();

                    all = all.stream()
                            .filter(t ->
                                    t.getSprintId() != null
                                            && t.getSprintId().equals(idSprint)
                            )
                            .collect(Collectors.toList());

                } else {
                    all = Collections.emptyList();
                }
            }
        }

        // Filtro por status.
        if (status != null) {

            Integer statusIdFilter = null;

            try {
                statusIdFilter = Integer.valueOf(
                        status.replaceAll("[^0-9]", "")
                );
            } catch (Exception ignored) {
            }

            if (statusIdFilter != null) {

                Integer finalStatusId = statusIdFilter;

                all = all.stream()
                        .filter(t ->
                                t.getStatusId() != null
                                        && t.getStatusId().equals(finalStatusId)
                        )
                        .collect(Collectors.toList());

            } else {

                String normFilter =
                        StatusUtils.normalizeTaskStatus(status);

                all = all.stream()
                        .filter(t -> {

                            String taskStatus = null;

                            if (t.getStatusId() != null) {
                                taskStatus =
                                        statusTarefaRepository.findById(
                                                        t.getStatusId()
                                                )
                                                .map(Status::getNome)
                                                .orElse(null);
                            }

                            String taskNorm =
                                    StatusUtils.normalizeTaskStatus(taskStatus);

                            return normFilter != null
                                    && normFilter.equals(taskNorm);
                        })
                        .collect(Collectors.toList());
            }
        }

        return all.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /*
     * Lista usuários associados ao projeto da tarefa.
     * Não exige que o usuário esteja associado à sprint.
     */
    public List<Map<String, Object>> listUsersForTask(Integer tarefaId) {

        var tOpt = tarefaRepository.findById(tarefaId);

        if (tOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada: id=" + tarefaId
            );
        }

        Tarefa t = tOpt.get();

        if (t.getProjetoId() == null) {
            return Collections.emptyList();
        }

        var membrosProjeto =
                projetoUsuarioRepository.findByIdProjetoId(t.getProjetoId());

        List<Map<String, Object>> out = new ArrayList<>();

        for (var membro : membrosProjeto) {

            Integer uid = membro.getId().getUsuarioId();

            usuarioRepository.findById(uid).ifPresent(u -> {

                Map<String, Object> m = new HashMap<>();

                m.put("id", u.getId());
                m.put("nome", u.getNome());
                m.put("email", u.getEmail());

                out.add(m);
            });
        }

        return out;
    }

    /*
     * Atualiza responsável.
     */
    @Transactional
    public TarefaResponseDTO updateAssignee(
            Integer tarefaId,
            Integer assigneeId) {

        var tOpt = tarefaRepository.findById(tarefaId);

        if (tOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada: id=" + tarefaId
            );
        }

        Tarefa t = tOpt.get();

        if (assigneeId == null) {

            t.setAssignee(null);

            Tarefa saved = tarefaRepository.save(t);
            tarefaRepository.flush();

            return toResponse(saved);
        }

        var userOpt = usuarioRepository.findById(assigneeId);

        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuário não encontrado: id=" + assigneeId
            );
        }

        /*
         * OPÇÃO A:
         * O responsável precisa pertencer ao projeto da tarefa.
         * Não precisa estar associado à sprint.
         */
        if (t.getProjetoId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Tarefa não está associada a um projeto"
            );
        }

        boolean memberProject =
                projetoUsuarioRepository.findByIdProjetoId(t.getProjetoId())
                        .stream()
                        .anyMatch(pu ->
                                pu.getId().getUsuarioId().equals(assigneeId)
                        );

        if (!memberProject) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuário não está associado ao projeto"
            );
        }

        t.setAssignee(assigneeId);

        Tarefa saved = tarefaRepository.save(t);
        tarefaRepository.flush();

        return toResponse(saved);
    }

    /*
     * Atualiza status.
     */
    @Transactional
    public TarefaResponseDTO updateStatus(
            Integer tarefaId,
            Integer statusId) {

        var tOpt = tarefaRepository.findById(tarefaId);

        if (tOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Tarefa não encontrada: id=" + tarefaId
            );
        }

        if (statusId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status inválido: id=null"
            );
        }

        var stOpt = statusTarefaRepository.findById(statusId);

        if (stOpt.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Status não encontrado: id=" + statusId
            );
        }

        Tarefa t = tOpt.get();

        t.setStatusId(statusId);

        String statusNorm =
                StatusUtils.normalizeTaskStatus(stOpt.get().getNome());

        t.setStatus(
                statusNorm == null ? "planejada" : statusNorm
        );

        Tarefa saved = tarefaRepository.save(t);

        tarefaRepository.flush();

        TarefaResponseDTO response = toResponse(saved);

        if (saved.getSprintId() != null) {

            SprintService.ProgressInfo info =
                    sprintService.recalculateAndPersistProgress(
                            saved.getSprintId()
                    );

            if (info != null) {
                response.setSprintProgress(info.progress);
            }

        } else {

            scheduleProjectProgressRecalculation(saved.getProjetoId());
        }

        return response;
    }

    /*
     * Salva as tags na tabela tarefa_tag.
     */
    private void salvarTags(Integer tarefaId, List<Integer> tagIds) {

        tarefaTagRepository.deleteByIdTarefaId(tarefaId);

        if (tagIds == null) {
            return;
        }

        for (Integer tagId : tagIds) {

            if (tagId == null) {
                continue;
            }

            if (tagRepository.findById(tagId).isEmpty()) {
                throw new ApiException(
                        "Tag não encontrada: id=" + tagId
                );
            }

            com.avantt_backend.entity.TarefaTag tt =
                    new com.avantt_backend.entity.TarefaTag(
                            tarefaId,
                            tagId
                    );

            tarefaTagRepository.save(tt);
        }
    }

    /*
     * Converte a entidade em DTO de resposta.
     */
    private TarefaResponseDTO toResponse(Tarefa t) {

        TarefaResponseDTO r = new TarefaResponseDTO();

        r.setId(
                t.getId() == null
                        ? null
                        : String.valueOf(t.getId())
        );

        // Título.
        r.setTitle(
                t.getTitulo() == null ? "" : t.getTitulo()
        );

        // Identificadores do projeto e da sprint.
        r.setProjectId(t.getProjetoId());
        r.setSprintId(t.getSprintId());

        // Projeto.
        String project = t.getProject();

        if ((project == null || project.isBlank())
                && t.getProjetoId() != null) {

            project = projetoRepository.findById(t.getProjetoId())
                    .map(Projeto::getName)
                    .orElse("");
        }

        r.setProject(project == null ? "" : project);

        // Sprint.
        String sprint = t.getSprint();

        if ((sprint == null || sprint.isBlank())
                && t.getSprintId() != null) {

            sprint = sprintRepository.findById(t.getSprintId())
                    .map(Sprint::getNome)
                    .orElse("");
        }

        r.setSprint(sprint == null ? "" : sprint);

        // Responsável.
        Integer assigneeId = t.getAssignee();

        String assigneeName = "";

        if (assigneeId != null) {
            assigneeName = usuarioRepository.findById(assigneeId)
                    .map(Usuario::getNome)
                    .orElse("");
        }

        r.setAssignee(assigneeName);
        r.setAssigneeId(assigneeId);

        // Avatar e cor.
        r.setAvatar(
                t.getAvatar() == null ? "" : t.getAvatar()
        );

        r.setAvatarColor(
                t.getAvatarColor() == null
                        || t.getAvatarColor().isBlank()
                        ? "#4B7BF5"
                        : t.getAvatarColor()
        );

        // Prioridade.
        String priority = null;

        if (t.getPrioridadeId() != null) {
            priority = prioridadeRepository.findById(t.getPrioridadeId())
                    .map(Prioridade::getNome)
                    .orElse(null);
        }

        String normPri = StatusUtils.normalizePriority(priority);

        r.setPriority(
                normPri == null ? "média" : normPri
        );

        // Status.
        r.setStatusId(t.getStatusId());

        String statusName = null;

        if (t.getStatusId() != null) {
            statusName = statusTarefaRepository.findById(t.getStatusId())
                    .map(Status::getNome)
                    .orElse(null);
        }

        r.setStatusName(statusName);

        String normStatus =
                StatusUtils.normalizeTaskStatus(statusName);

        r.setStatus(
                normStatus == null ? "planejada" : normStatus
        );

        // Campos auxiliares.
        r.setDaysDelayed(
                t.getDaysDelayed() == null ? 0 : t.getDaysDelayed()
        );

        r.setPlannedEnd(t.getPlannedEnd());

        r.setEstimatedHours(
                t.getEstimatedHours() == null
                        ? 0
                        : t.getEstimatedHours()
        );

        r.setBlockedBy(t.getBlockedBy());

        // Tags.
        try {

            var ttags =
                    tarefaTagRepository.findByIdTarefaId(t.getId());

            var ids = ttags.stream()
                    .map(tt -> tt.getId().getTagId())
                    .collect(Collectors.toList());

            r.setTagIds(ids);

        } catch (Exception e) {

            r.setTagIds(Collections.emptyList());
        }

        r.setDescription(t.getDescricao());

        return r;
    }
}
