package com.avantt_backend.exception;

import com.avantt_backend.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @Test
    void handleValidationException_buildsErrorResponse() {
        BindingResult br = mock(BindingResult.class);
        when(br.getFieldErrors()).thenReturn(List.of(new FieldError("obj","field","must not be null")));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(br);

        GlobalExceptionHandler h = new GlobalExceptionHandler();
        ResponseEntity<ErrorResponse> r = h.handleValidationException(ex);
        assertEquals(400, r.getStatusCode().value());
        ErrorResponse body = r.getBody();
        assertNotNull(body);
        assertTrue(body.getErrors().containsKey("field"));
    }
}
