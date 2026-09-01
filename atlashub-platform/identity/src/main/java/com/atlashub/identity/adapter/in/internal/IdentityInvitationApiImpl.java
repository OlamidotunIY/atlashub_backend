package com.atlashub.identity.adapter.in.internal;

import com.atlashub.identity.application.query.GetInvitationByTokenQuery;
import com.atlashub.identity.application.result.InvitationDto;
import com.atlashub.identity.application.usecase.GetInvitationByTokenUseCase;
import com.atlashub.shared.api.InvitationQueryApi;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IdentityInvitationApiImpl implements InvitationQueryApi {

    private final GetInvitationByTokenUseCase useCase;

    public IdentityInvitationApiImpl(GetInvitationByTokenUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public Optional<InvitationData> findByToken(String token) {
        try {
            InvitationDto dto = useCase.execute(new GetInvitationByTokenQuery(token));
            return Optional.of(new InvitationData(
                token,
                dto.status().name(),
                dto.invitedEmail(),
                dto.organizationId(),
                dto.role().name()
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
