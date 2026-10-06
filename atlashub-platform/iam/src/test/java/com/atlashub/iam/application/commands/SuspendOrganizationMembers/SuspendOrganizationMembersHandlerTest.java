package com.atlashub.iam.application.commands.SuspendOrganizationMembers;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SuspendOrganizationMembersHandlerTest {
    @Test
    void suspends_non_owner_members_and_preserves_owner_access() {
        OrganizationMemberRepository members = mock(OrganizationMemberRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        OrganizationMember owner = OrganizationMember.create(1L, 9L, 2L, 10L, null);
        OrganizationMember staff = OrganizationMember.create(3L, 9L, 4L, 11L, null);
        when(members.findAllByOrganizationIdAndStatus(9L, MemberStatus.ACTIVE)).thenReturn(List.of(owner, staff));
        when(roles.findById(10L)).thenReturn(Optional.of(
                CustomRole.create(10L, 9L, "Owner", "Owner", Set.of(), true, 2L)));
        when(roles.findById(11L)).thenReturn(Optional.of(
                CustomRole.create(11L, 9L, "Staff", "Staff", Set.of(12L), false, 2L)));

        int count = new SuspendOrganizationMembersHandler(members, roles)
                .execute(new SuspendOrganizationMembersCommand(9L, "Overdue"));

        assertEquals(1, count);
        assertEquals(MemberStatus.ACTIVE, owner.getStatus());
        assertEquals(MemberStatus.SUSPENDED, staff.getStatus());
        verify(members).save(staff);
        verify(members, never()).save(owner);
    }
}
