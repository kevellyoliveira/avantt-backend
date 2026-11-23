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
        dto.setStatus("Planejada");
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(20));

        Set<ConstraintViolation<ProjetoRequestDTO>> violations =
                validator.validate(dto);

        assertTrue(
                violations.isEmpty(),
                "O DTO válido não deveria apresentar violações: " + violations
        );
    }

    @Test
    void missingRequiredFields_triggerViolations() {

        ProjetoRequestDTO dto = new ProjetoRequestDTO();

        // Nome, data inicial e data final não foram informados.
        Set<ConstraintViolation<ProjetoRequestDTO>> violations =
                validator.validate(dto);

        assertFalse(violations.isEmpty());

        boolean hasName = violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("name"));

        boolean hasStart = violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("startDate"));

        boolean hasEnd = violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("endDate"));

        assertTrue(hasName, "Deveria validar o campo name");
        assertTrue(hasStart, "Deveria validar o campo startDate");
        assertTrue(hasEnd, "Deveria validar o campo endDate");
    }

    @Test
    void invalidDates_triggerViolations() {

        ProjetoRequestDTO dto = new ProjetoRequestDTO();

        dto.setName("Projeto X");
        dto.setStatus("Planejada");
        dto.setStartDate(LocalDate.now().minusDays(1));
        dto.setEndDate(LocalDate.now().minusDays(1));

        Set<ConstraintViolation<ProjetoRequestDTO>> violations =
                validator.validate(dto);

        assertFalse(violations.isEmpty());

        boolean hasStart = violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("startDate"));

        boolean hasEnd = violations.stream()
                .anyMatch(v ->
                        v.getPropertyPath().toString().equals("endDate"));

        assertTrue(hasStart, "A data inicial no passado deveria ser inválida");
        assertTrue(hasEnd, "A data final no passado deveria ser inválida");
    }
}