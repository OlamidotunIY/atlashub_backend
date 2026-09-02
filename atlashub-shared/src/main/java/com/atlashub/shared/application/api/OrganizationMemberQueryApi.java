package com.atlashub.shared.application.api;

public interface OrganizationMemberQueryApi {
    String getOnboardingStatus(Long userId, String email);
}
