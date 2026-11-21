-- ============================================================
-- BANCO DE DADOS - SISTEMA DE GERENCIAMENTO DE PROJETOS E TAREFAS
-- ============================================================

CREATE DATABASE IF NOT EXISTS sistema_gestao
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sistema_gestao;


-- ============================================================
-- 1. ORGANIZAÇÃO
-- ============================================================

CREATE TABLE organizacao (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome_fantasia VARCHAR(150) NOT NULL,
    razao_social VARCHAR(200),
    cnpj VARCHAR(18) UNIQUE,
    contato VARCHAR(150),
    email VARCHAR(150),
    telefone VARCHAR(20),
    is_ativo BOOLEAN DEFAULT TRUE,
    data_desativacao DATETIME NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 2. PERFIL
-- ============================================================

CREATE TABLE perfil (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);


-- ============================================================
-- 3. USUÁRIO
-- ============================================================

CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,
    perfil_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    cpf VARCHAR(14) UNIQUE,
    telefone VARCHAR(20),
    cargo VARCHAR(100),

    -- Mantido conforme solicitado
    papel VARCHAR(50),

    -- Autenticação
    senha VARCHAR(255) NULL,
    auth_token VARCHAR(255) NULL,
    auth_token_expiry DATETIME NULL,

    -- Recuperação de senha
    reset_token VARCHAR(255) NULL,
    reset_token_expiry DATETIME NULL,

    -- Convite e primeiro acesso
    convite_token VARCHAR(64) NULL,
    convite_token_expiry DATETIME NULL,
    primeiro_acesso_validado BOOLEAN NOT NULL DEFAULT FALSE,

    -- Controle do usuário
    is_ativo BOOLEAN DEFAULT TRUE,
    data_desativacao DATETIME NULL,

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,
    data_atualizacao DATETIME DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_usuario_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES organizacao(id),

    CONSTRAINT fk_usuario_perfil
        FOREIGN KEY (perfil_id)
        REFERENCES perfil(id)
);

CREATE INDEX idx_usuario_auth_token
    ON usuario(auth_token);

CREATE INDEX idx_usuario_reset_token
    ON usuario(reset_token);


-- ============================================================
-- 4. CLIENTE
-- ============================================================

CREATE TABLE cliente (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    documento VARCHAR(20),
    email VARCHAR(150),
    telefone VARCHAR(20),

    is_ativo BOOLEAN DEFAULT TRUE,
    data_desativacao DATETIME NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_cliente_organizacao
        FOREIGN KEY (organizacao_id)
        REFERENCES organizacao(id)
);


-- ============================================================
-- 5. PROJETO
-- ============================================================

