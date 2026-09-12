package com.avantt_backend.service;

import com.avantt_backend.dto.*;
import com.avantt_backend.entity.Projeto;
import com.avantt_backend.exception.ApiException;
import com.avantt_backend.repository.ProjetoRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class ProjetoService {

    private final ProjetoRepository projetoRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public ProjetoService(ProjetoRepository projetoRepository) {
        this.projetoRepository = projetoRepository;
    }

    @Transactional
    public ProjetoFrontendDTO create(ProjetoRequestDTO dto) {
        Projeto p = new Projeto();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setColor(dto.getColor());
        p.setStatus(dto.getStatus());
        p.setStartDate(dto.getStartDate());
        p.setEndDate(dto.getEndDate());
        p.setProgress(dto.getProgress());
        try {
            p.setSprints(MAPPER.writeValueAsString(dto.getSprints()));
            p.setTasks(MAPPER.writeValueAsString(dto.getTasks()));
            p.setTeam(MAPPER.writeValueAsString(dto.getTeam()));
            p.setRisks(MAPPER.writeValueAsString(dto.getRisks()));
            p.setMilestones(MAPPER.writeValueAsString(dto.getMilestones()));
        } catch (Exception e) {
            throw new ApiException("Erro ao serializar campos JSON", e);
        }

        Projeto saved = projetoRepository.save(p);
        return toFrontend(saved);
    }

    public List<ProjetoFrontendDTO> listAllForFrontend() {
        List<Projeto> all = projetoRepository.findAll();
        return all.stream().map(this::toFrontend).toList();
    }

    private ProjetoFrontendDTO toFrontend(Projeto p) {
        ProjetoFrontendDTO f = new ProjetoFrontendDTO();
        f.setId(p.getId() == null ? null : String.valueOf(p.getId()));
        f.setName(p.getName());
        f.setDescription(p.getDescription());
        f.setColor(p.getColor());
        f.setStatus(p.getStatus());
        f.setStartDate(p.getStartDate());
        f.setEndDate(p.getEndDate());
        f.setProgress(p.getProgress() == null ? 0 : p.getProgress());
        try {
            SprintsDTO s = p.getSprints() == null ? new SprintsDTO(0,0,0,0) : MAPPER.readValue(p.getSprints(), SprintsDTO.class);
            ProjectTasksDTO t = p.getTasks() == null ? new ProjectTasksDTO(0,0,0,0,0) : MAPPER.readValue(p.getTasks(), ProjectTasksDTO.class);
            List<String> team = p.getTeam() == null ? java.util.Collections.emptyList() : MAPPER.readValue(p.getTeam(), new TypeReference<List<String>>(){});
            List<String> risks = p.getRisks() == null ? java.util.Collections.emptyList() : MAPPER.readValue(p.getRisks(), new TypeReference<List<String>>(){});
            List<MilestoneDTO> milestones = p.getMilestones() == null ? java.util.Collections.emptyList() : MAPPER.readValue(p.getMilestones(), new TypeReference<List<MilestoneDTO>>(){});
            f.setSprints(s);
            f.setTasks(t);
            f.setTeam(team);
            f.setRisks(risks);
            f.setMilestones(milestones);
        } catch (Exception e) {
            throw new ApiException("Erro ao desserializar campos JSON", e);
        }
        return f;
    }
}
