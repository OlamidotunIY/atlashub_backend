package com.atlashub.iam.application.commands.ExpireInvitations;

import com.atlashub.iam.domain.entities.Invitation;
import com.atlashub.iam.domain.repositories.InvitationRepository;
import com.atlashub.iam.domain.valueobject.InvitationStatus;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ExpireInvitationsHandlerTest {
    @Test
    void expires_and_saves_pending_invitations_past_cutoff() {
        InvitationRepository repository = mock(InvitationRepository.class);
        ZonedDateTime now = ZonedDateTime.now();
        Invitation invitation = new Invitation(1L, 2L, new EmailAddress("member@example.com"), 3L, 4L,
                "hash", InvitationStatus.PENDING, now.minusMinutes(1), now.minusDays(8), now.minusDays(8));
        when(repository.findPendingExpiredBefore(now)).thenReturn(List.of(invitation));

        int count = new ExpireInvitationsHandler(repository).execute(new ExpireInvitationsCommand(now));

        assertEquals(1, count);
        assertEquals(InvitationStatus.EXPIRED, invitation.getStatus());
        verify(repository).save(invitation);
    }
}
