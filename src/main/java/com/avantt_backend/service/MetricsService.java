package com.avantt_backend.service;

import com.avantt_backend.dto.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MetricsService {

    @PersistenceContext
    private EntityManager em;

    public List<MetricStatusCountDTO> getStatusDistribution(Integer projetoId) {
        // inline aggregation in case views are not present in the database
        String sql = "SELECT s.nome AS status, COUNT(*) AS quantidade " +
                "FROM tarefa t JOIN status_tarefa s ON t.status_id = s.id " +
                "WHERE t.projeto_id = :pid GROUP BY s.nome";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<MetricStatusCountDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            MetricStatusCountDTO d = new MetricStatusCountDTO();
            d.setStatus(r[0] == null ? "" : r[0].toString());
            d.setQuantidade(r[1] == null ? 0 : ((Number) r[1]).intValue());
            out.add(d);
        }
        return out;
    }

    public MetricCompletionRateDTO getCompletionRate(Integer projetoId) {
        String sql = "SELECT " +
                "SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) AS concluidas, " +
                "COUNT(*) AS total, " +
                "ROUND(100.0 * SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) / NULLIF(COUNT(*),0),2) AS pct_conclusao " +
                "FROM tarefa t JOIN status_tarefa s ON t.status_id = s.id " +
                "WHERE t.projeto_id = :pid";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        if (rows.isEmpty()) return null;
        Object[] r = rows.get(0);
        MetricCompletionRateDTO d = new MetricCompletionRateDTO();
        d.setProjetoId(projetoId);
        d.setConcluidas(r[0] == null ? 0 : ((Number) r[0]).intValue());
        d.setTotal(r[1] == null ? 0 : ((Number) r[1]).intValue());
        d.setPctConclusao(r[2] == null ? 0.0 : ((Number) r[2]).doubleValue());
        return d;
    }

    public Integer getOverdueCount(Integer projetoId) {
        // use the database column names (data_fim) as present in the MySQL schema
        String sql = "SELECT COUNT(*) FROM tarefa t JOIN status_tarefa s ON t.status_id = s.id " +
                "WHERE t.projeto_id = :pid AND t.data_fim IS NOT NULL AND t.data_fim < CURDATE() AND s.nome <> 'Concluída'";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();
        if (rows.isEmpty()) return 0;
        Object r = rows.get(0);
        return r == null ? 0 : ((Number) r).intValue();
    }

    public List<MetricOverdueTaskDTO> getTopOverdue(Integer projetoId, int limit) {
        // select using the DB column `data_fim`
        String sql = "SELECT t.id AS tarefa_id, t.projeto_id, t.sprint_id, t.titulo, t.data_fim, s.nome AS status, DATEDIFF(CURDATE(), t.data_fim) AS dias_atraso " +
                "FROM tarefa t JOIN status_tarefa s ON t.status_id = s.id " +
                "WHERE t.projeto_id = :pid AND t.data_fim IS NOT NULL AND t.data_fim < CURDATE() AND s.nome <> 'Concluída' " +
                "ORDER BY dias_atraso DESC";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        q.setMaxResults(limit);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<MetricOverdueTaskDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            MetricOverdueTaskDTO d = new MetricOverdueTaskDTO();
            d.setTarefaId(r[0] == null ? null : ((Number) r[0]).intValue());
            d.setProjetoId(r[1] == null ? null : ((Number) r[1]).intValue());
            d.setSprintId(r[2] == null ? null : ((Number) r[2]).intValue());
            d.setTitulo(r[3] == null ? "" : r[3].toString());
            d.setDataFim(r[4] == null ? null : r[4].toString());
            d.setStatus(r[5] == null ? "" : r[5].toString());
            d.setDiasAtraso(r[6] == null ? 0 : ((Number) r[6]).intValue());
            out.add(d);
        }
        return out;
    }

    public List<MetricTasksByMemberDTO> getTasksByMember(Integer projetoId) {
        // use the DB column `horas_estimadas`
        String sql = "SELECT u.id AS usuario_id, u.nome AS usuario_nome, t.projeto_id, COUNT(*) AS tarefas, COALESCE(SUM(t.horas_estimadas),0) AS horas_estimadas " +
                "FROM tarefa_usuario tu JOIN usuario u ON tu.usuario_id = u.id JOIN tarefa t ON tu.tarefa_id = t.id " +
                "WHERE t.projeto_id = :pid GROUP BY u.id, u.nome, t.projeto_id ORDER BY tarefas DESC";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        @SuppressWarnings("unchecked")
        List<Object[]> rows = q.getResultList();
        List<MetricTasksByMemberDTO> out = new ArrayList<>();
        for (Object[] r : rows) {
            MetricTasksByMemberDTO d = new MetricTasksByMemberDTO();
            d.setUsuarioId(r[0] == null ? null : ((Number) r[0]).intValue());
            d.setUsuarioNome(r[1] == null ? "" : r[1].toString());
            d.setProjetoId(r[2] == null ? null : ((Number) r[2]).intValue());
            d.setTarefas(r[3] == null ? 0 : ((Number) r[3]).intValue());
            d.setHorasEstimadas(r[4] == null ? 0.0 : ((Number) r[4]).doubleValue());
            out.add(d);
        }
        return out;
    }

    public Double getHoursEstimated(Integer projetoId) {
        String sql = "SELECT horas_estimadas_total FROM vw_hours_estimated_by_project WHERE projeto_id = :pid";
        var q = em.createNativeQuery(sql);
        q.setParameter("pid", projetoId);
        @SuppressWarnings("unchecked")
        List<Object> rows = q.getResultList();
        if (rows.isEmpty()) return 0.0;
        Object r = rows.get(0);
        return r == null ? 0.0 : ((Number) r).doubleValue();
    }
}
