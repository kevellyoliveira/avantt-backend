package com.avantt_backend.exception;

/**
 * Exceção de exemplo para a camada de API.
 */
public class ApiException extends RuntimeException {

    public ApiException(String message) {
        super(message);
    }

    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
