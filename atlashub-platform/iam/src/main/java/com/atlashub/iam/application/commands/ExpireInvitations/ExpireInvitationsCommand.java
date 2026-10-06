package com.atlashub.iam.application.commands.ExpireInvitations;

import java.time.ZonedDateTime;

public record ExpireInvitationsCommand(ZonedDateTime cutoff) {}
