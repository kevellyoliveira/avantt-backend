CREATE DATABASE IF NOT EXISTS task_manager;
USE task_manager;


-- =========================================================
-- ORGANIZACAO
-- =========================================================

CREATE TABLE IF NOT EXISTS organizacao (
id INT AUTO_INCREMENT PRIMARY KEY,
nome_fantasia VARCHAR(255) NOT NULL,
cnpj VARCHAR(20) UNIQUE NOT NULL,
email_contato VARCHAR(255)
);


-- =========================================================
-- PERFIL
-- =========================================================

CREATE TABLE IF NOT EXISTS perfil (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(50) NOT NULL
);

INSERT IGNORE INTO perfil (nome) VALUES
('Admin'),
('Colaborador');


-- =========================================================
-- STATUS (shared table for projeto/sprint/tarefa)
-- =========================================================
CREATE TABLE IF NOT EXISTS `status` (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(50) NOT NULL UNIQUE
);

INSERT IGNORE INTO `status` (nome) VALUES
('Planejada'),
('Em andamento'),
('Concluída'),
('Bloqueada');


-- =========================================================
-- USUARIO
-- =========================================================

CREATE TABLE IF NOT EXISTS usuario (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(255) NOT NULL,
email VARCHAR(255) UNIQUE NOT NULL,
data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
is_ativo BOOLEAN DEFAULT TRUE,
perfil_id INT,
cargo VARCHAR(100),
data_desativacao TIMESTAMP NULL,
organizacao_id INT,

-- Autenticação
senha VARCHAR(255) NULL,
auth_token VARCHAR(255) NULL,
auth_token_expiry DATETIME NULL,

-- Recuperação de senha
reset_token VARCHAR(255) NULL,
reset_token_expiry DATETIME NULL,

-- Índices para busca dos tokens
INDEX idx_usuario_auth_token (auth_token),
INDEX idx_usuario_reset_token (reset_token),

-- Chaves estrangeiras
FOREIGN KEY (organizacao_id)
REFERENCES organizacao(id),

FOREIGN KEY (perfil_id)
REFERENCES perfil(id)
);

-- =========================================================
-- CLIENTE
-- =========================================================

CREATE TABLE IF NOT EXISTS cliente (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(255) NOT NULL,
email_contato VARCHAR(255),
telefone VARCHAR(20),
data_cadastro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
is_ativo BOOLEAN DEFAULT TRUE,
data_desativacao TIMESTAMP NULL
);


-- =========================================================
-- PROJETO
-- =========================================================

CREATE TABLE IF NOT EXISTS projeto (
id INT AUTO_INCREMENT PRIMARY KEY,
cliente_id INT,

nome VARCHAR(255) NOT NULL,
descricao TEXT,

data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
data_inicio TIMESTAMP,
data_fim TIMESTAMP,

status_id INT NULL,

horas_estimadas DECIMAL(10,2) DEFAULT 0.00,

cor VARCHAR(20),
risco VARCHAR(255) NULL,

FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);


-- =========================================================
-- USUARIOS DO PROJETO (N:N)
-- =========================================================

CREATE TABLE IF NOT EXISTS projeto_usuario (
id INT AUTO_INCREMENT PRIMARY KEY,
projeto_id INT,
usuario_id INT,
papel ENUM('Admin', 'Colaborador') DEFAULT 'Colaborador',
UNIQUE KEY ux_projeto_usuario (projeto_id, usuario_id),

FOREIGN KEY (projeto_id) REFERENCES projeto(id),
FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);


-- =========================================================
-- SPRINT
-- =========================================================

CREATE TABLE IF NOT EXISTS sprint (
id INT AUTO_INCREMENT PRIMARY KEY,
projeto_id INT,
nome VARCHAR(100),
data_inicio TIMESTAMP,
data_fim TIMESTAMP,
status_id INT NULL,

FOREIGN KEY (projeto_id) REFERENCES projeto(id)
);


-- =========================================================
-- SPRINT_USUARIO (N:N)
-- liga sprint <-> usuario; inclui papel e UNIQUE(sprint_id, usuario_id)
-- =========================================================

