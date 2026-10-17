package com.atlashub.logistics.warehousing.infrastructure.persistence.mappers;

import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class WarehousingJsonMapper {

    private final ObjectMapper objectMapper;

    public WarehousingJsonMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String itemsToJson(List<StockTransferItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(items);
        } catch (Exception e) {
            return null;
        }
    }

    public List<StockTransferItem> jsonToItems(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<StockTransferItem>>() {});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
