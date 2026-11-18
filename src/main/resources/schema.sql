-- Schema for sistema_gestao
-- NOTE: This file intentionally omits CREATE DATABASE / USE lines.
-- Run the full SQL (included in repo root as db/schema-full.sql) if you need to create the database itself.

-- ============================================================
-- BANCO DE DADOS - SISTEMA DE GERENCIAMENTO DE PROJETOS E TAREFAS
-- ============================================================

-- ============================================================
-- 1. ORGANIZAÇÃO
-- ============================================================

CREATE TABLE IF NOT EXISTS organizacao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia VARCHAR(150) NOT NULL,
    razao_social VARCHAR(200),
    cnpj VARCHAR(18) UNIQUE,
    contato VARCHAR(150),
    email VARCHAR(150),
    telefone VARCHAR(20),
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. PERFIL
-- ============================================================

CREATE TABLE IF NOT EXISTS perfil (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);


-- ============================================================
-- 3. USUÁRIO
-- ============================================================

CREATE TABLE IF NOT EXISTS usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,
    perfil_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    cargo VARCHAR(100),

    papel VARCHAR(50),

    senha VARCHAR(255) NULL,
    auth_token VARCHAR(255) NULL,
    auth_token_expiry DATETIME NULL,

    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL,

    is_ativo BOOLEAN DEFAULT TRUE,
    data_desativacao DATETIME NULL,

    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES organizacao(id),

    CONSTRAINT fk_usuario_perfil
        FOREIGN KEY (perfil_id)
        REFERENCES perfil(id)
);

CREATE INDEX IF NOT EXISTS idx_usuario_auth_token
    ON usuario(auth_token);

CREATE INDEX IF NOT EXISTS idx_usuario_reset_token
    ON usuario(reset_token);


-- ============================================================
-- 4. CLIENTE
-- ============================================================

CREATE TABLE IF NOT EXISTS cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(20),
    email VARCHAR(150),
    telefone VARCHAR(20),

    data_cadastro DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cliente_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES organizacao(id)
);


-- ============================================================
-- 5. PROJETO
-- ============================================================

CREATE TABLE IF NOT EXISTS projeto (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,
    cliente_id INT NULL,

    nome VARCHAR(150) NOT NULL,
    descricao TEXT,

    -- status stored in status table (status.id)
    status_id INT NULL,

    data_inicio DATE,
    data_fim DATE,

    horas_estimadas DECIMAL(10,2) DEFAULT 0.00,

    progresso INT DEFAULT 0,

    cor VARCHAR(20),

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_projeto_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES organizacao(id),

    CONSTRAINT fk_projeto_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
);


-- ============================================================
-- 6. PROJETO_USUARIO
-- Relacionamento N:N entre projeto e usuário
-- ============================================================

