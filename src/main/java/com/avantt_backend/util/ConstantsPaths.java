package com.avantt_backend.util;

/**
 * Constantes de caminhos para recursos estáticos e handlers.
 * Use essas constantes em controllers, templates ou configurações de WebMvc.
 */
public final class ConstantsPaths {

    private ConstantsPaths() { /* utilitário */ }

    // URL base pública para recursos estáticos
    public static final String STATIC = "/static";

    // Handler pattern usado pelo Spring ResourceHandler (ex: "/static/**")
    public static final String STATIC_RESOURCE_HANDLER = STATIC + "/**";

    // Localização no classpath onde recursos estáticos ficam (usado em addResourceHandlers)
    public static final String STATIC_RESOURCE_LOCATION = "classpath:/static/";

    // Exemplo adicional: pasta de imagens dentro de static
    public static final String IMAGES = STATIC + "/images";

}
