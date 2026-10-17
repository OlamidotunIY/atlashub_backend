package com.atlashub.analytics.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class ProjectionNotFoundException extends NotFoundException {
    public ProjectionNotFoundException(String projectionName, String key) {
        super(String.format("Projection %s not found for key: %s", projectionName, key));
    }
}
