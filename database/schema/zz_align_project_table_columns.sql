SET @projeto_criado_por_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'projeto'
      AND column_name = 'criado_por_id'
);

SET @projeto_criado_por_migration = IF(
    @projeto_criado_por_column_exists = 0,
    'ALTER TABLE projeto ADD COLUMN criado_por_id INT NULL',
    'SELECT 1'
);

PREPARE add_projeto_criado_por_column
    FROM @projeto_criado_por_migration;
EXECUTE add_projeto_criado_por_column;
DEALLOCATE PREPARE add_projeto_criado_por_column;

SET @projeto_aviso_prazo_column_exists = (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'projeto'
      AND column_name = 'aviso_prazo_enviado_para'
);

SET @projeto_aviso_prazo_migration = IF(
    @projeto_aviso_prazo_column_exists = 0,
    'ALTER TABLE projeto ADD COLUMN aviso_prazo_enviado_para DATE NULL',
    'SELECT 1'
);

PREPARE add_projeto_aviso_prazo_column
    FROM @projeto_aviso_prazo_migration;
EXECUTE add_projeto_aviso_prazo_column;
DEALLOCATE PREPARE add_projeto_aviso_prazo_column;
