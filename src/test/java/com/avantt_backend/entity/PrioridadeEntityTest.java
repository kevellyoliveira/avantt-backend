package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PrioridadeEntityTest {

    @Test
    void setAndGetFields() {
        Prioridade p = new Prioridade();
        p.setId(2);
        p.setNome("Média");

        assertEquals(2, p.getId());
        assertEquals("Média", p.getNome());
    }
}
