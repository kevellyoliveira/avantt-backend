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
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.entity.Usuario;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import com.avantt_backend.entity.SprintUsuarioId;
import com.avantt_backend.entity.SprintUsuario;

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

    @Transactional
    public SprintResponseDTO update(Integer sprintId, SprintRequestDTO dto) {
        var spOpt = sprintRepository.findById(sprintId);
        if (spOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Sprint não encontrada: id=" + sprintId);
        var s = spOpt.get();

        // cannot change project
        if (dto.getProjectId() != null && !dto.getProjectId().equals(s.getProjetoId())) {
            throw new ApiException("Não é permitido alterar o projeto da sprint");
        }

        // update name
        if (dto.getName() != null) s.setNome(dto.getName());

        // resolve project for date validations
        var projOpt = projetoRepository.findById(s.getProjetoId());
        if (projOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Projeto não encontrado: id=" + s.getProjetoId());
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
            if (stOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Status não encontrado: id=" + dto.getStatusId());
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
        int total = tarefaRepository.countBySprintId(saved.getId());
        int done = tarefaRepository.countBySprintIdAndStatusName(saved.getId(), "Done");
        int blocked = 0;
        int daysDelayed = 0;
        if (saved.getDataFim() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(saved.getDataFim(), today);
            }
        }
        int progress = (total == 0) ? 0 : (int) ((done * 100L) / total);
        r.setDaysDelayed(daysDelayed);
        r.setProgress(progress);
        r.setTotalTasks(total);
        r.setDoneTasks(done);
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
    public SprintResponseDTO create(SprintRequestDTO dto) {
        // Find project by id (avoid ambiguous names) - DTO validation ensures projectId presence
        var projetoOpt = projetoRepository.findById(dto.getProjectId());
        if (projetoOpt.isEmpty())
            throw new com.avantt_backend.exception.ResourceNotFoundException("Projeto não encontrado: id=" + dto.getProjectId());
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
        if (stOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Status não encontrado: id=" + dto.getStatusId());
        s.setStatusId(dto.getStatusId());

        Sprint saved = sprintRepository.save(s);

        // Build response DTO: include non-persisted fields from the incoming DTO or defaults
        SprintResponseDTO r = new SprintResponseDTO();
        r.setId(saved.getId());
        r.setName(saved.getNome());
        r.setProject(projeto.getName());
        r.setStartDate(saved.getDataInicio());
        r.setEndDate(saved.getDataFim());
        // compute totals from DB where possible
        int total = tarefaRepository.countBySprintId(saved.getId());
        int done = tarefaRepository.countBySprintIdAndStatusName(saved.getId(), "Done");
        int blocked = 0; // no explicit blocked flag in schema -> default 0
        int daysDelayed = 0;
        if (saved.getDataFim() != null) {
            LocalDate today = LocalDate.now();
            if (today.isAfter(saved.getDataFim())) {
                daysDelayed = (int) ChronoUnit.DAYS.between(saved.getDataFim(), today);
            }
        }
        int progress = (total == 0) ? 0 : (int) ((done * 100L) / total);
        r.setDaysDelayed(dto.getDaysDelayed() == null ? daysDelayed : dto.getDaysDelayed());
        r.setProgress(dto.getProgress() == null ? progress : dto.getProgress());
        r.setTotalTasks(dto.getTotalTasks() == null ? total : dto.getTotalTasks());
        r.setDoneTasks(dto.getDoneTasks() == null ? done : dto.getDoneTasks());
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
            // compute totals
            int total = (s.getId() == null) ? 0 : tarefaRepository.countBySprintId(s.getId());
            int done = (s.getId() == null) ? 0 : tarefaRepository.countBySprintIdAndStatusName(s.getId(), "Done");
            int blocked = 0;
            int daysDelayed = 0;
            if (s.getDataFim() != null) {
                LocalDate today = LocalDate.now();
                if (today.isAfter(s.getDataFim())) {
                    daysDelayed = (int) ChronoUnit.DAYS.between(s.getDataFim(), today);
                }
            }
            int progress = (total == 0) ? 0 : (int) ((done * 100L) / total);
            r.setDaysDelayed(daysDelayed);
            r.setProgress(progress);
            r.setTotalTasks(total);
            r.setDoneTasks(done);
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
