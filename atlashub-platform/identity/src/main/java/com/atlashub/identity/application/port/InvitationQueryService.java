package com.atlashub.identity.application.port;

import com.atlashub.identity.application.result.InvitationDto;

import java.util.Optional;

public interface InvitationQueryService {
    Optional<InvitationDto> getInvitationByToken(String token);
}