CREATE TABLE IF NOT EXISTS sprint_usuario (
id INT AUTO_INCREMENT PRIMARY KEY,
sprint_id INT,
usuario_id INT,
papel ENUM('Admin', 'Colaborador') DEFAULT 'Colaborador',
UNIQUE KEY ux_sprint_usuario (sprint_id, usuario_id),

FOREIGN KEY (sprint_id) REFERENCES sprint(id) ON DELETE CASCADE ON UPDATE CASCADE,
FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);


-- =========================================================
-- CRONOGRAMA
-- =========================================================

CREATE TABLE IF NOT EXISTS cronograma (
id INT AUTO_INCREMENT PRIMARY KEY,
projeto_id INT,
data_inicio TIMESTAMP,
data_fim TIMESTAMP,

FOREIGN KEY (projeto_id) REFERENCES projeto(id)
);

-- =========================================================
-- PRIORIDADE
-- =========================================================

CREATE TABLE IF NOT EXISTS prioridade (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(50)
);

INSERT IGNORE INTO prioridade (nome) VALUES
('Baixa'),
('Média'),
('Alta'),
('Crítica');


-- =========================================================
-- TAREFA
-- NOTE: removed tarefa_pai_id and is_subtarefa (no subtasks model)
-- =========================================================

CREATE TABLE IF NOT EXISTS tarefa (
id INT AUTO_INCREMENT PRIMARY KEY,
projeto_id INT,
cronograma_id INT,
sprint_id INT,

criado_por INT,
atribuido_para INT,

nome VARCHAR(255) NOT NULL,
descricao TEXT,

prazo TIMESTAMP,
story_points INT,

horas_estimadas DECIMAL(10,2) DEFAULT 0.00,

status_id INT,
prioridade_id INT,

data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
data_conclusao TIMESTAMP NULL,

INDEX idx_tarefa_projeto (projeto_id),
INDEX idx_tarefa_sprint (sprint_id),
INDEX idx_tarefa_atribuido (atribuido_para),

FOREIGN KEY (projeto_id) REFERENCES projeto(id),
FOREIGN KEY (cronograma_id) REFERENCES cronograma(id),
FOREIGN KEY (sprint_id) REFERENCES sprint(id),
FOREIGN KEY (criado_por) REFERENCES usuario(id),
FOREIGN KEY (atribuido_para) REFERENCES usuario(id),
FOREIGN KEY (status_id) REFERENCES `status`(id),
FOREIGN KEY (prioridade_id) REFERENCES prioridade(id),

-- composite FKs to ensure assignee belongs to the sprint and the project
CONSTRAINT fk_tarefa_sprint_usuario FOREIGN KEY (sprint_id, atribuido_para)
    REFERENCES sprint_usuario (sprint_id, usuario_id) ON UPDATE CASCADE ON DELETE NO ACTION,
CONSTRAINT fk_tarefa_projeto_usuario FOREIGN KEY (projeto_id, atribuido_para)
    REFERENCES projeto_usuario (projeto_id, usuario_id) ON UPDATE CASCADE ON DELETE NO ACTION
);


-- =========================================================
-- TAG
-- =========================================================

CREATE TABLE IF NOT EXISTS tag (
id INT AUTO_INCREMENT PRIMARY KEY,
nome VARCHAR(100) NOT NULL
);


-- =========================================================
-- TAREFA_TAG (N:N)
-- =========================================================

CREATE TABLE IF NOT EXISTS tarefa_tag (
tarefa_id INT,
tag_id INT,
PRIMARY KEY (tarefa_id, tag_id),

FOREIGN KEY (tarefa_id) REFERENCES tarefa(id),
FOREIGN KEY (tag_id) REFERENCES tag(id)
);


-- =========================================================
-- ANEXOS
-- =========================================================

CREATE TABLE IF NOT EXISTS anexo (
id INT AUTO_INCREMENT PRIMARY KEY,
tarefa_id INT,
nome_arquivo VARCHAR(255),
caminho_arquivo TEXT,
data_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

FOREIGN KEY (tarefa_id) REFERENCES tarefa(id)
);


