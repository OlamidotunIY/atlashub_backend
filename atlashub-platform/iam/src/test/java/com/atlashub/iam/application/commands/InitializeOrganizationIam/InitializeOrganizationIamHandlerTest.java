package com.atlashub.iam.application.commands.InitializeOrganizationIam;

import com.atlashub.iam.domain.entities.OrganizationMember;
import com.atlashub.iam.domain.repositories.CustomRoleRepository;
import com.atlashub.iam.domain.repositories.OrganizationMemberRepository;
import com.atlashub.iam.domain.valueobject.MemberStatus;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InitializeOrganizationIamHandlerTest {

    @Test
    void repeated_registration_event_does_not_create_duplicate_iam_state() {
        OrganizationMemberRepository members = mock(OrganizationMemberRepository.class);
        CustomRoleRepository roles = mock(CustomRoleRepository.class);
        OrganizationMember existing = new OrganizationMember(
                1L, 20L, 30L, 40L, MemberStatus.ACTIVE,
                ZonedDateTime.now(), null, ZonedDateTime.now());
        when(members.findByOrganizationIdAndUserId(20L, 30L)).thenReturn(Optional.of(existing));
        InitializeOrganizationIamHandler handler = new InitializeOrganizationIamHandler(members, roles);

        handler.execute(new InitializeOrganizationIamCommand(20L, 30L));

        verify(roles, never()).nextIdentity();
        verify(members, never()).nextIdentity();
        verify(roles, never()).save(org.mockito.ArgumentMatchers.any());
        verify(members, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
