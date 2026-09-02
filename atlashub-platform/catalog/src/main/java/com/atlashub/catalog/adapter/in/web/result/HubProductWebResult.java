package com.atlashub.catalog.adapter.in.web.result;

import com.atlashub.catalog.application.result.HubProductResult;
import java.time.ZonedDateTime;

public record HubProductWebResult(
        Long id,
        String key,
        String name,
        String description,
        String status,
        ZonedDateTime createdAt,
        ZonedDateTime updatedAt
) {
    public static HubProductWebResult from(HubProductResult appResult) {
        return new HubProductWebResult(
                appResult.id(),
                appResult.key().name(),
                appResult.name(),
                appResult.description(),
                appResult.status().name(),
                appResult.createdAt(),
                appResult.updatedAt()
        );
    }
}
