package com.avantt_backend.service;

import com.avantt_backend.dto.SprintRequestDTO;
import com.avantt_backend.dto.SprintResponseDTO;
import com.avantt_backend.entity.Sprint;
import com.avantt_backend.repository.ProjetoRepository;
import com.avantt_backend.repository.SprintRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;
    private final ProjetoRepository projetoRepository;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public SprintService(SprintRepository sprintRepository, ProjetoRepository projetoRepository) {
        this.sprintRepository = sprintRepository;
        this.projetoRepository = projetoRepository;
    }

    @Transactional
    public SprintResponseDTO create(SprintRequestDTO dto) {
        Sprint s = new Sprint();
        s.setNome(dto.getName());
        s.setProject(dto.getProject());
        s.setStartDate(dto.getStartDate());
        s.setEndDate(dto.getEndDate());
        s.setDaysDelayed(dto.getDaysDelayed() == null ? 0 : dto.getDaysDelayed());
        s.setProgress(dto.getProgress() == null ? 0 : dto.getProgress());
        s.setTotalTasks(dto.getTotalTasks() == null ? 0 : dto.getTotalTasks());
        s.setDoneTasks(dto.getDoneTasks() == null ? 0 : dto.getDoneTasks());
        s.setBlockedTasks(dto.getBlockedTasks() == null ? 0 : dto.getBlockedTasks());
        try {
            s.setTeam(dto.getTeam() == null ? "[]" : MAPPER.writeValueAsString(dto.getTeam()));
            s.setGoal(dto.getGoal());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Sprint saved = sprintRepository.save(s);
        SprintResponseDTO r = new SprintResponseDTO();
        r.setId(saved.getId() == null ? null : String.valueOf(saved.getId()));
        r.setName(saved.getNome());
        r.setProject(saved.getProject());
        r.setStartDate(saved.getStartDate());
        r.setEndDate(saved.getEndDate());
        r.setDaysDelayed(saved.getDaysDelayed() == null ? 0 : saved.getDaysDelayed());
        r.setProgress(saved.getProgress() == null ? 0 : saved.getProgress());
        r.setTotalTasks(saved.getTotalTasks() == null ? 0 : saved.getTotalTasks());
        r.setDoneTasks(saved.getDoneTasks() == null ? 0 : saved.getDoneTasks());
        r.setBlockedTasks(saved.getBlockedTasks() == null ? 0 : saved.getBlockedTasks());
        try {
            if (saved.getTeam() == null) r.setTeam(java.util.Collections.emptyList());
            else r.setTeam(MAPPER.readValue(saved.getTeam(), new TypeReference<java.util.List<String>>(){}));
        } catch (Exception e) { r.setTeam(java.util.Collections.emptyList()); }
        r.setGoal(saved.getGoal());
        return r;
    }

    public List<SprintResponseDTO> listAll(String projetoId) {
        List<Sprint> all;
        if (projetoId == null) {
            all = sprintRepository.findAll();
        } else {
            // try to resolve projetoId to name if numeric
            AtomicReference<String> projectFilter = new AtomicReference<>(projetoId);
            try {
                Integer pid = Integer.valueOf(projetoId.replaceAll("[^0-9]", ""));
                projetoRepository.findById(pid).ifPresent(p -> projectFilter.set(p.getName()));
            } catch (Exception ignored) {}
            all = sprintRepository.findByProject(projectFilter.get());
        }

        return all.stream().map(s -> {
            SprintResponseDTO r = new SprintResponseDTO();
            r.setId(s.getId() == null ? null : String.valueOf(s.getId()));
            r.setName(s.getNome());
            r.setProject(s.getProject());
            r.setStartDate(s.getStartDate());
            r.setEndDate(s.getEndDate());
            r.setDaysDelayed(s.getDaysDelayed() == null ? 0 : s.getDaysDelayed());
            r.setProgress(s.getProgress() == null ? 0 : s.getProgress());
            r.setTotalTasks(s.getTotalTasks() == null ? 0 : s.getTotalTasks());
            r.setDoneTasks(s.getDoneTasks() == null ? 0 : s.getDoneTasks());
            r.setBlockedTasks(s.getBlockedTasks() == null ? 0 : s.getBlockedTasks());
            try {
                if (s.getTeam() == null) r.setTeam(java.util.Collections.emptyList());
                else r.setTeam(MAPPER.readValue(s.getTeam(), new TypeReference<List<String>>() {}));
            } catch (Exception e) { r.setTeam(java.util.Collections.emptyList()); }
            r.setGoal(s.getGoal());
            return r;
        }).collect(Collectors.toList());
    }
}
