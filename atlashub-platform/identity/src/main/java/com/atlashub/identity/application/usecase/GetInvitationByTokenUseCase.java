package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.query.GetInvitationByTokenQuery;
import com.atlashub.identity.application.result.InvitationDto;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

@Service
public class GetInvitationByTokenUseCase extends BaseUseCase<GetInvitationByTokenQuery, InvitationDto> {

    private final InvitationRepository invitationRepository;

    public GetInvitationByTokenUseCase(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public InvitationDto execute(GetInvitationByTokenQuery query) {
        Invitation invitation = invitationRepository.findByToken(query.token())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));
        return new InvitationDto(
                invitation.getId(),
                invitation.getOrganizationId(),
                invitation.getInvitedEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                invitation.getExpiresAt()
        );
    }
}
