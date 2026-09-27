-- Migration 004: add FK constraints to ensure tarefa.assigned user belongs to sprint and project
-- This migration adds unique indexes on sprint_usuario and projeto_usuario (if missing)
-- and adds composite foreign keys from tarefa(sprint_id, atribuido_para) -> sprint_usuario(sprint_id, usuario_id)
-- and from tarefa(projeto_id, atribuido_para) -> projeto_usuario(projeto_id, usuario_id).

-- Create unique index on sprint_usuario(sprint_id, usuario_id) if not exists
SET @idx_name = 'ux_sprint_usuario_sprint_usuario';
SET @exists = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'sprint_usuario' AND index_name = @idx_name);
SET @sql = IF(@exists = 0, CONCAT('CREATE UNIQUE INDEX ', @idx_name, ' ON sprint_usuario (sprint_id, usuario_id)'), 'SELECT "index exists"');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- Create unique index on projeto_usuario(projeto_id, usuario_id) if not exists
SET @idx_name2 = 'ux_projeto_usuario_projeto_usuario';
SET @exists2 = (SELECT COUNT(*) FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'projeto_usuario' AND index_name = @idx_name2);
SET @sql2 = IF(@exists2 = 0, CONCAT('CREATE UNIQUE INDEX ', @idx_name2, ' ON projeto_usuario (projeto_id, usuario_id)'), 'SELECT "index exists"');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;

-- Add FK from tarefa(sprint_id, atribuido_para) -> sprint_usuario(sprint_id, usuario_id)
SET @fk_name = 'fk_tarefa_sprint_usuario';
SET @fk_exists = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'tarefa' AND constraint_name = @fk_name);
SET @addfk = IF(@fk_exists = 0, CONCAT('ALTER TABLE tarefa ADD CONSTRAINT ', @fk_name, ' FOREIGN KEY (sprint_id, atribuido_para) REFERENCES sprint_usuario (sprint_id, usuario_id) ON UPDATE CASCADE ON DELETE NO ACTION'), 'SELECT "fk exists"');
PREPARE stmt3 FROM @addfk; EXECUTE stmt3; DEALLOCATE PREPARE stmt3;

-- Add FK from tarefa(projeto_id, atribuido_para) -> projeto_usuario(projeto_id, usuario_id)
SET @fk_name2 = 'fk_tarefa_projeto_usuario';
SET @fk_exists2 = (SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_schema = DATABASE() AND table_name = 'tarefa' AND constraint_name = @fk_name2);
SET @addfk2 = IF(@fk_exists2 = 0, CONCAT('ALTER TABLE tarefa ADD CONSTRAINT ', @fk_name2, ' FOREIGN KEY (projeto_id, atribuido_para) REFERENCES projeto_usuario (projeto_id, usuario_id) ON UPDATE CASCADE ON DELETE NO ACTION'), 'SELECT "fk exists"');
PREPARE stmt4 FROM @addfk2; EXECUTE stmt4; DEALLOCATE PREPARE stmt4;

-- Done. If any of the above statements fail, inspect the database state and
-- the backup table tarefa_assignee_backup created by migration 003.
