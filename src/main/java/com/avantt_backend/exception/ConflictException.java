package com.avantt_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exceção para conflitos (409).
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class ConflictException extends ApiException {
    public ConflictException() { super(); }
    public ConflictException(String message) { super(message); }
    public ConflictException(String message, Throwable cause) { super(message, cause); }
    public ConflictException(Throwable cause) { super(cause); }
}
