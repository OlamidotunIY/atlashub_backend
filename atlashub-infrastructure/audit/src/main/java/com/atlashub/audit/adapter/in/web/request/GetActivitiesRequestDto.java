package com.atlashub.audit.adapter.in.web.request;

public record GetActivitiesRequestDto(
    Long organizationId,
    Integer page,
    Integer size
) {
    public GetActivitiesRequestDto {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
    }
}
