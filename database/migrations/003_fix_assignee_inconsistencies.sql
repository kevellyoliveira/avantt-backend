-- Migration 003: backup and fix tasks whose assignee is not member of the sprint or project
-- 1) creates a small backup table with affected tarefa ids
-- 2) inserts inconsistent rows into backup
-- 3) clears atribuido_para on those tarefas (safe default)

-- Create backup table (id list) if not exists
CREATE TABLE IF NOT EXISTS tarefa_assignee_backup (
    tarefa_id INT PRIMARY KEY,
    projeto_id INT,
    sprint_id INT,
    atribuido_para INT,
    backup_ts DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert tarefas where atribuido_para is not member of the sprint
INSERT IGNORE INTO tarefa_assignee_backup (tarefa_id, projeto_id, sprint_id, atribuido_para)
SELECT t.id, t.projeto_id, t.sprint_id, t.atribuido_para
FROM tarefa t
LEFT JOIN sprint_usuario su ON su.sprint_id = t.sprint_id AND su.usuario_id = t.atribuido_para
WHERE t.atribuido_para IS NOT NULL
  AND (t.sprint_id IS NULL OR su.usuario_id IS NULL);

-- Insert tarefas where atribuido_para is not member of the project
INSERT IGNORE INTO tarefa_assignee_backup (tarefa_id, projeto_id, sprint_id, atribuido_para)
SELECT t.id, t.projeto_id, t.sprint_id, t.atribuido_para
FROM tarefa t
LEFT JOIN projeto_usuario pu ON pu.projeto_id = t.projeto_id AND pu.usuario_id = t.atribuido_para
WHERE t.atribuido_para IS NOT NULL
  AND (t.projeto_id IS NULL OR pu.usuario_id IS NULL);

-- At this point, tarefa_assignee_backup contains any tarefas that violate the desired invariants.
-- We will clear atribuido_para on those tarefas so they don't block FK creation.
UPDATE tarefa t
JOIN tarefa_assignee_backup b ON t.id = b.tarefa_id
SET t.atribuido_para = NULL;

-- Report rows that were fixed (helpful when running manually)
SELECT 'fixed' AS action, COUNT(*) AS affected FROM tarefa_assignee_backup;

-- Note: the backup table retains records of what was cleared. If you want to restore
-- assignments, consult tarefa_assignee_backup and re-create associations manually after
-- ensuring corresponding sprint/projeto membership exists.
