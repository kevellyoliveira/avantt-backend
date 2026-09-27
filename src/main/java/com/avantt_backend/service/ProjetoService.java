package com.avantt_backend.service;

import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.SprintsDTO;
import com.avantt_backend.dto.ProjectTasksDTO;
import com.avantt_backend.util.StatusUtils;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final SprintService sprintService;
    private final TarefaService tarefaService;
    private final SprintRepository sprintRepository;
    private final TarefaRepository tarefaRepository;
    private final com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ProjetoService(ProjetoRepository projetoRepository,
                          UsuarioRepository usuarioRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository,
                          SprintService sprintService,
                          TarefaService tarefaService,
                          SprintRepository sprintRepository,
                          TarefaRepository tarefaRepository,
                          com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
        this.sprintRepository = sprintRepository;
        this.tarefaRepository = tarefaRepository;
        this.statusTarefaRepository = statusTarefaRepository;
    }

    @Transactional
    public ProjetoResponseDTO create(ProjetoRequestDTO dto) {
        Projeto p = new Projeto();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setColor(dto.getColor());
        // status resolution: dto validation ensures statusId presence
        var stOpt = statusTarefaRepository.findById(dto.getStatusId());
        if (stOpt.isEmpty()) throw new com.avantt_backend.exception.ResourceNotFoundException("Status não encontrado: id=" + dto.getStatusId());
        p.setStatusId(dto.getStatusId());
        p.setStartDate(dto.getStartDate());
        p.setProgress(dto.getProgress());

        if (dto.getEndDate() != null &&
                dto.getEndDate().isBefore(dto.getStartDate().plusDays(15))) {
            throw new ApiException("A data de fim deve ser pelo menos 15 dias após a data de início");
        } else {
            p.setEndDate(dto.getEndDate());
        }

        Projeto saved = projetoRepository.save(p);

        // associate team (list of user IDs). Validate all ids exist; return error on invalid user
        if (dto.getTeam() != null) {
            for (Integer usuarioId : dto.getTeam()) {
                if (usuarioId == null) throw new ApiException("Usuário inválido na lista de team");
                if (!usuarioRepository.findById(usuarioId).isPresent()) {
                    throw new ApiException("Usuário não encontrado: id=" + usuarioId);
                }
                ProjetoUsuario pu = new ProjetoUsuario();
                pu.setId(new ProjetoUsuarioId(saved.getId(), usuarioId));
                pu.setPapel(null);
                projetoUsuarioRepository.save(pu);
            }
        }

        return toFrontend(saved);
    }

    public List<ProjetoResponseDTO> listAll() {
        List<Projeto> all = projetoRepository.findAll();
        return all.stream().map(this::toFrontend).toList();
    }

    private ProjetoResponseDTO toFrontend(Projeto p) {
        ProjetoResponseDTO f = new ProjetoResponseDTO();
        f.setId(p.getId() == null ? null : String.valueOf(p.getId()));
        f.setName(p.getName());
        f.setDescription(p.getDescription());
        f.setColor(p.getColor());
        // set status normalized string for frontend based on statusId
        if (p.getStatusId() != null) {
            var st = statusTarefaRepository.findById(p.getStatusId());
            if (st.isPresent()) f.setStatus(StatusUtils.normalizeProjectStatus(st.get().getNome()));
            else f.setStatus(null);
        } else {
            f.setStatus(null);
        }
        f.setStartDate(p.getStartDate());
        f.setEndDate(p.getEndDate());
        f.setProgress(p.getProgress() == null ? 0 : p.getProgress());
        try {
            // build sprints by querying SprintService
            String projetoIdStr = p.getId() == null ? null : String.valueOf(p.getId());
            var sprints = sprintService.listAll(projetoIdStr);
            SprintsDTO s = new SprintsDTO();
            s.setTotal(sprints.size());
            int done = 0, delayed = 0, active = 0;
            for (var sp : sprints) {
                if (sp.getProgress() >= 100) done++;
                else if (sp.getDaysDelayed() > 0) delayed++;
                else active++;
            }
            s.setDone(done);
            s.setDelayed(delayed);
            s.setActive(active);

            // build tasks by querying TarefaService
            var tarefas = tarefaService.listAll(projetoIdStr, null, null);
            ProjectTasksDTO t = new ProjectTasksDTO();
            t.setTotal(tarefas.size());
            int tDone = 0, tDelayed = 0, tBlocked = 0, tCancelled = 0;
            for (var ta : tarefas) {
                String status = ta.getStatus();
                if (status != null && (status.equalsIgnoreCase("concluido") || status.equalsIgnoreCase("concluído") || status.equalsIgnoreCase("done"))) tDone++;
                if (ta.getDaysDelayed() > 0) tDelayed++;
                if (ta.getBlockedBy() != null && !ta.getBlockedBy().isBlank()) tBlocked++;
                if (status != null && (status.equalsIgnoreCase("cancelado") || status.equalsIgnoreCase("cancelled"))) tCancelled++;
            }
            t.setDone(tDone);
            t.setDelayed(tDelayed);
            t.setBlocked(tBlocked);
            t.setCancelled(tCancelled);

            // team -> load from projeto_usuario
            List<String> teamNames = java.util.Collections.emptyList();
            var puList = projetoUsuarioRepository.findByIdProjetoId(p.getId());
            if (puList != null && !puList.isEmpty()) {
                var temp = new java.util.ArrayList<String>();
                for (var pu : puList) {
                    Integer uid = pu.getId().getUsuarioId();
                    usuarioRepository.findById(uid).ifPresent(u -> temp.add(u.getNome()));
                }
                teamNames = temp;
            }

            f.setSprints(s);
            f.setTasks(t);
            f.setTeam(teamNames);
            // set status id/name for the project based on stored status id
            f.setStatusId(p.getStatusId());
            if (p.getStatusId() != null) {
                statusTarefaRepository.findById(p.getStatusId()).ifPresent(st -> f.setStatusName(st.getNome()));
            }

        } catch (Exception e) {
            throw new ApiException("Erro ao desserializar campos JSON", e);
        }
        return f;
    }
}