CREATE TABLE IF NOT EXISTS projeto_usuario (
    projeto_id INT NOT NULL,
    usuario_id INT NOT NULL,

    papel ENUM(
        'Admin',
        'Colaborador'
    ) DEFAULT 'Colaborador',

    data_entrada DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (projeto_id, usuario_id),

    CONSTRAINT fk_projeto_usuario_projeto
        FOREIGN KEY (projeto_id)
        REFERENCES projeto(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_projeto_usuario_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 7. SPRINT
-- ============================================================

CREATE TABLE IF NOT EXISTS sprint (
    id INT AUTO_INCREMENT PRIMARY KEY,

    projeto_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    objetivo TEXT,

    status_id INT NULL,
    progresso INT DEFAULT 0,
    data_inicio DATE,
    data_fim DATE,

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_sprint_projeto
        FOREIGN KEY (projeto_id)
        REFERENCES projeto(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 8. SPRINT_USUARIO
-- Relacionamento N:N entre sprint e usuário
-- ============================================================

CREATE TABLE IF NOT EXISTS sprint_usuario (
    sprint_id INT NOT NULL,
    usuario_id INT NOT NULL,

    data_entrada DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (sprint_id, usuario_id),

    CONSTRAINT fk_sprint_usuario_sprint
        FOREIGN KEY (sprint_id)
        REFERENCES sprint(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_sprint_usuario_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 9. STATUS DA TAREFA
-- ============================================================

CREATE TABLE IF NOT EXISTS status_tarefa (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);

INSERT IGNORE INTO status_tarefa (id, nome, descricao) VALUES
(1, 'Planejada', 'Tarefa planejada, mas ainda não iniciada'),
(2, 'Em andamento', 'Tarefa atualmente em execução'),
(3, 'Concluída', 'Tarefa finalizada'),
(4, 'Bloqueada', 'Tarefa impedida de continuar');


-- ============================================================
-- 10. PRIORIDADE
-- ============================================================

CREATE TABLE IF NOT EXISTS prioridade (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);

INSERT IGNORE INTO prioridade (id, nome, descricao) VALUES
(1, 'Baixa', 'Tarefa de baixa prioridade'),
(2, 'Média', 'Tarefa de prioridade média'),
(3, 'Alta', 'Tarefa de alta prioridade');


-- ============================================================
-- 11. TAREFA
-- ============================================================

CREATE TABLE IF NOT EXISTS tarefa (
    id INT AUTO_INCREMENT PRIMARY KEY,

    projeto_id INT NOT NULL,
    sprint_id INT NULL,

    status_id INT NOT NULL,
    prioridade_id INT NOT NULL,

    tarefa_pai_id INT NULL,

    titulo VARCHAR(200),
    criado_por INT NULL,
    atribuido_para INT NULL,
    -- nome é esperado pela aplicação (JPA mapeia coluna 'nome')
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,

    story_points INT NULL,

    horas_estimadas DECIMAL(10,2) DEFAULT 0.00,

    is_subtarefa BOOLEAN DEFAULT FALSE,

    data_inicio DATE NULL,
    data_fim DATE NULL,

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_tarefa_projeto
        FOREIGN KEY (projeto_id)
        REFERENCES projeto(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tarefa_sprint
        FOREIGN KEY (sprint_id)
        REFERENCES sprint(id)
        ON DELETE SET NULL,

    CONSTRAINT fk_tarefa_status
        FOREIGN KEY (status_id)
        REFERENCES status_tarefa(id),

    CONSTRAINT fk_tarefa_prioridade
        FOREIGN KEY (prioridade_id)
        REFERENCES prioridade(id),

    CONSTRAINT fk_tarefa_criado_por
        FOREIGN KEY (criado_por)
        REFERENCES usuario(id),

    CONSTRAINT fk_tarefa_atribuido_para
        FOREIGN KEY (atribuido_para)
        REFERENCES usuario(id),

    CONSTRAINT fk_tarefa_pai
        FOREIGN KEY (tarefa_pai_id)
        REFERENCES tarefa(id)
        ON DELETE SET NULL
);


-- Cria tabela `status` usada pela entidade Status (algumas queries usam status_tarefa)
CREATE TABLE IF NOT EXISTS `status` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

-- Insere mesmos status na tabela `status` caso ainda não existam
INSERT IGNORE INTO `status` (id, nome) VALUES
  (1, 'Planejada'),
  (2, 'Em andamento'),
  (3, 'Concluída'),
  (4, 'Bloqueada');


-- ============================================================
-- 12. TAREFA_USUARIO
-- Relacionamento N:N entre tarefa e usuário
-- ============================================================

CREATE TABLE IF NOT EXISTS tarefa_usuario (
    tarefa_id INT NOT NULL,
    usuario_id INT NOT NULL,

    papel ENUM(
        'Responsável',
        'Colaborador'
    ) DEFAULT 'Colaborador',

    data_atribuicao DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (tarefa_id, usuario_id),

    CONSTRAINT fk_tarefa_usuario_tarefa
        FOREIGN KEY (tarefa_id)
        REFERENCES tarefa(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tarefa_usuario_usuario
        FOREIGN KEY (usuario_id)
        REFERENCES usuario(id)
        ON DELETE CASCADE
);


-- ============================================================
-- 13. TAG
-- ============================================================

CREATE TABLE IF NOT EXISTS tag (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255),

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 14. TAREFA_TAG
-- Relacionamento N:N entre tarefa e tag
-- ============================================================

CREATE TABLE IF NOT EXISTS tarefa_tag (
    tarefa_id INT NOT NULL,
    tag_id INT NOT NULL,

    PRIMARY KEY (tarefa_id, tag_id),

    CONSTRAINT fk_tarefa_tag_tarefa
        FOREIGN KEY (tarefa_id)
        REFERENCES tarefa(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_tarefa_tag_tag
        FOREIGN KEY (tag_id)
        REFERENCES tag(id)
        ON DELETE CASCADE
);


-- Indexes
CREATE INDEX IF NOT EXISTS idx_tarefa_status ON tarefa(status_id);
CREATE INDEX IF NOT EXISTS idx_tarefa_projeto ON tarefa(projeto_id);
CREATE INDEX IF NOT EXISTS idx_tarefa_sprint ON tarefa(sprint_id);
CREATE INDEX IF NOT EXISTS idx_tarefa_datafim ON tarefa(data_fim);
CREATE INDEX IF NOT EXISTS idx_tarefa_usuario_usuario ON tarefa_usuario(usuario_id);

-- Note: projeto.status_id and sprint.status_id foreign keys require the `status` table
ALTER TABLE IF EXISTS projeto ADD CONSTRAINT fk_projeto_status FOREIGN KEY (status_id) REFERENCES `status`(id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE IF EXISTS sprint ADD CONSTRAINT fk_sprint_status FOREIGN KEY (status_id) REFERENCES `status`(id) ON UPDATE CASCADE ON DELETE SET NULL;

-- End of schema
