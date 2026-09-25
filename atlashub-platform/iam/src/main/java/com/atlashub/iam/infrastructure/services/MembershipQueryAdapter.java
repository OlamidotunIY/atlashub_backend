package com.atlashub.iam.infrastructure.services;

import com.atlashub.shared.application.port.MembershipQueryPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MembershipQueryAdapter implements MembershipQueryPort {
    @Override
    public boolean isMemberOf(Long userId, Long orgId) {
        return false;
    }

    @Override
    public boolean isActiveOwner(Long userId, Long orgId) {
        return false;
    }

    @Override
    public Set<String> getPermissions(Long userId, Long orgId) {
        return Set.of();
    }

    @Override
    public MemberStatus getMemberStatus(Long userId, Long orgId) {
        return null;
    }

    @Override
    public List<Long> listOrganizationIds(Long userId) {
        return List.of();
    }
}
