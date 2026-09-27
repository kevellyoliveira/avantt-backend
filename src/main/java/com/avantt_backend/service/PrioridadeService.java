package com.avantt_backend.service;

import com.avantt_backend.dto.PrioridadeDTO;
import com.avantt_backend.entity.Prioridade;
import com.avantt_backend.repository.PrioridadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrioridadeService {

    private final PrioridadeRepository repo;

    public PrioridadeService(PrioridadeRepository repo) { this.repo = repo; }

    public List<PrioridadeDTO> listAll() {
        return repo.findAll().stream().map(p -> new PrioridadeDTO(p.getId(), p.getNome())).collect(Collectors.toList());
    }
}
