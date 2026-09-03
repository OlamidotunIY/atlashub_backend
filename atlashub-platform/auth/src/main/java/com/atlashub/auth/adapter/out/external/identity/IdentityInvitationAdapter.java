package com.atlashub.auth.adapter.out.external.identity;

import com.atlashub.auth.application.port.out.InvitationQueryPort;
import com.atlashub.shared.application.api.InvitationQueryApi;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdentityInvitationAdapter implements InvitationQueryPort {

    private final InvitationQueryApi api;

    public IdentityInvitationAdapter(InvitationQueryApi api) {
        this.api = api;
    }

    @Override
    public Optional<InvitationDetails> findByToken(String token) {
        return api.findByToken(token).map(data -> new InvitationDetails(
            data.token(),
            data.organizationId(),
            data.invitedEmail(),
            data.status(),
            data.invitedEmail()
        ));
    }
}
