package com.atlashub.catalog.adapter.in.web.request;


public record CreateHubProductWebRequest(
        String key,
        String name,
        String description
) {
}
