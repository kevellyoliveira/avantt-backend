package com.avantt_backend.repository;

import com.avantt_backend.entity.StatusTarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusTarefaRepository extends JpaRepository<StatusTarefa, Integer> {
}
