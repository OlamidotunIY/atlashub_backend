package com.atlashub.identity.adapter.in.web.request;

public record ListUsersRequestDto(
    Integer page,
    Integer size
) {
    public ListUsersRequestDto {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 20;
    }
}
