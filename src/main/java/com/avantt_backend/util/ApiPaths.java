package com.avantt_backend.util;

/**
 * Constantes de caminhos de API para serem usadas pelos controllers.
 */
public final class ApiPaths {

    private ApiPaths() {
        // util class
    }

    public static final String API = "/api";
    public static final String TAREFAS = API + "/tarefas";
    public static final String SPRINTS = API + "/sprints";
    public static final String PROJETOS = API + "/projetos";
    public static final String USUARIOS = API + "/usuarios";
}
