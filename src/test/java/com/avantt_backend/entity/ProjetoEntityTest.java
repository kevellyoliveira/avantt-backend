package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ProjetoEntityTest {

    @Test
    void setAndGetFields() {
        Projeto p = new Projeto();
        p.setId(10);
        p.setName("Projeto Test");
        p.setDescription("Desc");
        p.setColor("#fff");
        p.setStatusId(2);
        p.setClienteId(3);
        p.setOrganizacaoId(4);
        p.setStartDate(LocalDate.of(2027,1,1));
        p.setEndDate(LocalDate.of(2027,1,20));
        p.setProgress(50);

        assertEquals(10, p.getId());
        assertEquals("Projeto Test", p.getName());
        assertEquals("Desc", p.getDescription());
        assertEquals("#fff", p.getColor());
        assertEquals(2, p.getStatusId());
        assertEquals(3, p.getClienteId());
        assertEquals(4, p.getOrganizacaoId());
        assertEquals(LocalDate.of(2027,1,1), p.getStartDate());
        assertEquals(LocalDate.of(2027,1,20), p.getEndDate());
        assertEquals(50, p.getProgress());
    }
}
