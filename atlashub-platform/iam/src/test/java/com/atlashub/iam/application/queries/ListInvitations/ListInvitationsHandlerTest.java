package com.atlashub.iam.application.queries.ListInvitations;

import com.atlashub.iam.domain.repositories.InvitationRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.*;

class ListInvitationsHandlerTest {
    @Test
    void lists_all_organization_invitations_when_status_is_omitted() {
        InvitationRepository repository = mock(InvitationRepository.class);
        when(repository.findByOrganizationId(9L)).thenReturn(List.of());

        new ListInvitationsHandler(repository).execute(new ListInvitationsQuery(9L, null));

        verify(repository).findByOrganizationId(9L);
        verify(repository, never()).findByOrganizationIdAndStatus(anyLong(), any());
    }
}