-- =========================================================
-- COMENTARIOS
-- =========================================================

CREATE TABLE IF NOT EXISTS comentario (
id INT AUTO_INCREMENT PRIMARY KEY,
tarefa_id INT,
usuario_id INT,
conteudo TEXT,
data_criacao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

FOREIGN KEY (tarefa_id) REFERENCES tarefa(id),
FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);


-- =========================================================
-- HISTORICO (AUDIT LOG)
-- =========================================================

CREATE TABLE IF NOT EXISTS historico_tarefa (
id INT AUTO_INCREMENT PRIMARY KEY,
tarefa_id INT,
usuario_id INT,
campo_alterado VARCHAR(100),
valor_antigo TEXT,
valor_novo TEXT,
data_alteracao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

FOREIGN KEY (tarefa_id) REFERENCES tarefa(id),
FOREIGN KEY (usuario_id) REFERENCES usuario(id)
);


-- =========================================================
-- SEEDS: organizacao, usuario, cliente, projeto, projeto_usuario, sprint, cronograma, tag, tarefa, tarefa_tag, anexo, comentario, historico_tarefa
-- (Adaptado: removed subtasks columns from tarefa inserts)
-- =========================================================

INSERT IGNORE INTO organizacao (
nome_fantasia,
cnpj,
email_contato
) VALUES
(
'TechNova Soluções',
'12.345.678/0001-90',
'contato@technova.com.br'
),
(
'Inova Digital',
'23.456.789/0001-01',
'contato@inovadigital.com.br'
),
(
'BlueSoft Tecnologia',
'34.567.890/0001-12',
'contato@bluesoft.com.br'
);


INSERT IGNORE INTO usuario (
nome,
email,
is_ativo,
perfil_id,
cargo,
data_desativacao,
organizacao_id
) VALUES
('Ana Carolina Souza','ana.souza@email.com',TRUE,1,'Gerente de Projetos',NULL,1),
('Bruno Henrique Lima','bruno.lima@email.com',TRUE,2,'Desenvolvedor Backend',NULL,1),
('Camila Oliveira Santos','camila.santos@email.com',TRUE,2,'Desenvolvedora Frontend',NULL,1),
('Daniel Martins','daniel.martins@email.com',TRUE,2,'QA Engineer',NULL,1),
('Eduardo Ferreira','eduardo.ferreira@email.com',TRUE,1,'Tech Lead',NULL,2),
('Fernanda Alves','fernanda.alves@email.com',TRUE,2,'UX/UI Designer',NULL,2),
('Gabriel Rocha','gabriel.rocha@email.com',TRUE,2,'Desenvolvedor Full Stack',NULL,2),
('Helena Costa','helena.costa@email.com',FALSE,2,'Analista de Negócios','2026-08-15 10:30:00',3);


INSERT IGNORE INTO cliente (
nome,
email_contato,
telefone,
is_ativo,
data_desativacao
) VALUES
('Empresa Alpha Tecnologia','contato@alpha.com.br','(11) 99999-1111',TRUE,NULL),
('Mercado Fácil','contato@mercadofacil.com.br','(11) 98888-2222',TRUE,NULL),
('StartUp Solutions','contato@startupsolutions.com.br','(11) 97777-3333',TRUE,NULL),
('Grupo Financeiro Brasil','contato@gfb.com.br','(11) 96666-4444',FALSE,'2026-07-20 15:00:00');


