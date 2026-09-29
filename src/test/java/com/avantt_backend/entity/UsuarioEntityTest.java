package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioEntityTest {

    @Test
    void defaultIsAtivo_shouldBeTrue() {
        Usuario u = new Usuario();
        assertNotNull(u.getIsAtivo());
        assertTrue(u.getIsAtivo());
    }

    @Test
    void setIsAtivo_and_dataDesativacao_behavior() {
        Usuario u = new Usuario();
        assertTrue(u.getIsAtivo());
        u.setIsAtivo(false);
        assertFalse(u.getIsAtivo());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        u.setDataDesativacao(now);
        assertEquals(now, u.getDataDesativacao());
    }
}
