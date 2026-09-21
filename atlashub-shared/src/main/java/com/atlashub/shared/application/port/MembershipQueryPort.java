package com.atlashub.shared.application.port;

import java.util.Set;

public interface MembershipQueryPort {
    boolean isMemberOf(Long userId, Long orgId);
    boolean isActiveOwner(Long userId, Long orgId);
    Set<String> getPermissions(Long userId, Long orgId);  // returns permission codes for JWT
    MemberStatus getMemberStatus(Long userId, Long orgId);

    interface MemberStatus {

    }
}
