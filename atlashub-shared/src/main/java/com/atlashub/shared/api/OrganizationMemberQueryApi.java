package com.atlashub.shared.api;

public interface OrganizationMemberQueryApi {
    String getOnboardingStatus(Long userId, String email);
}
