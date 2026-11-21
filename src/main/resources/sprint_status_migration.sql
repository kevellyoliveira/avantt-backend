-- Execute once on existing databases; new installations get this column from schema.sql.
SET @sprint_status_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sprint'
      AND column_name = 'status'
);

SET @sprint_status_ddl = IF(
    @sprint_status_exists = 0,
    'ALTER TABLE sprint ADD COLUMN status ENUM(''Planejada'', ''Em andamento'', ''Finalizada'') NOT NULL DEFAULT ''Planejada''',
    'SELECT 1'
);

PREPARE sprint_status_statement FROM @sprint_status_ddl;
EXECUTE sprint_status_statement;
DEALLOCATE PREPARE sprint_status_statement;

UPDATE sprint SET status = 'Planejada' WHERE status IS NULL;

ALTER TABLE sprint
    MODIFY COLUMN status ENUM('Planejada', 'Em andamento', 'Finalizada')
    NOT NULL DEFAULT 'Planejada';
