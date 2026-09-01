package com.atlashub.auth.application.port.out;

import java.util.Optional;

public interface InvitationQueryPort {
    Optional<InvitationDetails> findByToken(String token);

    record InvitationDetails(
        String token,
        Long organizationId,
        String invitedEmail,
        String role,
        String status
    ) {}
}
