package com.avantt_backend.repository;

import com.avantt_backend.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
    // find sprints by projeto_id
    List<Sprint> findByProjetoId(Integer projetoId);
    java.util.Optional<com.avantt_backend.entity.Sprint> findByNome(String nome);
}
