package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatusEntityTest {

    @Test
    void setAndGetFields() {
        Status s = new Status();
        s.setId(1);
        s.setNome("Planejada");

        assertEquals(1, s.getId());
        assertEquals("Planejada", s.getNome());
    }
}
