package com.atlashub.authentication.application.command.SwitchOrganization;

import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.events.ActiveOrganizationSwitchedEvent;
import com.atlashub.authentication.domain.exceptions.InvalidTokenException;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.port.DomainEventPublisher;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Component
public class SwitchOrganizationHandler extends Command<SwitchOrganizationCommand, RefreshTokenResponse> {
    private final SessionRepository sessionRepository;
    private final MembershipQueryPort membershipQueryPort;
    private final TokenPort tokenPort;
    private final DomainEventPublisher eventPublisher;

    public SwitchOrganizationHandler(SessionRepository sessionRepository,
                                     MembershipQueryPort membershipQueryPort,
                                     TokenPort tokenPort,
                                     DomainEventPublisher eventPublisher) {
        this.sessionRepository = sessionRepository;
        this.membershipQueryPort = membershipQueryPort;
        this.tokenPort = tokenPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RefreshTokenResponse execute(SwitchOrganizationCommand input) {
        Session current = sessionRepository.findById(input.currentSessionId())
                .filter(session -> session.getUserId().equals(input.userId().toString()))
                .filter(session -> !session.isExpired())
                .orElseThrow(InvalidTokenException::new);
        if (membershipQueryPort.getMemberStatus(input.userId(), input.targetOrganizationId())
                != MembershipQueryPort.MembershipStatus.ACTIVE) {
            throw new InvalidTokenException();
        }

        Set<String> permissions = membershipQueryPort.getPermissions(
                input.userId(), input.targetOrganizationId());
        String refreshToken = UUID.randomUUID().toString();
        Long newSessionId = sessionRepository.nextIdentity();
        ZonedDateTime refreshExpiresAt = ZonedDateTime.now().plusDays(30);
        Session replacement = Session.create(newSessionId, HashingUtils.sha256Hex(refreshToken),
                input.userId().toString(), input.targetOrganizationId(), current.getEnvironment(),
                current.getDeviceFingerprint(), current.getTokenFamilyId(), refreshExpiresAt,
                current.getIpAddress(), current.getUserAgent());
        TokenPort.AccessTokenResult access = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(input.userId().toString(), newSessionId.toString(),
                        input.targetOrganizationId().toString(), current.getEnvironment(), permissions));

        sessionRepository.deleteById(current.getId());
        sessionRepository.save(replacement);
        ActiveOrganizationSwitchedEvent event = new ActiveOrganizationSwitchedEvent(
                UUID.randomUUID().toString(), input.userId(), ZonedDateTime.now(),
                CorrelationId.getOrCreate(),
                new ActiveOrganizationSwitchedEvent.Payload(input.userId(), input.targetOrganizationId()));
        eventPublisher.publish(EnvelopedDomainEvent.wrap(event));
        return new RefreshTokenResponse(access.token(), access.expiresAt(), refreshToken, refreshExpiresAt);
    }
}
