package com.avantt_backend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProjetoRequestDTOValidationTest {

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
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("Projeto X");
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));

        Set<ConstraintViolation<ProjetoRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void missingRequiredFields_triggerViolations() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        // missing name, statusId, dates

        Set<ConstraintViolation<ProjetoRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        boolean hasName = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("name"));
        boolean hasStatus = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("statusId"));
        boolean hasStart = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate"));
        boolean hasEnd = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("endDate"));

        assertTrue(hasName);
        assertTrue(hasStatus);
        assertTrue(hasStart);
        assertTrue(hasEnd);
    }

    @Test
    void invalidDates_triggerViolations() {
        ProjetoRequestDTO dto = new ProjetoRequestDTO();
        dto.setName("P");
        dto.setStatusId(1);
        dto.setStartDate(LocalDate.now().minusDays(1)); // past
        dto.setEndDate(LocalDate.now().minusDays(1)); // past

        Set<ConstraintViolation<ProjetoRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("endDate")));
    }
}