CREATE TABLE projeto (
    id INT AUTO_INCREMENT PRIMARY KEY,

    organizacao_id INT NOT NULL,
    cliente_id INT NULL,

    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    cor VARCHAR(20) NULL,
    progresso INT NOT NULL DEFAULT 0,

    status ENUM(
        'Planejada',
        'Em andamento',
        'Concluída',
        'Bloqueada'
    ) DEFAULT 'Planejada',

    data_inicio DATE,
    data_fim DATE,

    horas_estimadas DECIMAL(10,2) DEFAULT 0.00,

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

CREATE TABLE projeto_usuario (
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

CREATE TABLE sprint (
    id INT AUTO_INCREMENT PRIMARY KEY,

    projeto_id INT NOT NULL,

    nome VARCHAR(150) NOT NULL,
    objetivo TEXT,
    progresso INT NOT NULL DEFAULT 0,

    status ENUM(
        'Planejada',
        'Em andamento',
        'Finalizada'
    ) DEFAULT 'Planejada',

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

CREATE TABLE sprint_usuario (
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

CREATE TABLE status_tarefa (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);


INSERT INTO status_tarefa (id, nome, descricao) VALUES
(1, 'Planejada', 'Tarefa planejada, mas ainda não iniciada'),
(2, 'Em andamento', 'Tarefa atualmente em execução'),
(3, 'Concluída', 'Tarefa finalizada'),
(4, 'Bloqueada', 'Tarefa impedida de continuar');


-- ============================================================
-- 10. PRIORIDADE
-- ============================================================

CREATE TABLE prioridade (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255)
);


INSERT INTO prioridade (id, nome, descricao) VALUES
(1, 'Baixa', 'Tarefa de baixa prioridade'),
(2, 'Média', 'Tarefa de prioridade média'),
(3, 'Alta', 'Tarefa de alta prioridade');


-- ============================================================
-- 11. TAREFA
-- ============================================================

CREATE TABLE tarefa (
    id INT AUTO_INCREMENT PRIMARY KEY,

    projeto_id INT NOT NULL,
    sprint_id INT NULL,

    status_id INT NOT NULL,
    prioridade_id INT NOT NULL,

    tarefa_pai_id INT NULL,

    titulo VARCHAR(200) NOT NULL,
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

    CONSTRAINT fk_tarefa_pai
        FOREIGN KEY (tarefa_pai_id)
        REFERENCES tarefa(id)
        ON DELETE SET NULL
);


-- ============================================================
-- 12. TAREFA_USUARIO
-- Relacionamento N:N entre tarefa e usuário
-- ============================================================

CREATE TABLE tarefa_usuario (
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

CREATE TABLE tag (
    id INT AUTO_INCREMENT PRIMARY KEY,

    nome VARCHAR(50) NOT NULL UNIQUE,
    descricao VARCHAR(255),

    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP
);


-- ============================================================
-- 14. TAREFA_TAG
-- Relacionamento N:N entre tarefa e tag
-- ============================================================

CREATE TABLE tarefa_tag (
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


-- ============================================================
-- DADOS INICIAIS
-- ============================================================


-- ============================================================
-- ORGANIZAÇÕES
-- ============================================================

INSERT INTO organizacao
(nome_fantasia, razao_social, cnpj, contato, email, telefone)
VALUES
(
    'Avantt Tecnologia',
    'Avantt Tecnologia LTDA',
    '11.111.111/0001-11',
    'Bruno Milici',
    'contato@avantt.com',
    '(11) 99999-1111'
),
(
    'Tech Solutions',
    'Tech Solutions LTDA',
    '22.222.222/0001-22',
    'Carlos Silva',
    'contato@techsolutions.com',
    '(11) 99999-2222'
),
(
    'Dev Corp',
    'Dev Corp LTDA',
    '33.333.333/0001-33',
    'Ana Souza',
    'contato@devcorp.com',
    '(11) 99999-3333'
);


-- ============================================================
-- PERFIS
-- ============================================================

INSERT INTO perfil
(nome, descricao)
VALUES
('Administrador', 'Administrador da organização'),
('Gerente', 'Gerente de projetos'),
('Desenvolvedor', 'Membro da equipe de desenvolvimento'),
('Colaborador', 'Colaborador da organização');


-- ============================================================
-- USUÁRIOS
-- ============================================================

INSERT INTO usuario
(
    organizacao_id,
    perfil_id,
    nome,
    email,
    cpf,
    telefone,
    cargo,
    papel
)
VALUES
(
    1,
    1,
    'Bruno Milici',
    'bruno@avantt.com',
    '111.111.111-11',
    '(11) 99999-1111',
    'Administrador',
    'Administrador'
),
(
    1,
    2,
    'Carlos Oliveira',
    'carlos@avantt.com',
    '222.222.222-22',
    '(11) 99999-2222',
    'Gerente de Projetos',
    'Gerente'
),
(
    1,
    3,
    'Ana Souza',
    'ana@avantt.com',
    '333.333.333-33',
    '(11) 99999-3333',
    'Desenvolvedora',
    'Desenvolvedor'
),
(
    1,
    3,
    'João Santos',
    'joao@avantt.com',
    '444.444.444-44',
    '(11) 99999-4444',
    'Desenvolvedor',
    'Desenvolvedor'
),
(
    2,
    1,
    'Marcos Lima',
    'marcos@techsolutions.com',
    '555.555.555-55',
    '(11) 99999-5555',
    'Administrador',
    'Administrador'
),
(
    2,
    3,
    'Juliana Alves',
    'juliana@techsolutions.com',
    '666.666.666-66',
    '(11) 99999-6666',
    'Desenvolvedora',
    'Desenvolvedor'
),
(
    3,
    2,
    'Ricardo Mendes',
    'ricardo@devcorp.com',
    '777.777.777-77',
    '(11) 99999-7777',
    'Gerente de Projetos',
    'Gerente'
),
(
    3,
    4,
    'Fernanda Costa',
    'fernanda@devcorp.com',
    '888.888.888-88',
    '(11) 99999-8888',
    'Analista',
    'Colaborador'
);


-- ============================================================
-- CLIENTES
-- ============================================================

INSERT INTO cliente
(
    organizacao_id,
    nome,
    documento,
    email,
    telefone
)
VALUES
(
    1,
    'Empresa Alpha',
    '12.345.678/0001-01',
    'contato@alpha.com',
    '(11) 98888-1111'
),
(
    1,
    'Empresa Beta',
    '23.456.789/0001-02',
    'contato@beta.com',
    '(11) 98888-2222'
),
(
    2,
    'Empresa Gamma',
    '34.567.890/0001-03',
    'contato@gamma.com',
    '(11) 98888-3333'
),
(
    3,
    'Empresa Delta',
    '45.678.901/0001-04',
    'contato@delta.com',
    '(11) 98888-4444'
);


-- ============================================================
-- PROJETOS
-- ============================================================

INSERT INTO projeto
(
    organizacao_id,
    cliente_id,
    nome,
    descricao,
    status,
    data_inicio,
    data_fim,
    horas_estimadas
)
VALUES
(
    1,
    1,
    'Sistema de Gestão',
    'Desenvolvimento de sistema para gerenciamento de projetos e tarefas.',
    'Em andamento',
    '2026-01-10',
    '2026-12-20',
    1200.00
),
(
    1,
    2,
    'Dashboard Gerencial',
    'Desenvolvimento de dashboard para acompanhamento de indicadores.',
    'Planejada',
    '2026-04-01',
    '2026-08-30',
    600.00
),
(
    2,
    3,
    'Aplicativo Mobile',
    'Desenvolvimento de aplicativo mobile.',
    'Em andamento',
    '2026-02-01',
    '2026-10-30',
    900.00
),
(
    3,
    4,
    'Portal Corporativo',
    'Desenvolvimento do novo portal corporativo.',
    'Planejada',
    '2026-05-01',
    '2026-11-30',
    750.00
);


-- ============================================================
-- PROJETO_USUARIO
-- ============================================================

INSERT INTO projeto_usuario
(
    projeto_id,
    usuario_id,
    papel
)
VALUES
(1, 1, 'Admin'),
(1, 2, 'Admin'),
(1, 3, 'Colaborador'),
(1, 4, 'Colaborador'),

(2, 1, 'Admin'),
(2, 3, 'Colaborador'),

(3, 5, 'Admin'),
(3, 6, 'Colaborador'),

(4, 7, 'Admin'),
(4, 8, 'Colaborador');


-- ============================================================
-- SPRINTS
-- ============================================================

INSERT INTO sprint
(
    projeto_id,
    nome,
    objetivo,
    status,
    data_inicio,
    data_fim
)
VALUES
(
    1,
    'Sprint 01',
    'Configuração inicial do sistema.',
    'Finalizada',
    '2026-01-10',
    '2026-01-24'
),
(
    1,
    'Sprint 02',
    'Desenvolvimento do módulo de usuários.',
    'Finalizada',
    '2026-01-25',
    '2026-02-08'
),
(
    1,
    'Sprint 03',
    'Desenvolvimento do módulo de projetos.',
    'Em andamento',
    '2026-02-09',
    '2026-02-23'
),
(
    1,
    'Sprint 04',
    'Desenvolvimento do módulo de tarefas.',
    'Planejada',
    '2026-02-24',
    '2026-03-10'
),
(
    2,
    'Sprint Dashboard 01',
    'Levantamento dos indicadores.',
    'Planejada',
    '2026-04-01',
    '2026-04-15'
),
(
    2,
    'Sprint Dashboard 02',
    'Desenvolvimento dos dashboards.',
    'Planejada',
    '2026-04-16',
    '2026-04-30'
),
(
    3,
    'Sprint Mobile 01',
    'Estrutura inicial do aplicativo.',
    'Finalizada',
    '2026-02-01',
    '2026-02-15'
),
(
    3,
    'Sprint Mobile 02',
    'Desenvolvimento das telas.',
    'Em andamento',
    '2026-02-16',
    '2026-03-02'
),
(
    4,
    'Sprint Portal 01',
    'Levantamento de requisitos.',
    'Planejada',
    '2026-05-01',
    '2026-05-15'
),
(
    4,
    'Sprint Portal 02',
    'Desenvolvimento do portal.',
    'Planejada',
    '2026-05-16',
    '2026-05-30'
);


-- ============================================================
-- SPRINT_USUARIO
-- ============================================================

INSERT INTO sprint_usuario
(
    sprint_id,
    usuario_id
)
VALUES
(1, 1),
(1, 3),
(1, 4),

(2, 2),
(2, 3),
(2, 4),

(3, 2),
(3, 3),
(3, 4),

(4, 2),
(4, 3),
(4, 4),

(5, 1),
(5, 3),

(6, 1),
(6, 3),

(7, 5),
(7, 6),

(8, 5),
(8, 6),

(9, 7),
(9, 8),

(10, 7),
(10, 8);


-- ============================================================
-- TAGS
-- ============================================================

INSERT INTO tag
(nome, descricao)
VALUES
('Backend', 'Atividades relacionadas ao backend'),
('Frontend', 'Atividades relacionadas ao frontend'),
('Banco de Dados', 'Atividades relacionadas ao banco de dados'),
('Bug', 'Correção de problemas'),
('Melhoria', 'Melhoria ou otimização'),
('Documentação', 'Atividades de documentação'),
('Urgente', 'Atividade de alta urgência'),
('API', 'Atividades relacionadas a APIs'),
('Segurança', 'Atividades relacionadas à segurança'),
('Testes', 'Atividades de testes');


-- ============================================================
-- TAREFAS
-- ============================================================

INSERT INTO tarefa
(
    projeto_id,
    sprint_id,
    status_id,
    prioridade_id,
    tarefa_pai_id,
    titulo,
    descricao,
    story_points,
    horas_estimadas,
    is_subtarefa,
    data_inicio,
    data_fim
)
VALUES

-- Projeto 1
(
    1,
    1,
    3,
    3,
    NULL,
    'Configurar banco de dados',
    'Criar a estrutura inicial do banco de dados do sistema.',
    5,
    10.00,
    FALSE,
    '2026-01-10',
    '2026-01-15'
),

(
    1,
    1,
    3,
    2,
    NULL,
    'Configurar ambiente backend',
    'Configurar o ambiente inicial da aplicação backend.',
    3,
    8.00,
    FALSE,
    '2026-01-11',
    '2026-01-16'
),

(
    1,
    2,
    3,
    2,
    NULL,
    'Criar cadastro de usuários',
    'Implementar cadastro e gerenciamento de usuários.',
    8,
    20.00,
    FALSE,
    '2026-01-25',
    '2026-02-05'
),

(
    1,
    2,
    2,
    3,
    NULL,
    'Implementar autenticação',
    'Implementar login e autenticação dos usuários.',
    8,
    24.00,
    FALSE,
    '2026-01-28',
    NULL
),

(
    1,
    3,
    2,
    2,
    NULL,
    'Criar módulo de projetos',
    'Desenvolver funcionalidades para gerenciamento de projetos.',
    8,
    24.00,
    FALSE,
    '2026-02-09',
    NULL
),

(
    1,
    3,
    1,
    2,
    5,
    'Criar CRUD de projetos',
    'Implementar operações de criação, consulta, atualização e exclusão.',
    5,
    16.00,
    TRUE,
    '2026-02-10',
    NULL
),

(
    1,
    3,
    1,
    2,
    5,
    'Criar tela de projetos',
    'Desenvolver a interface de gerenciamento de projetos.',
    5,
    16.00,
    TRUE,
    '2026-02-11',
    NULL
),

(
    1,
    4,
    1,
    3,
    NULL,
    'Criar módulo de tarefas',
    'Implementar o gerenciamento de tarefas.',
    13,
    40.00,
    FALSE,
    '2026-02-24',
    NULL
),

(
    1,
    4,
    4,
    3,
    8,
    'Implementar associação de usuários',
    'Criar associação entre tarefas e usuários.',
    5,
    12.00,
    TRUE,
    NULL,
    NULL
),

(
    1,
    4,
    1,
    2,
    8,
    'Implementar sistema de tags',
    'Criar sistema de classificação das tarefas por tags.',
    3,
    8.00,
    TRUE,
    NULL,
    NULL
),

-- Projeto 2
(
    2,
    5,
    1,
    2,
    NULL,
    'Definir indicadores',
    'Definir os indicadores que serão apresentados no dashboard.',
    5,
    12.00,
    FALSE,
    '2026-04-01',
    NULL
),

(
    2,
    6,
    1,
    2,
    NULL,
    'Desenvolver dashboard',
    'Desenvolver os painéis gerenciais.',
    8,
    30.00,
    FALSE,
    NULL,
    NULL
),

-- Projeto 3
(
    3,
    7,
    3,
    2,
    NULL,
    'Criar estrutura mobile',
    'Criar a estrutura inicial do aplicativo mobile.',
    5,
    15.00,
    FALSE,
    '2026-02-01',
    '2026-02-12'
),

(
    3,
    8,
    2,
    3,
    NULL,
    'Desenvolver tela inicial',
    'Implementar a tela inicial do aplicativo.',
    5,
    16.00,
    FALSE,
    '2026-02-16',
    NULL
),

-- Projeto 4
(
    4,
    9,
    1,
    2,
    NULL,
    'Levantamento de requisitos',
    'Realizar levantamento dos requisitos do portal.',
    5,
    12.00,
    FALSE,
    '2026-05-01',
    NULL
);


-- ============================================================
-- TAREFA_USUARIO
-- ============================================================

INSERT INTO tarefa_usuario
(
    tarefa_id,
    usuario_id,
    papel
)
VALUES
(1, 3, 'Responsável'),
(1, 4, 'Colaborador'),

(2, 4, 'Responsável'),

(3, 3, 'Responsável'),
(3, 4, 'Colaborador'),

(4, 3, 'Responsável'),

(5, 2, 'Responsável'),
(5, 3, 'Colaborador'),

(6, 3, 'Responsável'),

(7, 4, 'Responsável'),

(8, 2, 'Responsável'),
(8, 3, 'Colaborador'),
(8, 4, 'Colaborador'),

(9, 4, 'Responsável'),

(10, 3, 'Responsável'),

(11, 3, 'Responsável'),

(12, 3, 'Responsável'),

(13, 6, 'Responsável'),

(14, 6, 'Responsável'),

(15, 8, 'Responsável');

CREATE TABLE projeto_marco (
    id INT NOT NULL AUTO_INCREMENT,
    projeto_id INT NOT NULL,
    nome VARCHAR(150) NOT NULL,
    data DATE NOT NULL,
    concluido TINYINT(1) NOT NULL DEFAULT 0,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    INDEX idx_projeto_marco_projeto (projeto_id),

    CONSTRAINT fk_projeto_marco_projeto
        FOREIGN KEY (projeto_id)
        REFERENCES projeto(id)
        ON DELETE CASCADE
);

DESCRIBE projeto_marco;

SHOW CREATE TABLE projeto_marco;




CREATE TABLE projeto_risco (
    id INT NOT NULL AUTO_INCREMENT,
    projeto_id INT NOT NULL,
    descricao VARCHAR(500) NOT NULL,
    data_criacao DATETIME DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    INDEX idx_projeto_risco_projeto (projeto_id),

    CONSTRAINT fk_projeto_risco_projeto
        FOREIGN KEY (projeto_id)
        REFERENCES projeto(id)
        ON DELETE CASCADE
);

DESCRIBE projeto_risco;
SHOW CREATE TABLE projeto_risco;
-- ============================================================
-- TAREFA_TAG
-- ============================================================

INSERT INTO tarefa_tag
(
    tarefa_id,
    tag_id
)
VALUES
(1, 3),
(1, 6),

(2, 1),

(3, 1),
(3, 8),

(4, 1),
(4, 9),

(5, 1),

(6, 1),
(6, 8),

(7, 2),

(8, 1),

(9, 1),

(10, 2),

(11, 6),

(12, 2),

(13, 2),

(14, 2),

(15, 6);

-- ============================================================
-- MÉTRICAS
-- ============================================================

-- Distribuição pos status (gráfico de pizza)
CREATE OR REPLACE VIEW vw_status_distribution AS                                                                                                                               
     SELECT                                                                                                                                                                         
       t.projeto_id,                                                                                                                                                                
       s.nome AS status,                                                                                                                                                            
       COUNT(*) AS quantidade                                                                                                                                                       
     FROM tarefa t                                                                                                                                                                  
     JOIN status_tarefa s ON t.status_id = s.id 
     GROUP BY t.projeto_id, s.nome;     
     
select * from vw_status_distribution;

-- Quantidade de tarfeas por projeto

 CREATE OR REPLACE VIEW vw_completion_rate AS                     
     SELECT                                                                                                                                                                         
       t.projeto_id,                                                                                                                                                                
       SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) AS concluidas,                                                                                                         
       COUNT(*) AS total,                                                                                                                                                           
       ROUND(100.0 * SUM(CASE WHEN s.nome = 'Concluída' THEN 1 ELSE 0 END) / NULLIF(COUNT(*),0),2) AS pct_conclusao                                                                 
     FROM tarefa t                                                                                                                                                                  
     JOIN status_tarefa s ON t.status_id = s.id                                                                                                                                     
     GROUP BY t.projeto_id;  
     
 select * from vw_completion_rate;    
 
 -- Número de tarefas atrasadas
     CREATE OR REPLACE VIEW vw_overdue_tasks AS                                                                                                                                          SELECT                                                                                                                                                                         
       t.projeto_id,                                                                                                                                                                
       COUNT(*) AS tarefas_atrasadas                                                                                                                                                
     FROM tarefa t                                                                                                                                                                  
     JOIN status_tarefa s ON t.status_id = s.id                                                                                                                                     
     WHERE t.data_fim IS NOT NULL                                                                                                                                                   
       AND t.data_fim < CURDATE()                                                                                                                                                   
       AND s.nome <> 'Concluída'                                                                                                                                                    
     GROUP BY t.projeto_id;
     
select * from vw_overdue_tasks;     

-- lista das tarefas mais atrasadas
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
       
select * from vw_top_overdue_tasks;       

-- Tarefa por pessoa 
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
     
select * from vw_tasks_by_member;     


-- estimativas de horas por projeto
CREATE OR REPLACE VIEW vw_hours_estimated_by_project AS                                                                                                                        
     SELECT                                                                                                                                                                         
       projeto_id,                                                                                                                                                                  
       COALESCE(SUM(horas_estimadas), 0) AS horas_estimadas_total                                                                                                                   
     FROM tarefa                                                                                                                                                                         
     GROUP BY projeto_id;   
     
select * from vw_hours_estimated_by_project;

SHOW TABLES LIKE '%status%';  

DESCRIBE status_tarefa;

DESCRIBE projeto;

DESCRIBE cliente;

DESCRIBE tarefa;

DESCRIBE usuario;
DESCRIBE tarefa_usuario;
select * from tarefa;

SHOW TABLES LIKE '%tarefa%';

SHOW TABLES LIKE '%status%';

SELECT
    id,
    nome,
    email,
    organizacao_id,
    perfil_id,
    is_ativo
FROM usuario;
DESCRIBE status_tarefa;

SELECT * FROM status_tarefa LIMIT 5;

select * from projeto;

SELECT COLUMN_NAME, DATA_TYPE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_NAME = 'tarefa'
ORDER BY ORDINAL_POSITION;



SELECT DATABASE();
DESCRIBE usuario;
SHOW COLUMNS FROM tarefa;

ALTER TABLE tarefa
ADD COLUMN atribuido_para INT NULL;

CREATE INDEX idx_tarefa_atribuido_para
ON tarefa (atribuido_para);

DESCRIBE tarefa;

SELECT
    id,
    titulo,
    atribuido_para
FROM tarefa
ORDER BY id DESC;

SELECT
    t.id,
    t.titulo,
    t.atribuido_para,
    u.nome AS nome_responsavel
FROM tarefa t
LEFT JOIN usuario u
    ON u.id = t.atribuido_para
ORDER BY t.id DESC;

SELECT
    id,
    titulo,
    projeto_id,
    sprint_id,
    status_id
FROM tarefa
WHERE sprint_id IS NULL;

SELECT
    id,
    nome
FROM status_tarefa
ORDER BY id;
SELECT
    id,
    titulo,
    projeto_id,
    sprint_id,
    status_id
FROM tarefa
ORDER BY id DESC
LIMIT 20;
SELECT
    t.id,
    t.titulo,
    t.atribuido_para,
    u.id AS usuario_id,
    u.nome AS nome_responsavel
FROM tarefa t
LEFT JOIN usuario u
    ON u.id = t.atribuido_para
ORDER BY t.id DESC;

SELECT *
FROM projeto_marco
ORDER BY id DESC;

SELECT *
FROM usuario
ORDER BY id DESC
LIMIT 30;
     
     
 SELECT
    id,
    nome,
    email,
    cpf,
    telefone,
    cargo,
    papel,
    is_ativo,
    data_criacao
FROM usuario
ORDER BY id DESC;
 
     SELECT *
FROM organizacao
ORDER BY id DESC;

SELECT TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_SCHEMA = DATABASE()
  AND (
    LOWER(TABLE_NAME) LIKE '%marco%'
    OR LOWER(TABLE_NAME) LIKE '%milestone%'
    OR LOWER(TABLE_NAME) LIKE '%risco%'
    OR LOWER(TABLE_NAME) LIKE '%risk%'
  );
  
  SHOW TABLES;
  
  DESCRIBE sprint;

DESCRIBE projeto;

SELECT
    u.id AS usuario_id,
    u.nome,
    u.organizacao_id,
    o.id AS organizacao_encontrada
FROM usuario u
LEFT JOIN organizacao o
    ON o.id = u.organizacao_id
WHERE u.id = 10;
                                                                                                                                                                             
CREATE INDEX idx_tarefa_status ON tarefa(status_id);                                                                                                                         
CREATE INDEX idx_tarefa_projeto ON tarefa(projeto_id);                                                                                                                       
CREATE INDEX idx_tarefa_sprint ON tarefa(sprint_id);                                                                                                                        
CREATE INDEX idx_tarefa_datafim ON tarefa(data_fim);                                                                                                                         
CREATE INDEX idx_tarefa_usuario_usuario ON tarefa_usuario(usuario_id);          
                     
                     
SELECT COUNT(*)
FROM tarefa t
INNER JOIN tarefa_usuario tu
    ON tu.tarefa_id = t.id
WHERE t.projeto_id = ?
  AND tu.usuario_id = ?;
  
  SELECT
    id,
    titulo,
    projeto_id,
    sprint_id,
    atribuido_para
FROM tarefa
ORDER BY id DESC;
SELECT
    ID,
    TITULO,
    ATRIBUIDO_PARA
FROM TAREFA
WHERE ATRIBUIDO_PARA = 16;

SELECT
    ATRIBUIDO_PARA,
    COUNT(*) AS QUANTIDADE_TAREFAS
FROM TAREFA
WHERE ATRIBUIDO_PARA IS NOT NULL
GROUP BY ATRIBUIDO_PARA
ORDER BY ATRIBUIDO_PARA;

-- ============================================================
-- COMPATIBILIDADE COM BASES CRIADAS ANTES DESTE SCRIPT
-- ============================================================

DROP PROCEDURE IF EXISTS ajustar_schema_compatibilidade;
DELIMITER $$
CREATE PROCEDURE ajustar_schema_compatibilidade()
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'organizacao' AND COLUMN_NAME = 'is_ativo'
    ) THEN
        ALTER TABLE organizacao ADD COLUMN is_ativo BOOLEAN DEFAULT TRUE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'organizacao' AND COLUMN_NAME = 'data_desativacao'
    ) THEN
        ALTER TABLE organizacao ADD COLUMN data_desativacao DATETIME NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente' AND COLUMN_NAME = 'is_ativo'
    ) THEN
        ALTER TABLE cliente ADD COLUMN is_ativo BOOLEAN DEFAULT TRUE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'cliente' AND COLUMN_NAME = 'data_desativacao'
    ) THEN
        ALTER TABLE cliente ADD COLUMN data_desativacao DATETIME NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'projeto' AND COLUMN_NAME = 'cor'
    ) THEN
        ALTER TABLE projeto ADD COLUMN cor VARCHAR(20) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'projeto' AND COLUMN_NAME = 'progresso'
    ) THEN
        ALTER TABLE projeto ADD COLUMN progresso INT NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'sprint' AND COLUMN_NAME = 'progresso'
    ) THEN
        ALTER TABLE sprint ADD COLUMN progresso INT NOT NULL DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'convite_token'
    ) THEN
        ALTER TABLE usuario ADD COLUMN convite_token VARCHAR(64) NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'convite_token_expiry'
    ) THEN
        ALTER TABLE usuario ADD COLUMN convite_token_expiry DATETIME NULL;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND COLUMN_NAME = 'primeiro_acesso_validado'
    ) THEN
        ALTER TABLE usuario ADD COLUMN primeiro_acesso_validado BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'usuario' AND INDEX_NAME = 'idx_usuario_convite_token'
    ) THEN
        CREATE INDEX idx_usuario_convite_token ON usuario(convite_token);
    END IF;
END$$
DELIMITER ;

CALL ajustar_schema_compatibilidade();
DROP PROCEDURE ajustar_schema_compatibilidade;

UPDATE usuario
SET primeiro_acesso_validado = TRUE
WHERE id > 0
  AND senha IS NOT NULL
  AND TRIM(senha) <> '';

SELECT
    ID,
    NOME,
    EMAIL
FROM USUARIO
WHERE ID = 16;

SELECT *
FROM USUARIO
WHERE ID = 16;


SELECT
    PU.PROJETO_ID,
    PU.USUARIO_ID,
    P.NOME,
    P.ORGANIZACAO_ID
FROM PROJETO_USUARIO PU
INNER JOIN PROJETO P
    ON P.ID = PU.PROJETO_ID
WHERE PU.PROJETO_ID = 9
  AND PU.USUARIO_ID = 16;

SELECT
    ID,
    NOME,
    ORGANIZACAO_ID
FROM PROJETO
ORDER BY ID;
  
  
  select * from usuario;
  
DELETE FROM usuario
WHERE ID = 17;


-- ============================================================
-- FIM DO SCRIPT
-- ============================================================