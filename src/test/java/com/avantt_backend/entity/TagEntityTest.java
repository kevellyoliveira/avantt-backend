package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagEntityTest {

    @Test
    void setAndGetFields() {
        Tag t = new Tag();
        t.setId(1);
        t.setNome("Backend");

        assertEquals(1, t.getId());
        assertEquals("Backend", t.getNome());
    }

    @Test
    void constructWithName() {
        Tag t = new Tag("Frontend");
        assertEquals("Frontend", t.getNome());
    }
}
