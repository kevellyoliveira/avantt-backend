package com.avantt_backend.service;

import com.avantt_backend.dto.MetricCompletionRateDTO;
import com.avantt_backend.dto.MetricOverdueTaskDTO;
import com.avantt_backend.dto.MetricStatusCountDTO;
import jakarta.persistence.Query;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetricsServiceTest {

    @Mock
    EntityManager em;

    @Mock
    Query query;

    @InjectMocks
    MetricsService metricsService;

    @BeforeEach
    void setup() throws Exception {
        // ensure em is the mock in the service (in case of different injection behavior)
        var f = MetricsService.class.getDeclaredField("em");
        f.setAccessible(true);
        f.set(metricsService, em);
    }

    @Test
    void getStatusDistribution_parsesRows() {
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        Object[] r1 = new Object[]{"Planejado", 5};
        Object[] r2 = new Object[]{"Concluída", 2};
        when(query.getResultList()).thenReturn(List.of(r1, r2));

        List<MetricStatusCountDTO> out = metricsService.getStatusDistribution(1);
        assertEquals(2, out.size());
        assertEquals("Planejado", out.get(0).getStatus());
        assertEquals(5, out.get(0).getQuantidade());
    }

    @Test
    void getCompletionRate_handlesEmptyAndValues() {
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of((Object) new Object[]{3, 10, 30.0}));

        MetricCompletionRateDTO d = metricsService.getCompletionRate(2);
        assertNotNull(d);
        assertEquals(2, d.getProjetoId());
        assertEquals(3, d.getConcluidas());
        assertEquals(10, d.getTotal());
        assertEquals(30.0, d.getPctConclusao());
    }

    @Test
    void getTopOverdue_parsesRows() {
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.setMaxResults(anyInt())).thenReturn(query);
        Object[] r = new Object[]{11, 1, 2, "Tarefa X", "2023-01-01", "Em andamento", 5};
        when(query.getResultList()).thenReturn(List.of((Object) r));

        List<MetricOverdueTaskDTO> out = metricsService.getTopOverdue(1, 10);
        assertEquals(1, out.size());
        assertEquals(11, out.get(0).getTarefaId());
        assertEquals(5, out.get(0).getDiasAtraso());
    }

    @Test
    void getHoursEstimated_handlesEmptyAndNumber() {
        when(em.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyString(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(List.of(123.45));

        Double v = metricsService.getHoursEstimated(1);
        assertEquals(123.45, v);
    }
}
