package com.avantt_backend.exception;

/**
 * Exceção base para erros da API.
 * Mantém compatibilidade com usos existentes que lançam ApiException diretamente.
 */
public class ApiException extends RuntimeException {
    public ApiException() { super(); }
    public ApiException(String message) { super(message); }
    public ApiException(String message, Throwable cause) { super(message, cause); }
    public ApiException(Throwable cause) { super(cause); }
}
