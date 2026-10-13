package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PerfilEntityTest {

    @Test
    void setAndGetFields() {
        Perfil p = new Perfil();
        p.setId(1);
        p.setNome("Admin");

        assertEquals(1, p.getId());
        assertEquals("Admin", p.getNome());
    }
}
