-- Migration: Create tag and tarefa_tag tables
-- Run this against your existing database. This migration creates the global tag table
-- and the tarefa_tag join table for many-to-many association between tarefa and tag.

CREATE TABLE IF NOT EXISTS tag (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS tarefa_tag (
    tarefa_id INT NOT NULL,
    tag_id INT NOT NULL,
    PRIMARY KEY (tarefa_id, tag_id),
    FOREIGN KEY (tarefa_id) REFERENCES tarefa(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag(id) ON DELETE CASCADE
);

-- Note: preset tag values were not inserted here. If you want a set of
-- pre-defined tags (e.g. BUG, FEATURE, DOCS), tell me which values and I
-- will add INSERT ... WHERE NOT EXISTS statements to seed them.
