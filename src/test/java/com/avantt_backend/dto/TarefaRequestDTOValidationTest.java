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

class TarefaRequestDTOValidationTest {

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
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("T1");
        dto.setProjectId(1);
        dto.setSprintId(2);
        dto.setStatusId(1);
        dto.setPlannedEnd(LocalDate.now().plusDays(5));

        Set<ConstraintViolation<TarefaRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void missingRequiredFields_triggerViolations() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        // missing title, projectId, sprintId, statusId, plannedEnd

        Set<ConstraintViolation<TarefaRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());

        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("title")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("projectId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("sprintId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("statusId")));
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("plannedEnd")));
    }

    @Test
    void plannedEndMustBeFuture() {
        TarefaRequestDTO dto = new TarefaRequestDTO();
        dto.setTitle("T1");
        dto.setProjectId(1);
        dto.setSprintId(2);
        dto.setStatusId(1);
        dto.setPlannedEnd(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<TarefaRequestDTO>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("plannedEnd")));
    }
}
