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
    // generic status endpoint (replaces previous status-tarefas)
    public static final String STATUS = API + "/status";
    // kept for backward compatibility (deprecated)
    public static final String STATUS_TAREFAS = API + "/status-tarefas";
    public static final String USUARIOS = API + "/usuarios";

}
