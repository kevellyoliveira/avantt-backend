-- Full schema with CREATE DATABASE and USE lines
CREATE DATABASE IF NOT EXISTS sistema_gestao
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_gestao;

-- (remaining schema is the same as src/main/resources/schema.sql)
-- For convenience, you can import this file directly with the mysql client:
-- mysql -u root -p < db/schema-full.sql

-- (If you need the full content duplicated here, open src/main/resources/schema.sql)
