package com.atlashub.iam.infrastructure.persistence.adapters;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.repositories.PermissionRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MembershipQueryPortAdapterTest {

    @Test
    void returns_the_active_members_role_name() {
        OrganizationMemberRepository members = mock(OrganizationMemberRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        PermissionRepository permissions = mock(PermissionRepository.class);
        ZonedDateTime now = ZonedDateTime.now();
        OrganizationMember member = new OrganizationMember(1L, 2L, 3L, 4L, MemberStatus.ACTIVE, now, null, now);
        CustomRole role = new CustomRole(4L, 2L, "Owner", "Built-in owner role", Set.of(), true, 3L, now, now);
        when(members.findByOrganizationIdAndUserId(2L, 3L)).thenReturn(Optional.of(member));
        when(roles.findById(4L)).thenReturn(Optional.of(role));

        var adapter = new MembershipQueryPortAdapter(members, roles, permissions);

        assertEquals(Optional.of("Owner"), adapter.getActiveRoleName(3L, 2L));
    }
}
