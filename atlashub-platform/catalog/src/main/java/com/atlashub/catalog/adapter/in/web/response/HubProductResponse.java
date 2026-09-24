package com.atlashub.catalog.adapter.in.web.response;

public record HubProductResponse(
        Long id,
        String key,
        String name,
        String description,
        String status
) {}
