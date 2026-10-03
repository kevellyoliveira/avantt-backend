-- Metric views for dashboard
-- Run these statements in your MySQL database (sistema_gestao)
-- They create read-only views that aggregate common dashboard metrics.
-- Adjust names or filters (por organização, por projeto) as needed.

USE sistema_gestao;

-- 1) Distribution by status (for pizza chart)
CREATE OR REPLACE VIEW vw_status_distribution AS
SELECT
  t.projeto_id,
  s.nome AS status,
  COUNT(*) AS quantidade
FROM tarefa t
JOIN status_tarefa s ON t.status_id = s.id
GROUP BY t.projeto_id, s.nome;

-- 2) Completion rate per project
CREATE OR REPLACE VIEW vw_completion_rate AS
SELECT
  t.projeto_id,
  SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) AS concluidas,
  COUNT(*) AS total,
  ROUND(100.0 * SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) / NULLIF(COUNT(*),0),2) AS pct_conclusao
FROM tarefa t
JOIN status_tarefa s ON t.status_id = s.id
GROUP BY t.projeto_id;

-- 3) Count of overdue tasks per project
CREATE OR REPLACE VIEW vw_overdue_tasks AS
SELECT
  t.projeto_id,
  COUNT(*) AS tarefas_atrasadas
FROM tarefa t
JOIN status_tarefa s ON t.status_id = s.id
WHERE t.data_fim IS NOT NULL
  AND t.data_fim < CURDATE()
  AND s.nome <> 'Concluída'
GROUP BY t.projeto_id;

-- 4) Top overdue tasks (list) - useful for "mais atrasadas"
CREATE OR REPLACE VIEW vw_top_overdue_tasks AS
SELECT
  t.id AS tarefa_id,
  t.projeto_id,
  t.sprint_id,
  t.titulo,
  t.data_fim,
  s.nome AS status,
  DATEDIFF(CURDATE(), t.data_fim) AS dias_atraso
FROM tarefa t
JOIN status_tarefa s ON t.status_id = s.id
WHERE t.data_fim IS NOT NULL
  AND t.data_fim < CURDATE()
  AND s.nome <> 'Concluída';

-- 5) Tasks by member (count and estimated hours)
CREATE OR REPLACE VIEW vw_tasks_by_member AS
SELECT
  u.id AS usuario_id,
  u.nome AS usuario_nome,
  t.projeto_id,
  COUNT(*) AS tarefas,
  COALESCE(SUM(t.horas_estimadas), 0) AS horas_estimadas
FROM tarefa_usuario tu
JOIN usuario u ON tu.usuario_id = u.id
JOIN tarefa t ON tu.tarefa_id = t.id
GROUP BY u.id, u.nome, t.projeto_id;

-- 6) Estimated hours by project
CREATE OR REPLACE VIEW vw_hours_estimated_by_project AS
SELECT
  projeto_id,
  COALESCE(SUM(horas_estimadas), 0) AS horas_estimadas_total
FROM tarefa
GROUP BY projeto_id;

-- Index recommendations (run once; these speed up the aggregate queries)
-- CREATE INDEX idx_tarefa_status ON tarefa(status_id);
-- CREATE INDEX idx_tarefa_projeto ON tarefa(projeto_id);
-- CREATE INDEX idx_tarefa_sprint ON tarefa(sprint_id);
-- CREATE INDEX idx_tarefa_datafim ON tarefa(data_fim);
-- CREATE INDEX idx_tarefa_usuario_usuario ON tarefa_usuario(usuario_id);

-- Notes:
-- - MySQL doesn't have native materialized views. For high-traffic dashboards consider
--   creating summary tables updated by scheduled jobs or triggers and indexing them.
-- - To get results per project, filter WHERE projeto_id = :id on the views.
-- Example queries:
-- SELECT status, quantidade FROM vw_status_distribution WHERE projeto_id = 1;
-- SELECT * FROM vw_completion_rate WHERE projeto_id = 1;
-- SELECT * FROM vw_overdue_tasks WHERE projeto_id = 1;
-- SELECT * FROM vw_top_overdue_tasks WHERE projeto_id = 1 ORDER BY dias_atraso DESC LIMIT 10;
-- SELECT * FROM vw_tasks_by_member WHERE projeto_id = 1 ORDER BY tarefas DESC;
