package com.atlashub.identity.adapter.out.persistence.query;

import com.atlashub.identity.adapter.out.persistence.repository.SpringDataInvitationRepository;
import com.atlashub.identity.adapter.out.persistence.repository.SpringDataUserRepository;
import com.atlashub.identity.application.port.InvitationQueryService;
import com.atlashub.identity.application.result.InvitationDto;
import com.atlashub.identity.domain.valueobject.InvitationStatus;
import com.atlashub.identity.domain.valueobject.OrganizationRole;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InvitationQueryServiceImpl implements InvitationQueryService {

    private final SpringDataInvitationRepository repository;
    private final SpringDataUserRepository userRepository;

    @Override
    public Optional<InvitationDto> getInvitationByToken(String token) {
        return repository.findByToken(token)
                .map(invitation -> new InvitationDto(
                        invitation.getId(),
                        invitation.getOrganizationId(),
                        invitation.getInvitedEmail(),
                        OrganizationRole.valueOf(invitation.getRole()),
                        InvitationStatus.valueOf(invitation.getStatus()),
                        userRepository.findByEmail(invitation.getInvitedEmail()).isPresent(),
                        invitation.getExpiresAt()
                ));
    }
}
