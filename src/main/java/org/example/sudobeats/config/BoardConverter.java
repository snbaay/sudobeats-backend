package org.example.sudobeats.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts a 9x9 int[][] board to a compact JSON string for PostgreSQL TEXT storage
 * and back again.  The ObjectMapper is instantiated once and reused (thread-safe).
 */
@Converter
public class BoardConverter implements AttributeConverter<int[][], String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(int[][] board) {
        if (board == null) return null;
        try {
            return MAPPER.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialise board to JSON", e);
        }
    }

    @Override
    public int[][] convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, int[][].class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialise board from JSON", e);
        }
    }
}
