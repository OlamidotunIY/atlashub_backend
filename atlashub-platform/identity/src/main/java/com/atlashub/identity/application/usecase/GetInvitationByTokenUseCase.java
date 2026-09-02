package com.atlashub.identity.application.usecase;

import com.atlashub.identity.application.query.GetInvitationByTokenQuery;
import com.atlashub.identity.application.result.InvitationDto;
import com.atlashub.identity.domain.exception.IdentityErrorCode;
import com.atlashub.identity.domain.model.Invitation;
import com.atlashub.identity.domain.model.User;
import com.atlashub.identity.domain.repository.InvitationRepository;
import com.atlashub.identity.domain.repository.UserRepository;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetInvitationByTokenUseCase extends BaseUseCase<GetInvitationByTokenQuery, InvitationDto> {

    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;

    public GetInvitationByTokenUseCase(InvitationRepository invitationRepository, UserRepository userRepository) {
        this.invitationRepository = invitationRepository;
        this.userRepository = userRepository;
    }

    @Override
    public InvitationDto execute(GetInvitationByTokenQuery query) {
        Invitation invitation = invitationRepository.findByToken(query.token())
                .orElseThrow(() -> new NotFoundException(IdentityErrorCode.INVITATION_NOT_FOUND, "Invitation not found"));

        Optional<User> user = userRepository.findByEmail(invitation.getInvitedEmail());

        return new InvitationDto(
                invitation.getId(),
                invitation.getOrganizationId(),
                invitation.getInvitedEmail(),
                invitation.getRole(),
                invitation.getStatus(),
                user.isPresent(),
                invitation.getExpiresAt()
        );
    }
}
