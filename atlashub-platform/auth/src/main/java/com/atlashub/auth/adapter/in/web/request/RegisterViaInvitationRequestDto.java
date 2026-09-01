package com.atlashub.auth.adapter.in.web.request;

public record RegisterViaInvitationRequestDto(
    String firstName,
    String lastName,
    String password
) {}
