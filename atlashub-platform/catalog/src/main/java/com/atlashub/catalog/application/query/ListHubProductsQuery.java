package com.atlashub.catalog.application.query;

import com.atlashub.catalog.domain.valueobject.ProductStatus;

public record ListHubProductsQuery(ProductStatus status) {
}
