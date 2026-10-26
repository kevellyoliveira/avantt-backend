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
    private final com.avantt_backend.repository.OrganizacaoRepository organizacaoRepository;
    private final com.avantt_backend.repository.ClienteRepository clienteRepository;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ProjetoService(ProjetoRepository projetoRepository,
                          UsuarioRepository usuarioRepository,
                          ProjetoUsuarioRepository projetoUsuarioRepository,
                          SprintService sprintService,
                          TarefaService tarefaService,
                          SprintRepository sprintRepository,
                          TarefaRepository tarefaRepository,
                          com.avantt_backend.repository.StatusTarefaRepository statusTarefaRepository,
                          com.avantt_backend.repository.OrganizacaoRepository organizacaoRepository,
                          com.avantt_backend.repository.ClienteRepository clienteRepository) {
        this.projetoRepository = projetoRepository;
        this.usuarioRepository = usuarioRepository;
        this.projetoUsuarioRepository = projetoUsuarioRepository;
        this.sprintService = sprintService;
        this.tarefaService = tarefaService;
        this.sprintRepository = sprintRepository;
        this.tarefaRepository = tarefaRepository;
        this.statusTarefaRepository = statusTarefaRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.clienteRepository = clienteRepository;
    }

    // Return current progress info for a project (recalculates from sprints)
    public com.avantt_backend.service.SprintService.ProjectProgressInfo getProgress(Integer projetoId) {
        return sprintService.recalculateAndPersistProjectProgress(projetoId);
    }

    @Transactional
    public ProjetoResponseDTO create(ProjetoRequestDTO dto) {
        Projeto p = new Projeto();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setColor(dto.getColor());
        // status resolution: dto validation ensures statusId presence
        var stOpt = statusTarefaRepository.findById(dto.getStatusId());
        if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
        p.setStatusId(dto.getStatusId());
        // cliente/organizacao (relation exists only via projeto)
        if (dto.getClienteId() != null) {
            if (!clienteRepository.existsById(dto.getClienteId()))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Cliente não encontrado: id=" + dto.getClienteId());
            p.setClienteId(dto.getClienteId());
        }
        if (dto.getOrganizacaoId() != null) {
            if (!organizacaoRepository.existsById(dto.getOrganizacaoId()))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Organização não encontrada: id=" + dto.getOrganizacaoId());
            p.setOrganizacaoId(dto.getOrganizacaoId());
        }
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
            if (usuarioId == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário inválido na lista de team");
            if (!usuarioRepository.findById(usuarioId).isPresent()) {
                    throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "Usuário não encontrado: id=" + usuarioId);
            }
                ProjetoUsuario pu = new ProjetoUsuario();
                pu.setId(new ProjetoUsuarioId(saved.getId(), usuarioId));
                pu.setPapel(null);
                projetoUsuarioRepository.save(pu);
            }
        }

        return toFrontend(saved);
    }

    @Transactional
    public ProjetoResponseDTO update(Integer projetoId, ProjetoRequestDTO dto) {
        var pOpt = projetoRepository.findById(projetoId);
        if (pOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Projeto não encontrado: id=" + projetoId);
        var p = pOpt.get();

        // validate status if provided
        if (dto.getStatusId() != null) {
            var stOpt = statusTarefaRepository.findById(dto.getStatusId());
            if (stOpt.isEmpty()) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Status não encontrado: id=" + dto.getStatusId());
            p.setStatusId(dto.getStatusId());
        }

        // cliente/organizacao updates
        if (dto.getClienteId() != null) {
            if (!clienteRepository.existsById(dto.getClienteId()))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Cliente não encontrado: id=" + dto.getClienteId());
            p.setClienteId(dto.getClienteId());
        }
        if (dto.getOrganizacaoId() != null) {
            if (!organizacaoRepository.existsById(dto.getOrganizacaoId()))
                throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "Organização não encontrada: id=" + dto.getOrganizacaoId());
            p.setOrganizacaoId(dto.getOrganizacaoId());
        }

        // update basic fields
        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        if (dto.getColor() != null) p.setColor(dto.getColor());

        // dates: must respect minimum 15 days rule
        java.time.LocalDate newStart = (dto.getStartDate() == null) ? p.getStartDate() : dto.getStartDate();
        java.time.LocalDate newEnd = (dto.getEndDate() == null) ? p.getEndDate() : dto.getEndDate();
        if (newStart != null && newEnd != null) {
            if (newEnd.isBefore(newStart.plusDays(15))) {
                throw new ApiException("A data de fim deve ser pelo menos 15 dias após a data de início");
            }
        }
        p.setStartDate(newStart);
        p.setEndDate(newEnd);

        // team additions
        if (dto.getAddTeam() != null && !dto.getAddTeam().isEmpty()) {
            for (Integer usuarioId : dto.getAddTeam()) {
                if (usuarioId == null) throw new ApiException("Usuário inválido na lista de team");
                if (!usuarioRepository.findById(usuarioId).isPresent()) throw new ApiException("Usuário não encontrado: id=" + usuarioId);

                var id = new ProjetoUsuarioId(p.getId(), usuarioId);
                if (!projetoUsuarioRepository.existsById(id)) {
                    ProjetoUsuario pu = new ProjetoUsuario();
                    pu.setId(id);
                    pu.setPapel(null);
                    projetoUsuarioRepository.save(pu);
                }
            }
        }

        // team removals
        if (dto.getRemoveTeam() != null && !dto.getRemoveTeam().isEmpty()) {
            for (Integer usuarioId : dto.getRemoveTeam()) {
                if (usuarioId == null) continue;
                var id = new ProjetoUsuarioId(p.getId(), usuarioId);
                if (!projetoUsuarioRepository.existsById(id)) continue; // nothing to remove

                // ensure user not assigned to any task in this project
                int assigned = tarefaRepository.countByProjetoIdAndAssignee(p.getId(), usuarioId);
                if (assigned > 0) throw new ApiException("Não é possível remover o usuário: ele está associado a tarefas deste projeto: id=" + usuarioId);

                projetoUsuarioRepository.deleteById(id);
            }
        }

        Projeto saved = projetoRepository.save(p);
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
            // include relacionamentos
            f.setClienteId(p.getClienteId());
            f.setOrganizacaoId(p.getOrganizacaoId());
            if (p.getOrganizacaoId() != null) {
                organizacaoRepository.findById(p.getOrganizacaoId()).ifPresent(org -> f.setOrganizacaoName(org.getNomeFantasia()));
            }
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
