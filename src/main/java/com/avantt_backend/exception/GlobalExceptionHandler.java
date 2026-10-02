package com.avantt_backend.exception;

import com.avantt_backend.dto.ErrorResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handler global para transformar erros de validação em ErrorResponse consistente.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> {
            // usa a mensagem padrão (que pode ter sido personalizada na annotation)
            errors.put(fe.getField(), fe.getDefaultMessage());
        });

        ErrorResponse err = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Erro de validação", LocalDateTime.now(), errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
}
