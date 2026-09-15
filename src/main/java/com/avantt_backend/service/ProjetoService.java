package com.avantt_backend.service;

import com.avantt_backend.dto.MilestoneDTO;
import com.avantt_backend.dto.ProjetoRequestDTO;
import com.avantt_backend.dto.ProjetoResponseDTO;
import com.avantt_backend.dto.SprintsDTO;
import com.avantt_backend.dto.ProjectTasksDTO;
import com.avantt_backend.util.StatusUtils;
import com.avantt_backend.entity.MarcoProjeto;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.entity.ProjetoUsuario;
import com.avantt_backend.entity.ProjetoUsuarioId;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProjetoUsuarioRepository projetoUsuarioRepository;
    private final MarcoProjetoRepository marcoProjetoRepository;
    private final SprintService sprintService;
    private final TarefaService tarefaService;
    private final SprintRepository sprintRepository;
    private final TarefaRepository tarefaRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ProjetoService(ProjetoRepository projetoRepository,
                          UsuarioRepository usuarioRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository,
                          MarcoProjetoRepository marcoProjetoRepository,
                          SprintService sprintService,
                          TarefaService tarefaService,
                          SprintRepository sprintRepository,
                          TarefaRepository tarefaRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.marcoProjetoRepository = marcoProjetoRepository;
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
        this.sprintRepository = sprintRepository;
        this.tarefaRepository = tarefaRepository;
    }

    @Transactional
    public ProjetoResponseDTO create(ProjetoRequestDTO dto) {
        Projeto p = new Projeto();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setColor(dto.getColor());
        p.setStatus(StatusUtils.normalizeProjectStatus(dto.getStatus()));
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setProgress(dto.getProgress());

        // risks: frontend sends list, DB expects a single VARCHAR nullable
        if (dto.getRisks() == null || dto.getRisks().isEmpty()) {
            p.setRisks(null);
        } else {
            // join into a single string
            p.setRisks(String.join("; ", dto.getRisks()));
        }

        Projeto saved = projetoRepository.save(p);

        // associate team
        if (dto.getTeam() != null) {
            for (String member : dto.getTeam()) {
                Integer usuarioId = null;
                try {
                    // try parse as id
                    usuarioId = Integer.valueOf(member);
                    if (!usuarioRepository.findById(usuarioId).isPresent()) {
                        throw new ApiException("Usuario com id " + usuarioId + " não encontrado");
                    }
                } catch (NumberFormatException nfe) {
                    // not an id, try find by name
                    var opt = usuarioRepository.findByNomeIgnoreCase(member);
                    if (opt.isPresent()) {
                        usuarioId = opt.get().getId();
                    } else {
                        throw new ApiException("Usuario com nome '" + member + "' não encontrado");
                    }
                }
                ProjetoUsuario pu = new ProjetoUsuario();
                pu.setId(new ProjetoUsuarioId(saved.getId(), usuarioId));
                pu.setPapel(null);
                projetoUsuarioRepository.save(pu);
            }
        }

        // create milestones
        if (dto.getMilestones() != null) {
            for (MilestoneDTO m : dto.getMilestones()) {
                MarcoProjeto mp = new MarcoProjeto();
                mp.setProjetoId(saved.getId());
                mp.setNome(m.getName());
                mp.setData(m.getDate());
                mp.setConcluido(m.isDone());
                marcoProjetoRepository.save(mp);
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
        f.setStatus(p.getStatus());
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

            // risks -> stored as single string; convert to list for frontend
            List<String> risks = java.util.Collections.emptyList();
            if (p.getRisks() != null && !p.getRisks().isBlank()) {
                risks = java.util.Arrays.stream(p.getRisks().split(";"))
                        .map(String::trim).filter(s0 -> !s0.isEmpty()).toList();
            }

            // milestones -> load from marco_projeto
            List<MilestoneDTO> milestones = java.util.Collections.emptyList();
            var mlist = marcoProjetoRepository.findByProjetoId(p.getId());
            if (mlist != null && !mlist.isEmpty()) {
                var temp = new java.util.ArrayList<MilestoneDTO>();
                for (var mp : mlist) {
                    MilestoneDTO md = new MilestoneDTO();
                    md.setName(mp.getNome());
                    md.setDate(mp.getData());
                    md.setDone(mp.getConcluido() == null ? false : mp.getConcluido());
                    temp.add(md);
                }
                milestones = temp;
            }

            f.setSprints(s);
            f.setTasks(t);
            f.setTeam(teamNames);
            f.setRisks(risks);
            f.setMilestones(milestones);
        } catch (Exception e) {
            throw new ApiException("Erro ao desserializar campos JSON", e);
        }
        return f;
    }
}
