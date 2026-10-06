package com.atlashub.iam.infrastructure.messaging.schedulers;

import com.atlashub.iam.application.commands.ExpireInvitations.ExpireInvitationsCommand;
import com.atlashub.iam.application.commands.ExpireInvitations.ExpireInvitationsHandler;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InvitationExpirationSchedulerTest {
    @Test
    void delegates_to_expiration_command() {
        ExpireInvitationsHandler handler = mock(ExpireInvitationsHandler.class);
        new InvitationExpirationScheduler(handler).expireInvitations();
        verify(handler).execute(any(ExpireInvitationsCommand.class));
    }
}
