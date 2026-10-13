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

class SprintRequestDTOValidationTest {

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
    void validDto_noViolations() {
        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setProjectId(1);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));
        dto.setStatusId(1);

        Set<ConstraintViolation<SprintRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void missingRequiredFields_triggerViolations() {
        SprintRequestDTO dto = new SprintRequestDTO();
        // missing startDate, endDate, statusId

        Set<ConstraintViolation<SprintRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        boolean hasStart = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate"));
        boolean hasEnd = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("endDate"));
        boolean hasStatus = violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("statusId"));

        assertTrue(hasStart);
        assertTrue(hasEnd);
        assertTrue(hasStatus);
    }

    @Test
    void invalidDates_triggerViolations() {
        SprintRequestDTO dto = new SprintRequestDTO();
        dto.setProjectId(1);
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now().minusDays(1));
        dto.setStatusId(1);

        Set<ConstraintViolation<SprintRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("startDate")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("endDate")));
    }
}
