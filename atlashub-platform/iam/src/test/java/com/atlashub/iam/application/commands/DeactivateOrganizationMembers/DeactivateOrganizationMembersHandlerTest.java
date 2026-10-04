package com.atlashub.iam.application.commands.DeactivateOrganizationMembers;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DeactivateOrganizationMembersHandlerTest {
    @Test
    void organization_ban_deactivates_every_member_including_owner() {
        OrganizationMemberRepository members = mock(OrganizationMemberRepository.class);
        OrganizationMember owner = OrganizationMember.create(1L, 9L, 2L, 10L, null);
        OrganizationMember staff = OrganizationMember.create(3L, 9L, 4L, 11L, null);
        when(members.findAllByOrganizationId(9L)).thenReturn(List.of(owner, staff));

        int count = new DeactivateOrganizationMembersHandler(members)
                .execute(new DeactivateOrganizationMembersCommand(9L));

        assertEquals(2, count);
        assertEquals(MemberStatus.INACTIVE, owner.getStatus());
        assertEquals(MemberStatus.INACTIVE, staff.getStatus());
        verify(members).save(owner);
        verify(members).save(staff);
    }
}
