package com.avantt_backend.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusUtilsTest {

    @Test
    void normalizeProjectStatus_nullOrBlank() {
        assertNull(StatusUtils.normalizeProjectStatus(null));
        assertNull(StatusUtils.normalizeProjectStatus("   "));
    }

    @Test
    void normalizeProjectStatus_knownVariants() {
        assertEquals("Planejado", StatusUtils.normalizeProjectStatus("planejado"));
        assertEquals("Planejado", StatusUtils.normalizeProjectStatus("planned"));
        assertEquals("Em andamento", StatusUtils.normalizeProjectStatus("in progress"));
        assertEquals("Em andamento", StatusUtils.normalizeProjectStatus("andamento"));
        assertEquals("Concluído", StatusUtils.normalizeProjectStatus("done"));
        assertEquals("Concluído", StatusUtils.normalizeProjectStatus("concluído"));
        assertEquals("Cancelado", StatusUtils.normalizeProjectStatus("cancelled"));
    }

    @Test
    void normalizeProjectStatus_fallbackCapitalization() {
        assertEquals("Customstatus", StatusUtils.normalizeProjectStatus("customstatus"));
        assertEquals("Custom status", StatusUtils.normalizeProjectStatus(" custom status "));
    }

    @Test
    void isDoneAndIsCancelled() {
        assertTrue(StatusUtils.isDone("done"));
        assertTrue(StatusUtils.isDone("Concluído"));
        assertFalse(StatusUtils.isDone("andamento"));

        assertTrue(StatusUtils.isCancelled("cancel"));
        assertFalse(StatusUtils.isCancelled("done"));
    }

    @Test
    void normalizeTaskStatus_variantsAndFallback() {
        assertEquals("planejada", StatusUtils.normalizeTaskStatus("to do"));
        assertEquals("planejada", StatusUtils.normalizeTaskStatus("TODO"));
        assertEquals("em andamento", StatusUtils.normalizeTaskStatus("in progress"));
        assertEquals("concluída", StatusUtils.normalizeTaskStatus("done"));
        // fallback returns lowercased trimmed
        assertEquals("custom", StatusUtils.normalizeTaskStatus("  CuStOm  "));
    }

    @Test
    void normalizePriority_variants() {
        assertEquals("baixa", StatusUtils.normalizePriority("baixa"));
        assertEquals("média", StatusUtils.normalizePriority("media"));
        assertEquals("alta", StatusUtils.normalizePriority("Alta"));
        assertEquals("crítica", StatusUtils.normalizePriority("crit"));
        assertNull(StatusUtils.normalizePriority(null));
    }
}
