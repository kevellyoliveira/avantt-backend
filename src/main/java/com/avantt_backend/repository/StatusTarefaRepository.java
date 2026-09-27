package com.avantt_backend.repository;

import com.avantt_backend.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTarefaRepository extends JpaRepository<Status, Integer> {
    Optional<Status> findByNomeIgnoreCase(String nome);
}
