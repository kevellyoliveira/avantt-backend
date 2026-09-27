-- Migration 005: backup and remove coluna tarefa_pai_id da tabela tarefa
-- Steps:
-- 1) if coluna exists, copy non-null values to backup table tarefa_pai_backup
-- 2) drop any foreign key referencing tarefa_pai_id
-- 3) drop the coluna tarefa_pai_id

-- Check if column exists
SET @col_exists = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tarefa' AND COLUMN_NAME = 'tarefa_pai_id');
SELECT @col_exists as col_exists;

-- If column doesn't exist, nothing to do
-- If it exists, create backup and drop FK/column
IF @col_exists > 0 THEN

  -- create backup table if not exists
  CREATE TABLE IF NOT EXISTS tarefa_pai_backup (
    tarefa_id INT PRIMARY KEY,
    tarefa_pai_id INT,
    backup_ts DATETIME DEFAULT CURRENT_TIMESTAMP
  );

  -- backup rows with non-null tarefa_pai_id
  INSERT IGNORE INTO tarefa_pai_backup (tarefa_id, tarefa_pai_id)
  SELECT id, tarefa_pai_id FROM tarefa WHERE tarefa_pai_id IS NOT NULL;

  -- find foreign key constraint name (if any) that references tarefa_pai_id
  SELECT CONSTRAINT_NAME INTO @fk_name
  FROM information_schema.KEY_COLUMN_USAGE
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'tarefa'
    AND COLUMN_NAME = 'tarefa_pai_id'
    AND REFERENCED_TABLE_NAME IS NOT NULL
  LIMIT 1;

  -- drop foreign key if found
  IF @fk_name IS NOT NULL THEN
    SET @dropfk = CONCAT('ALTER TABLE tarefa DROP FOREIGN KEY ', @fk_name);
    PREPARE stmt FROM @dropfk; EXECUTE stmt; DEALLOCATE PREPARE stmt;
  END IF;

  -- drop index on column if exists (some engines create one for FK)
  SET @idx_name = (SELECT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tarefa' AND COLUMN_NAME = 'tarefa_pai_id' LIMIT 1);
  IF @idx_name IS NOT NULL THEN
    SET @dropidx = CONCAT('ALTER TABLE tarefa DROP INDEX ', @idx_name);
    PREPARE stmt2 FROM @dropidx; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
  END IF;

  -- finally drop the column
  ALTER TABLE tarefa DROP COLUMN tarefa_pai_id;

END IF;

-- End migration 005
