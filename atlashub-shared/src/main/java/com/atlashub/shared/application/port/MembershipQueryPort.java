package com.atlashub.shared.application.port;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MembershipQueryPort {
    boolean isMemberOf(Long userId, Long orgId);
    boolean isActiveOwner(Long userId, Long orgId);
    Set<String> getPermissions(Long userId, Long orgId);
    MembershipStatus getMemberStatus(Long userId, Long orgId);
    Optional<String> getActiveRoleName(Long userId, Long orgId);
    List<Long> listOrganizationIds(Long userId);

    enum MembershipStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED
    }
}
