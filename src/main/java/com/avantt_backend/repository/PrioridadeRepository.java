package com.avantt_backend.repository;

import com.avantt_backend.entity.Prioridade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrioridadeRepository extends JpaRepository<Prioridade, Integer> {
}
