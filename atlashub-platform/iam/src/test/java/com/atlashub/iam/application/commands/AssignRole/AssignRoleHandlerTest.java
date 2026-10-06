package com.atlashub.iam.application.commands.AssignRole;

import com.atlashub.iam.domain.entities.CustomRole;
import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.mockito.Mockito.*;

class AssignRoleHandlerTest {
    @Test
    void locks_scoped_member_before_assigning_role() {
        OrganizationMemberRepository members = mock(OrganizationMemberRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        OrganizationMember member = OrganizationMember.create(1L, 2L, 3L, 4L, 5L);
        CustomRole role = CustomRole.create(6L, 2L, "Manager", "Manager", Set.of(7L), false, 3L);
        when(members.findByIdAndOrganizationIdForUpdate(1L, 2L)).thenReturn(Optional.of(member));
        when(roles.findById(6L)).thenReturn(Optional.of(role));

        new AssignRoleHandler(members, roles).execute(new AssignRoleCommand(1L, 6L, 3L, 2L));

        verify(members).findByIdAndOrganizationIdForUpdate(1L, 2L);
        verify(members).save(member);
    }
}
