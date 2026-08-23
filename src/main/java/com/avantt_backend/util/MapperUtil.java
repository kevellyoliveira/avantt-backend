package com.avantt_backend.util;

import com.avantt_backend.dto.ExampleDto;
import com.avantt_backend.model.ExampleModel;

/**
 * Utilitário simples para mapear entidades para DTOs.
 */
public final class MapperUtil {

    private MapperUtil() {}

    public static ExampleDto toDto(ExampleModel m) {
        if (m == null) return null;
        return new ExampleDto(m.getId(), m.getName());
    }
}
