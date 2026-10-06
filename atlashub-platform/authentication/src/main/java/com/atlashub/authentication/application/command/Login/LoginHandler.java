package com.atlashub.authentication.application.command.Login;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.exceptions.LiveEnvironmentUnavailableException;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.ComplianceQueryPort;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class LoginHandler extends Command<LoginCommand, LoginResponse> {

    final PasswordEncoderPort encoderPort;
    final AuthAccountRepository accountRepository;
    final TrustedDeviceRepository deviceRepository;
    final VerificationRepository verificationRepository;
    final OtpVerificationIssuer issuer;
    final OtpTransmissionPort transmissionPort;
    final MembershipQueryPort membershipQueryPort;
    final UserQueryPort userQueryPort;
    final SessionRepository sessionRepository;
    final TokenPort tokenPort;
    final ComplianceQueryPort complianceQueryPort;

    public LoginHandler(PasswordEncoderPort encoderPort, AuthAccountRepository accountRepository,
                        TrustedDeviceRepository deviceRepository, VerificationRepository verificationRepository,
                        OtpVerificationIssuer issuer, OtpTransmissionPort transmissionPort,
                        MembershipQueryPort membershipQueryPort, UserQueryPort userQueryPort,
                        SessionRepository sessionRepository, TokenPort tokenPort,
                        ComplianceQueryPort complianceQueryPort) {
        this.encoderPort = encoderPort;
        this.accountRepository = accountRepository;
        this.deviceRepository = deviceRepository;
        this.verificationRepository = verificationRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
        this.membershipQueryPort = membershipQueryPort;
        this.userQueryPort = userQueryPort;
        this.sessionRepository = sessionRepository;
        this.tokenPort = tokenPort;
        this.complianceQueryPort = complianceQueryPort;
    }

    @Override
    @Transactional
    public LoginResponse execute(LoginCommand input) {
        String environment;
        try {
            environment = ApiEnvironment.parse(input.environment()).name();
        } catch (IllegalArgumentException exception) {
            throw new InvalidCredentials();
        }

        AuthAccount account = accountRepository.findByAccountId(input.email()).orElseThrow(InvalidCredentials::new);

        UserQueryPort.UserDto user = userQueryPort.findById(account.getUserId()).orElseThrow(() -> new NotFoundException("User not found"));

        if (account.isLocked()) {
            throw new AuthLocked();
        }

        if (!user.emailVerified()) {
            throw new EmailVerificationRequired();
        }

        if (!encoderPort.matches(input.password(), account.getPassword())) {
            account.recordFailedLogin();
            accountRepository.save(account);
            throw new InvalidCredentials();
        }

        Long orgId = user.activeOrganizationId();
        if (orgId == null || membershipQueryPort.getMemberStatus(account.getUserId(), orgId)
                != MembershipQueryPort.MembershipStatus.ACTIVE) {
            throw new InvalidCredentials();
        }
        if (ApiEnvironment.LIVE.name().equals(environment) && !complianceQueryPort.isApproved(orgId)) {
            throw new LiveEnvironmentUnavailableException();
        }
        Set<String> permissions = membershipQueryPort.getPermissions(account.getUserId(), orgId);

        boolean firstLogin = account.getLastLoginAt() == null;

        Optional<TrustedDevice> existingDevice = deviceRepository.findByUserIdAndDeviceFingerprint(account.getUserId(), input.deviceFingerprint());
        TrustedDevice device;

        if (existingDevice.isEmpty() || existingDevice.get().getExpiresAt().isBefore(ZonedDateTime.now())) {
            if (!firstLogin) {
                OtpVerificationIssuer.IssuedToken issuedToken = issuer.issue(verificationRepository.nextIdentity(), account.getAccountId(), VerificationType.email_otp);
                transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issuedToken.rawOtp());
                verificationRepository.save(issuedToken.token());
                return LoginResponse.otpRequired("Otp for device authorization sent");
            }
            device = TrustedDevice.create(deviceRepository.nextIdentity(), account.getUserId(), input.deviceFingerprint(), input.userAgent(), input.ipAddress());
        } else {
            device = existingDevice.get();
        }

        deviceRepository.save(device);

        account.recordSuccessfulLogin(input.ipAddress());
        accountRepository.save(account);

        String rawToken = UUID.randomUUID().toString();
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime sessionExpiresAt = now.plusDays(30);
        Long sessionId = sessionRepository.nextIdentity();

        TokenPort.AccessTokenResult accessToken = tokenPort.generateAccessToken(new TokenPort.AccessTokenPayload(user.id().toString(), sessionId.toString(), orgId.toString(), environment, permissions));

        Session session = Session.create(sessionId, HashingUtils.sha256Hex(rawToken), account.getUserId().toString(), orgId, environment, input.deviceFingerprint(), UUID.randomUUID().toString(), sessionExpiresAt, input.ipAddress(), input.userAgent());
        sessionRepository.save(session);

        return LoginResponse.success(accessToken.token(), rawToken, accessToken.expiresAt(), sessionExpiresAt);
    }
}