INSERT IGNORE INTO projeto (
cliente_id,
nome,
descricao,
data_inicio,
data_fim,
status_id,
horas_estimadas,
cor,
risco
) VALUES
(1,'Portal Corporativo','Desenvolvimento de um novo portal corporativo para gerenciamento de clientes.','2026-07-01 09:00:00','2026-08-20 18:00:00',(SELECT id FROM `status` WHERE nome = 'Concluída'),320.00,'#FF5733',NULL),
(1,'Aplicativo Mobile','Aplicativo mobile para acompanhamento de pedidos e notificações.','2026-07-15 09:00:00','2026-08-30 18:00:00',(SELECT id FROM `status` WHERE nome = 'Concluída'),280.00,'#3498DB',NULL),
(2,'E-commerce Mercado Fácil','Desenvolvimento da plataforma de vendas online.','2026-06-10 09:00:00','2026-07-30 18:00:00',(SELECT id FROM `status` WHERE nome = 'Concluída'),400.00,'#2ECC71',NULL),
(4,'Sistema de Gestão','Sistema interno para gerenciamento de tarefas e equipes.','2026-08-01 09:00:00','2026-09-15 18:00:00',(SELECT id FROM `status` WHERE nome = 'Em andamento'),360.00,'#9B59B6',NULL);


INSERT IGNORE INTO projeto_usuario (projeto_id, usuario_id, papel) VALUES
(1, 1, 'Admin'),
(1, 2, 'Colaborador'),
(1, 3, 'Colaborador'),
(1, 4, 'Colaborador'),
(2, 5, 'Admin'),
(2, 6, 'Colaborador'),
(2, 7, 'Colaborador'),
(2, 4, 'Colaborador'),
(3, 1, 'Admin'),
(3, 2, 'Colaborador'),
(3, 7, 'Colaborador'),
(3, 6, 'Colaborador'),
(4, 5, 'Admin'),
(4, 3, 'Colaborador'),
(4, 4, 'Colaborador'),
(4, 7, 'Colaborador');


