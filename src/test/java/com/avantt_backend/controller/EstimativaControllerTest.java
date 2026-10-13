package com.avantt_backend.controller;

import com.avantt_backend.dto.EstimativaRequestDTO;
import com.avantt_backend.dto.EstimativaResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class EstimativaControllerTest {

    EstimativaController controller = new EstimativaController();

    @Test
    void calcular_basicWithinLimits() {
        EstimativaRequestDTO req = new EstimativaRequestDTO();
        req.setPessoas(5);
        req.setSprints(2);
        req.setQuantidadeDiasSprint(15);

        ResponseEntity<?> resp = controller.calcular(req);
        assertEquals(200, resp.getStatusCode().value());
        assertTrue(resp.getBody() instanceof EstimativaResponseDTO);
        EstimativaResponseDTO dto = (EstimativaResponseDTO) resp.getBody();
        assertTrue(dto.getTempoEstimado() > 0);
        assertNotNull(dto.getPessoasCenarios());
        assertNotNull(dto.getSprintsCenarios());
    }

    @Test
    void calcular_rejectsLimitsExceeded() {
        EstimativaRequestDTO req = new EstimativaRequestDTO();
        req.setPessoas(1000); // exceed default limitePessoas
        req.setSprints(200);
        req.setQuantidadeDiasSprint(10); // below minimum

        var resp = controller.calcular(req);
        assertEquals(400, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
    }

    @Test
    void tempoEstimado_invalidArgs_throw() throws Exception {
        // use reflection to access private method tempoEstimado
        var m = EstimativaController.class.getDeclaredMethod("tempoEstimado", int.class, int.class, int.class);
        m.setAccessible(true);
        // invalid: zero pessoas
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> m.invoke(controller, 0, 1, 15));
        // invalid: diasPorSprint too low
        assertThrows(java.lang.reflect.InvocationTargetException.class, () -> m.invoke(controller, 1, 1, 10));
    }
}
