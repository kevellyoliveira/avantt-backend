package com.avantt_backend.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonAttributeConverter implements AttributeConverter<Object, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not convert object to JSON", e);
        }
    }

    @Override
    public Object convertToEntityAttribute(String dbData) {
        // Not used generically. Repositories/services read raw JSON and map on their side.
        return dbData;
    }
}
