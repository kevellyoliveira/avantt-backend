package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.entity.Sprint;
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

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private final TarefaRepository tarefaRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final UsuarioRepository usuarioRepository;

    public SprintService(SprintRepository sprintRepository,
                         ProjetoRepository projetoRepository,
                         TarefaRepository tarefaRepository,
                         ProjetoUsuarioRepository projetoUsuarioRepository,
                         UsuarioRepository usuarioRepository) {
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
        this.tarefaRepository = tarefaRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public SprintResponseDTO create(SprintRequestDTO dto) {
        // Find project by name
        var projetoOpt = projetoRepository.findByName(dto.getProject());
        if (projetoOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Projeto não encontrado: " + dto.getProject());
        var projeto = projetoOpt.get();

        Sprint s = new Sprint();
        // persist only the columns that exist on sprint table
        s.setProjetoId(projeto.getId());
        s.setNome(dto.getName());
        s.setDataInicio(dto.getStartDate());
        s.setDataFim(dto.getEndDate());
        // default status for a newly created sprint
        s.setStatus("Planejada");

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
        // team: prefer provided list, otherwise load from projeto_usuario
        if (dto.getTeam() != null && !dto.getTeam().isEmpty()) {
            r.setTeam(dto.getTeam());
        } else {
            r.setTeam(loadTeamForProject(projeto.getId()));
        }
        r.setGoal(dto.getGoal());
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
            } catch (Exception ignored) {}
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
            // team from projeto_usuario
            if (s.getProjetoId() != null) r.setTeam(loadTeamForProject(s.getProjetoId()));
            else r.setTeam(java.util.Collections.emptyList());
            r.setGoal(null);
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
