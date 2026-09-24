package com.atlashub.shared.application.port;

import java.util.List;
import java.util.Set;

public interface MembershipQueryPort {
    boolean isMemberOf(Long userId, Long orgId);
    boolean isActiveOwner(Long userId, Long orgId);
    Set<String> getPermissions(Long userId, Long orgId);
    MemberStatus getMemberStatus(Long userId, Long orgId);
    List<Long> listOrganizationIds(Long userId);

    interface MemberStatus {
    }
}
