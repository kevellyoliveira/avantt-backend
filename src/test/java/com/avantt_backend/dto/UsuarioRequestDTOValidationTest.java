package com.avantt_backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioRequestDTOValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void init() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void close() {
        factory.close();
    }

    @Test
    void validDto_hasNoViolations() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("Bruno");
        dto.setEmail("bruno@example.com");
        dto.setPerfilId(1);

        Set<ConstraintViolation<UsuarioRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void missingFields_triggerViolations() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        // missing name, email, perfilId

        Set<ConstraintViolation<UsuarioRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        boolean hasName = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        boolean hasEmail = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        boolean hasPerfil = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("perfilId"));

        assertTrue(hasName);
        assertTrue(hasEmail);
        assertTrue(hasPerfil);
    }

    @Test
    void invalidEmail_triggerViolation() {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setName("X");
        dto.setEmail("not-an-email");
        dto.setPerfilId(1);

        Set<ConstraintViolation<UsuarioRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }
}
