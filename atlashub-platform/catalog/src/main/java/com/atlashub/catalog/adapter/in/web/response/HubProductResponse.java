package com.atlashub.catalog.adapter.in.web.response;

import com.atlashub.catalog.domain.valueobject.ProductKey;
import com.atlashub.catalog.domain.valueobject.ProductStatus;

public record HubProductResponse(
        Long id,
        ProductKey key,
        String name,
        String description,
        ProductStatus status
) {}
