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

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM tarefa t WHERE t.sprint_id = :sprintId", nativeQuery = true)
    int countBySprintId(@org.springframework.data.repository.query.Param("sprintId") Integer sprintId);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM tarefa t JOIN `status` s ON t.status_id = s.id WHERE t.sprint_id = :sprintId AND s.nome = :statusName", nativeQuery = true)
    int countBySprintIdAndStatusName(@org.springframework.data.repository.query.Param("sprintId") Integer sprintId, @org.springframework.data.repository.query.Param("statusName") String statusName);

    @org.springframework.data.jpa.repository.Query(value = "SELECT COUNT(*) FROM tarefa t WHERE t.sprint_id = :sprintId AND t.atribuido_para = :usuarioId", nativeQuery = true)
    int countBySprintIdAndAssignee(@org.springframework.data.repository.query.Param("sprintId") Integer sprintId, @org.springframework.data.repository.query.Param("usuarioId") Integer usuarioId);
}
