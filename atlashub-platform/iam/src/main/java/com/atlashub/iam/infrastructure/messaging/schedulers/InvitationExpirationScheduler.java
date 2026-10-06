package com.atlashub.iam.infrastructure.messaging.schedulers;

import com.atlashub.iam.application.commands.ExpireInvitations.ExpireInvitationsCommand;
import com.atlashub.iam.application.commands.ExpireInvitations.ExpireInvitationsHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Component
public class InvitationExpirationScheduler {
    private final ExpireInvitationsHandler handler;

    public InvitationExpirationScheduler(ExpireInvitationsHandler handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "${iam.invitation-expiration-cron:0 */15 * * * *}", zone = "UTC")
    public void expireInvitations() {
        handler.execute(new ExpireInvitationsCommand(ZonedDateTime.now(ZoneOffset.UTC)));
    }
}