INSERT IGNORE INTO sprint (projeto_id, nome, data_inicio, data_fim, status_id) VALUES
(1, 'Sprint 01 - Estrutura', '2026-07-01 09:00:00', '2026-07-14 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(1, 'Sprint 02 - Funcionalidades', '2026-07-15 09:00:00', '2026-07-28 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(1, 'Sprint 03 - Melhorias', '2026-07-29 09:00:00', '2026-08-12 18:00:00', (SELECT id FROM `status` WHERE nome = 'Em andamento')),
(2, 'Sprint 01 - Mobile', '2026-07-15 09:00:00', '2026-07-29 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(2, 'Sprint 02 - Integração', '2026-07-30 09:00:00', '2026-08-13 18:00:00', (SELECT id FROM `status` WHERE nome = 'Em andamento')),
(3, 'Sprint 01 - E-commerce', '2026-06-10 09:00:00', '2026-06-24 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(3, 'Sprint 02 - Checkout', '2026-06-25 09:00:00', '2026-07-09 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(3, 'Sprint 03 - Pagamento', '2026-07-10 09:00:00', '2026-07-24 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(4, 'Sprint 01 - MVP', '2026-08-01 09:00:00', '2026-08-15 18:00:00', (SELECT id FROM `status` WHERE nome = 'Finalizada')),
(4, 'Sprint 02 - Relatórios', '2026-08-16 09:00:00', '2026-08-30 18:00:00', (SELECT id FROM `status` WHERE nome = 'Em andamento'));

-- populate sprint_usuario by associating each sprint with its project members
INSERT INTO sprint_usuario (sprint_id, usuario_id, papel)
SELECT s.id, pu.usuario_id, pu.papel
FROM sprint s
JOIN projeto_usuario pu ON pu.projeto_id = s.projeto_id;

-- add foreign keys from projeto.status_id and sprint.status_id to status
ALTER TABLE projeto ADD CONSTRAINT fk_projeto_status FOREIGN KEY (status_id) REFERENCES `status`(id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE sprint ADD CONSTRAINT fk_sprint_status FOREIGN KEY (status_id) REFERENCES `status`(id) ON UPDATE CASCADE ON DELETE SET NULL;


INSERT IGNORE INTO cronograma (projeto_id, data_inicio, data_fim) VALUES
(1, '2026-07-01 09:00:00', '2026-08-20 18:00:00'),
(2, '2026-07-15 09:00:00', '2026-08-30 18:00:00'),
(3, '2026-06-10 09:00:00', '2026-07-30 18:00:00'),
(4, '2026-08-01 09:00:00', '2026-09-15 18:00:00');


INSERT IGNORE INTO tag (nome) VALUES
('Backend'),('Frontend'),('Bug'),('Melhoria'),('Urgente'),('Documentação'),('Banco de Dados'),('API'),('Design'),('Testes');


-- =========================================================
-- TAREFAS (sem subtasks)
-- =========================================================

INSERT IGNORE INTO tarefa (
projeto_id,
cronograma_id,
sprint_id,
criado_por,
atribuido_para,
nome,
descricao,
prazo,
story_points,
horas_estimadas,
status_id,
prioridade_id,
data_conclusao
) VALUES
(1, 1, 3, 1, 2, 'Criar API de autenticação', 'Implementar autenticação utilizando JWT.', '2026-08-10 18:00:00', 8, 24.00, 3, 3, '2026-08-09 16:30:00'),
(1, 1, 3, 1, 3, 'Criar tela de login', 'Desenvolver interface de login do portal.', '2026-08-11 18:00:00', 5, 16.00, 3, 2, '2026-08-10 14:20:00'),
(1, 1, 3, 1, 4, 'Testar autenticação', 'Criar testes funcionais para o fluxo de login.', '2026-08-12 18:00:00', 3, 10.00, 2, 2, NULL),
(1, 2, 3, 2, 2, 'Corrigir validação de senha', 'Corrigir regra de validação da senha no backend.', '2026-08-15 18:00:00', 3, 8.00, 1, 3, NULL),
(1, 2, 3, 1, 3, 'Atualizar documentação da API', 'Documentar endpoints utilizando Swagger.', '2026-08-18 18:00:00', 2, 6.00, 1, 1, NULL),
(2, 2, 5, 5, 7, 'Implementar notificações push', 'Criar serviço responsável pelo envio de notificações.', '2026-08-12 18:00:00', 8, 24.00, 2, 3, NULL),
(2, 2, 5, 5, 6, 'Criar tela de pedidos', 'Criar tela para visualização dos pedidos do cliente.', '2026-08-15 18:00:00', 5, 16.00, 2, 2, NULL),
(2, 2, 5, 5, 4, 'Testar fluxo de pedidos', 'Realizar testes no fluxo completo de pedidos.', '2026-08-18 18:00:00', 3, 10.00, 1, 2, NULL),
(3, 3, 8, 1, 2, 'Criar integração com gateway', 'Integrar sistema com gateway de pagamento.', '2026-07-20 18:00:00', 8, 24.00, 3, 3, '2026-07-19 17:00:00'),
(3, 3, 8, 1, 7, 'Implementar checkout', 'Desenvolver fluxo completo de checkout.', '2026-07-22 18:00:00', 13, 40.00, 3, 3, '2026-07-21 15:00:00'),
(3, 3, 8, 1, 6, 'Melhorar layout do checkout', 'Ajustar experiência visual da página de checkout.', '2026-07-25 18:00:00', 5, 16.00, 3, 2, '2026-07-24 13:00:00'),
(3, 3, 8, 2, 4, 'Testar pagamento', 'Validar pagamentos aprovados, recusados e expirados.', '2026-07-27 18:00:00', 5, 16.00, 2, 3, NULL),
(4, 4, 10, 5, 3, 'Criar dashboard', 'Criar dashboard principal do sistema.', '2026-08-25 18:00:00', 8, 24.00, 2, 2, NULL),
(4, 4, 10, 5, 7, 'Criar endpoint de relatórios', 'Criar endpoint REST para consulta de relatórios.', '2026-08-27 18:00:00', 5, 16.00, 2, 3, NULL),
(4, 4, 10, 5, 4, 'Validar relatórios', 'Validar os dados apresentados nos relatórios.', '2026-08-29 18:00:00', 3, 10.00, 1, 2, NULL);


-- previously subtasks are now inserted as normal tasks (no parent relation)
INSERT IGNORE INTO tarefa (projeto_id, cronograma_id, sprint_id, criado_por, atribuido_para, nome, descricao, prazo, story_points, horas_estimadas, status_id, prioridade_id, data_conclusao) VALUES
(1,1,3,2,2,'Criar entidade de usuário','Criar entidade User no backend.','2026-08-05 18:00:00',2,6.00,3,2,'2026-08-04 16:00:00'),
(1,1,3,2,2,'Criar JWT Service','Implementar serviço responsável pela criação dos tokens.','2026-08-07 18:00:00',3,8.00,3,3,'2026-08-07 17:30:00'),
(1,1,3,1,3,'Criar formulário de login','Criar formulário com email e senha.','2026-08-08 18:00:00',2,6.00,3,2,'2026-08-08 14:00:00');


INSERT IGNORE INTO tarefa_tag (tarefa_id, tag_id) VALUES
(1,1),(1,8),(2,2),(2,9),(3,10),(4,1),(4,3),(5,6),(6,1),(6,8),(7,2),(8,10),(9,1),(9,8),(10,2),(11,9),(12,10),(13,2),(14,1),(14,8),(15,10);


INSERT IGNORE INTO anexo (tarefa_id, nome_arquivo, caminho_arquivo) VALUES
(1,'arquitetura-auth.png','/uploads/tarefas/1/arquitetura-auth.png'),
(1,'jwt-config.json','/uploads/tarefas/1/jwt-config.json'),
(2,'tela-login.png','/uploads/tarefas/2/tela-login.png'),
(5,'swagger-api.pdf','/uploads/tarefas/5/swagger-api.pdf'),
(6,'push-notification.pdf','/uploads/tarefas/6/push-notification.pdf'),
(10,'checkout-layout.png','/uploads/tarefas/10/checkout-layout.png'),
(14,'dashboard-wireframe.png','/uploads/tarefas/14/dashboard-wireframe.png');


INSERT IGNORE INTO comentario (tarefa_id, usuario_id, conteudo) VALUES
(1,2,'A autenticação JWT foi implementada. Falta apenas finalizar os testes.'),
(1,1,'Ótimo. Precisamos validar também o tempo de expiração do token.'),
(2,3,'A tela de login já está pronta para revisão.'),
(3,4,'Encontrei um problema na validação de senha com caracteres especiais.'),
(4,2,'A regra foi ajustada e enviada para homologação.'),
(6,7,'A integração com o serviço de notificações está em andamento.'),
(7,6,'A tela está pronta, aguardando integração com a API.'),
(9,2,'Gateway integrado com sucesso em ambiente de homologação.'),
(10,7,'Checkout finalizado. Precisamos apenas validar alguns cenários.'),
(11,6,'Realizei os ajustes de responsividade.'),
(12,4,'Os testes encontraram um erro no pagamento recusado.'),
(14,3,'Dashboard em desenvolvimento.'),
(15,7,'Endpoint já está disponível para testes.');


INSERT IGNORE INTO historico_tarefa (tarefa_id, usuario_id, campo_alterado, valor_antigo, valor_novo) VALUES
(1,2,'status_id','2','3'),
(1,2,'prioridade_id','2','3'),
(2,3,'status_id','2','3'),
(3,4,'status_id','1','2'),
(4,2,'status_id','3','1'),
(6,7,'status_id','1','2'),
(7,6,'status_id','1','2'),
(9,2,'status_id','2','3'),
(9,2,'prioridade_id','2','3'),
(10,7,'status_id','2','3'),
(11,6,'status_id','2','3'),
(12,4,'status_id','1','2'),
(14,3,'status_id','1','2'),
(15,7,'status_id','1','2');

-- End of schema

USE task_manager;

SELECT * FROM organizacao;
SELECT * FROM perfil;
SELECT * FROM usuario;
SELECT * FROM cliente;
SELECT * FROM projeto;
SELECT * FROM projeto_usuario;
SELECT * FROM sprint;
SELECT * FROM sprint_usuario;
SELECT * FROM cronograma;
SELECT * FROM `status`;
SELECT * FROM prioridade;
SELECT * FROM tarefa;
SELECT * FROM tag;
SELECT * FROM tarefa_tag;
SELECT * FROM anexo;
SELECT * FROM comentario;
SELECT * FROM historico_tarefa;
