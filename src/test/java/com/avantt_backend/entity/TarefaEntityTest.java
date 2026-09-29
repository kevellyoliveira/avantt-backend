package com.avantt_backend.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class TarefaEntityTest {

    @Test
    void setAndGetFields() {
        Tarefa t = new Tarefa();
        t.setId(11);
        t.setNome("Fix bug");
        t.setTitulo("Fix bug");
        t.setProjetoId(2);
        t.setSprintId(3);
        t.setAssignee(5);
        t.setAvatar("A");
        t.setAvatarColor("#fff");
        t.setPriority("alta");
        t.setStatus("em andamento");
        t.setStatusId(2);
        t.setDaysDelayed(0);
        t.setPlannedEnd(LocalDate.of(2027,8,1));
        t.setEstimatedHours(4);
        t.setBlockedBy(null);
        t.setTags("[]");
        t.setDescricao("Detalhes");

        assertEquals(11, t.getId());
        assertEquals("Fix bug", t.getNome());
        assertEquals(2, t.getProjetoId());
        assertEquals(3, t.getSprintId());
        assertEquals(5, t.getAssignee());
        assertEquals("A", t.getAvatar());
        assertEquals("#fff", t.getAvatarColor());
        assertEquals("alta", t.getPriority());
        assertEquals("em andamento", t.getStatus());
        assertEquals(2, t.getStatusId());
        assertEquals(0, t.getDaysDelayed());
        assertEquals(LocalDate.of(2027,8,1), t.getPlannedEnd());
        assertEquals(4, t.getEstimatedHours());
        assertEquals("[]", t.getTags());
        assertEquals("Detalhes", t.getDescricao());
    }
}
