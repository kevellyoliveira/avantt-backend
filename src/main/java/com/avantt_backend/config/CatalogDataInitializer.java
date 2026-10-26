package com.avantt_backend.config;

import com.avantt_backend.entity.Status;
import com.avantt_backend.repository.StatusTarefaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CatalogDataInitializer implements CommandLineRunner {
    private final StatusTarefaRepository statusRepository;

    public CatalogDataInitializer(StatusTarefaRepository statusRepository) {
        this.statusRepository = statusRepository;
    }

    @Override
    public void run(String... args) {
        List.of("Planejada", "Em andamento", "Concluída", "Bloqueada")
                .forEach(name -> statusRepository.findByNomeIgnoreCase(name).orElseGet(() -> {
                    Status status = new Status();
                    status.setNome(name);
                    return statusRepository.save(status);
                }));
    }
}
