package com.atlashub.auth.adapter.out.external.identity;

import com.atlashub.auth.application.port.out.OrganizationMemberQueryPort;
import org.springframework.stereotype.Component;

import com.atlashub.shared.api.OrganizationMemberQueryApi;

@Component
public class IdentityOrganizationMemberAdapter implements OrganizationMemberQueryPort {

    private final OrganizationMemberQueryApi api;

    public IdentityOrganizationMemberAdapter(OrganizationMemberQueryApi api) {
        this.api = api;
    }

    @Override
    public String getOnboardingStatus(Long userId, String email) {
        return api.getOnboardingStatus(userId, email);
    }
}
