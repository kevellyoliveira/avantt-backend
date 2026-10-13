package com.avantt_backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void apiPaths_and_mensagens_constants() {
        assertEquals("/api", ApiPaths.API);
        assertTrue(ApiPaths.TAREFAS.contains(ApiPaths.API));
        assertNotNull(Mensagens.MENSAGEM_ERRO_INTERNO_500);
    }

    @Test
    void jsonAttributeConverter_serializes() {
        JsonAttributeConverter c = new JsonAttributeConverter();
        String s = (String) c.convertToDatabaseColumn(java.util.Map.of("a", 1));
        assertTrue(s.contains("\"a\""));
        // Jackson writes arrays without spaces by default
        assertEquals("[1,2]", c.convertToDatabaseColumn(new int[]{1,2}));
        assertNull(c.convertToDatabaseColumn(null));
        // convertToEntityAttribute returns the raw string
        assertEquals("raw", c.convertToEntityAttribute("raw"));
    }
}
