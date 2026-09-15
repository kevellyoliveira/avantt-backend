package com.avantt_backend.util;

/**
 * Centraliza textos usados nas anotações OpenAPI/Swagger.
 * Mantém os textos em um único lugar para facilitar tradução e manutenção.
 */

public class Mensagens {

    public static final String MENSAGEM_ERRO_INTERNO_500 = "Ocorreu um erro interno ao processar a solicitação.";

    // PROJETOS
    public static final String MENSAGEM_PROJETO_CRIADO_201 = "Projeto criado com sucesso.";
    public static final String MENSAGEM_ERRO_CRIAR_PROJETO_400 = "Não foi possível criar o projeto.";
    public static final String MENSAGEM_ERRO_CRIAR_PROJETO_409 = "Projeto já cadastrado.";
    public static final String MENSAGEM_ERRO_LISTAR_PROJETOS_404 = "Nenhum projeto encontrado.";

    // TAREFAS
    public static final String MENSAGEM_TAREFA_CRIADA_201 = "Tarefa criada com sucesso.";
    public static final String MENSAGEM_ERRO_CRIAR_TAREFA_400 = "Não foi possível criar a tarefa.";
    public static final String MENSAGEM_ERRO_CRIAR_TAREFA_409 = "Tarefa já cadastrada.";
    public static final String MENSAGEM_ERRO_LISTAR_TAREFAS_404 = "Nenhuma tarefa encontrada.";

    // SPRINTS
    public static final String MENSAGEM_SPRINT_CRIADA_201 = "Sprint criada com sucesso.";
    public static final String MENSAGEM_ERRO_CRIAR_SPRINT_400 = "Não foi possível criar a sprint.";
    public static final String MENSAGEM_ERRO_CRIAR_SPRINT_409 = "Sprint já cadastrada.";
    public static final String MENSAGEM_ERRO_LISTAR_SPRINTS_404 = "Nenhuma sprint encontrada.";
    // USUARIOS
    public static final String MENSAGEM_USUARIO_CRIADO_201 = "Usuário criado com sucesso.";
    public static final String MENSAGEM_ERRO_EDITAR_USUARIO_404 = "Não foi possível editar o usuário.";
    public static final String MENSAGEM_ERRO_LISTAR_USUARIOS_404 = "Nenhum usuário encontrado.";
    public static final String MENSAGEM_ERRO_CRIAR_USUARIO_400 = "Não foi possível criar o usuário.";
    public static final String MENSAGEM_ERRO_CRIAR_USUARIO_409 = "Usuário já cadastrado.";

    //AUTH
    public static final String MENSAGEM_EMAIL_CADASTRADO_409 = "Email já cadastrado";
    public static final String MENSAGEM_CREDENCIAIS_INVALIDAS_401 = "Credenciais inválidas";
    public static final String MENSAGEM_ERRO_EMAIL_404 = "Email não encontrado";
    public static final String MENSAGEM_ERRO_TOKEN_400 = "Token inválido ou expirado";


}
