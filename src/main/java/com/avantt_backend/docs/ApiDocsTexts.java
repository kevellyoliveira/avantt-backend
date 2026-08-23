package com.avantt_backend.docs;

/**
 * Centraliza textos usados nas anotações OpenAPI/Swagger.
 * Mantém os textos em um único lugar para facilitar tradução e manutenção.
 */
public final class ApiDocsTexts {

    private ApiDocsTexts() {}

    // Tags
    public static final String TAG_EXAMPLE = "Exemplo";
    public static final String TAG_EXAMPLE_DESC = "Operações relacionadas a exemplos (ExampleDto)";

    // HomeController - list
    public static final String EXAMPLE_LIST_SUMMARY = "Lista exemplos";
    public static final String EXAMPLE_LIST_DESCRIPTION = "Retorna a lista completa de ExampleDto.";

    // Responses
    public static final String RESP_200 = "Lista retornada com sucesso.";
    public static final String RESP_400 = "Requisição inválida (parâmetros incorretos).";
    public static final String RESP_404 = "Recurso não encontrado.";
    public static final String RESP_401 = "Não autorizado.";
    public static final String RESP_500 = "Erro interno no servidor.";

    // Parameters
    public static final String PARAM_PAGE_DESC = "Número da página (0-based).";
    public static final String PARAM_SIZE_DESC = "Tamanho da página.";
    public static final String PARAM_SORT_DESC = "Ordenação no formato 'campo,asc|desc'.";
    public static final String PARAM_NAME_DESC = "Filtro por nome (contém).";

    public static final String PARAM_PAGE_EX = "0";
    public static final String PARAM_SIZE_EX = "10";
    public static final String PARAM_SORT_EX = "name,asc";
    public static final String PARAM_NAME_EX = "Exemplo";

    // Parameter names (centralizados para uso nas anotações)
    public static final String PARAM_PAGE_NAME = "page";
    public static final String PARAM_SIZE_NAME = "size";
    public static final String PARAM_SORT_NAME = "sort";
    public static final String PARAM_NAME_NAME = "name";

    // Request / Response examples (JSON strings)
    public static final String EXAMPLE_LIST_REQUEST = "{}";
    public static final String EXAMPLE_LIST_RESPONSE = "[{ \"id\": 1, \"name\": \"Exemplo\" }]";

}
