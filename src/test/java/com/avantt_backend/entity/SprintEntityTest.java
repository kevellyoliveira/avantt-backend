package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class SprintEntityTest {

    @Test
    void setAndGetFields() {
        Sprint s = new Sprint();
        s.setId(3);
        s.setProjetoId(7);
        s.setNome("Sprint X");
        s.setObjetivo("Entrega do fluxo");
        s.setStatus("Em andamento");
        s.setDataInicio(LocalDate.of(2027,5,1));
        s.setDataFim(LocalDate.of(2027,5,20));
        s.setProgresso(40);

        assertEquals(3, s.getId());
        assertEquals(7, s.getProjetoId());
        assertEquals("Sprint X", s.getNome());
        assertEquals("Entrega do fluxo", s.getObjetivo());
        assertEquals("Em andamento", s.getStatus());
        assertEquals(LocalDate.of(2027,5,1), s.getDataInicio());
        assertEquals(LocalDate.of(2027,5,20), s.getDataFim());
        assertEquals(40, s.getProgresso());
    }
}
