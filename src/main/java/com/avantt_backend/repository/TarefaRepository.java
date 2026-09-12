package com.avantt_backend.repository;

import com.avantt_backend.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {
    List<Tarefa> findByProject(String project);
    List<Tarefa> findByProjectAndSprint(String project, String sprint);
    List<Tarefa> findByStatus(String status);
}
