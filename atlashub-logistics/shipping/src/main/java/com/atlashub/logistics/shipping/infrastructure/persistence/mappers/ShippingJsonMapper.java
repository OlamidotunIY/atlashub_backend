package com.atlashub.logistics.shipping.infrastructure.persistence.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class ShippingJsonMapper {

    private final ObjectMapper objectMapper;

    public ShippingJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String shipmentIdsToJson(List<Long> shipmentIds) {
        if (shipmentIds == null || shipmentIds.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(shipmentIds);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Long> jsonToShipmentIds(String json) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            return objectMapper.readValue(json, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
