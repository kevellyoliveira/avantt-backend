package com.avantt_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorResponse {
    private int status;
    private String error;
    private String message;
    // Machine-readable error code that frontend can use to decide how to show the message
    private String code;
    private LocalDateTime timestamp = LocalDateTime.now();
    private List<FieldError> details;

    public ErrorResponse() {}

    public ErrorResponse(int status, String error, String message) {
        this.status = status; this.error = error; this.message = message;
    }

    public ErrorResponse(int status, String error, String message, String code) {
        this.status = status; this.error = error; this.message = message; this.code = code;
    }

    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public List<FieldError> getDetails() { return details; }
    public void setDetails(List<FieldError> details) { this.details = details; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public static class FieldError {
        private String field;
        private String message;
        public FieldError() {}
        public FieldError(String field, String message) { this.field = field; this.message = message; }
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
