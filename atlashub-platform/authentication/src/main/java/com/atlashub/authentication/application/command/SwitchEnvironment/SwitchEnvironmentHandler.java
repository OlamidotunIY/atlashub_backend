package com.atlashub.authentication.application.command.SwitchEnvironment;

import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.exceptions.InvalidTokenException;
import com.atlashub.authentication.domain.exceptions.LiveEnvironmentUnavailableException;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Component
public class SwitchEnvironmentHandler extends Command<SwitchEnvironmentCommand, RefreshTokenResponse> {
    private final SessionRepository sessionRepository;
    private final MembershipQueryPort membershipQueryPort;
    private final ComplianceQueryPort complianceQueryPort;
    private final TokenPort tokenPort;

    public SwitchEnvironmentHandler(
            SessionRepository sessionRepository,
            MembershipQueryPort membershipQueryPort,
            ComplianceQueryPort complianceQueryPort,
            TokenPort tokenPort
    ) {
        this.sessionRepository = sessionRepository;
        this.membershipQueryPort = membershipQueryPort;
        this.complianceQueryPort = complianceQueryPort;
        this.tokenPort = tokenPort;
    }

    @Override
    @Transactional
    public RefreshTokenResponse execute(SwitchEnvironmentCommand command) {
        Session current = sessionRepository.findById(command.currentSessionId())
                .filter(session -> session.getUserId().equals(command.userId().toString()))
                .filter(session -> !session.isExpired())
                .orElseThrow(InvalidTokenException::new);
        ApiEnvironment target = ApiEnvironment.parse(command.targetEnvironment());
        if (target == ApiEnvironment.LIVE
                && !complianceQueryPort.isApproved(current.getOrganizationId())) {
            throw new LiveEnvironmentUnavailableException();
        }
        if (membershipQueryPort.getMemberStatus(command.userId(), current.getOrganizationId())
                != MembershipQueryPort.MembershipStatus.ACTIVE) {
            throw new InvalidTokenException();
        }

        Set<String> permissions = membershipQueryPort.getPermissions(
                command.userId(), current.getOrganizationId());
        String refreshToken = UUID.randomUUID().toString();
        Long replacementId = sessionRepository.nextIdentity();
        ZonedDateTime refreshExpiresAt = ZonedDateTime.now().plusDays(30);
        Session replacement = Session.create(
                replacementId, HashingUtils.sha256Hex(refreshToken), current.getUserId(),
                current.getOrganizationId(), target.name(), current.getDeviceFingerprint(),
                current.getTokenFamilyId(), refreshExpiresAt, current.getIpAddress(),
                current.getUserAgent());
        TokenPort.AccessTokenResult access = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(
                        current.getUserId(), replacementId.toString(),
                        current.getOrganizationId().toString(), target.name(), permissions));

        sessionRepository.deleteById(current.getId());
        sessionRepository.save(replacement);
        return new RefreshTokenResponse(
                access.token(), access.expiresAt(), refreshToken, refreshExpiresAt);
    }
}
