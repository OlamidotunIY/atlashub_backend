package com.atlashub.auth.application.port.out;

public interface OrganizationMemberQueryPort {
    String getOnboardingStatus(Long userId, String email);
}
