package com.atlashub.catalog.application.command;

import com.atlashub.catalog.domain.valueobject.ProductKey;

public record CreateHubProductCommand(ProductKey key, String name, String description) {
}
