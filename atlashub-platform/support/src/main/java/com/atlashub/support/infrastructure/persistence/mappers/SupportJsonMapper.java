package com.atlashub.support.infrastructure.persistence.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SupportJsonMapper {

    private final ObjectMapper objectMapper;

    public SupportJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String urlsToJson(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(urls);
        } catch (Exception e) {
            return null;
        }
    }

    public List<String> jsonToUrls(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
