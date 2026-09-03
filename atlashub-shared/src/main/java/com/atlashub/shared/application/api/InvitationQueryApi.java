package com.atlashub.shared.application.api;

import java.util.Optional;

public interface InvitationQueryApi {
    Optional<InvitationData> findByToken(String token);

    record InvitationData(String token, String status, String invitedEmail, Long organizationId, String role) {}
}
