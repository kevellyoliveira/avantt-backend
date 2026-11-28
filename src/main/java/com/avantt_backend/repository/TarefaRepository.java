
package com.avantt_backend.repository;

import com.avantt_backend.entity.Tarefa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TarefaRepository extends JpaRepository<Tarefa, Integer> {

    List<Tarefa> findByProject(String project);

    List<Tarefa> findByProjectAndSprint(String project, String sprint);

    List<Tarefa> findByStatus(String status);

    List<Tarefa> findBySprintId(Integer sprintId);

    // Busca todas as tarefas do projeto, inclusive as tarefas do backlog.
    List<Tarefa> findByProjetoId(Integer projetoId);
    List<Tarefa> findByProjetoIdIn(List<Integer> projetoIds);

    @Query(
            value = "SELECT COUNT(*) FROM tarefa t " +
                    "WHERE t.sprint_id = :sprintId",
            nativeQuery = true
    )
    int countBySprintId(@Param("sprintId") Integer sprintId);

    @Query(
            value = "SELECT COUNT(*) FROM tarefa t " +
                    "JOIN status_tarefa s ON t.status_id = s.id " +
                    "WHERE t.sprint_id = :sprintId " +
                    "AND s.nome = :statusName",
            nativeQuery = true
    )
    int countBySprintIdAndStatusName(
            @Param("sprintId") Integer sprintId,
            @Param("statusName") String statusName
    );

    @Query(
            value = "SELECT COUNT(*) FROM tarefa t " +
                    "INNER JOIN tarefa_usuario tu ON tu.tarefa_id = t.id " +
                    "WHERE t.sprint_id = :sprintId " +
                    "AND tu.usuario_id = :usuarioId",
            nativeQuery = true
    )
    int countBySprintIdAndAssignee(
            @Param("sprintId") Integer sprintId,
            @Param("usuarioId") Integer usuarioId
    );

    @Query(
            value = "SELECT COUNT(*) FROM tarefa t " +
                    "INNER JOIN tarefa_usuario tu ON tu.tarefa_id = t.id " +
                    "WHERE t.projeto_id = :projetoId " +
                    "AND tu.usuario_id = :usuarioId",
            nativeQuery = true
    )
    int countByProjetoIdAndAssignee(
            @Param("projetoId") Integer projetoId,
            @Param("usuarioId") Integer usuarioId
    );
}
