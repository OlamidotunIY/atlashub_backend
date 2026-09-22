package com.atlashub.authentication.application.command.Login;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.application.port.SessionPort;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.exceptions.AuthLocked;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.authentication.domain.valueobject.Session;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.service.HashingUtils;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Component
public class LoginHandler extends Command<LoginCommand, LoginResponse> {

    final PasswordEncoderPort encoderPort;
    final AuthAccountRepository accountRepository;
    final TrustedDeviceRepository deviceRepository;
    final OtpVerificationRepository verificationRepository;
    final OtpVerificationIssuer issuer;
    final OtpTransmissionPort transmissionPort;
    final MembershipQueryPort membershipQueryPort;
    final UserQueryPort userQueryPort;
    final SessionPort sessionPort;
    final TokenPort tokenPort;

    public LoginHandler(PasswordEncoderPort encoderPort, AuthAccountRepository accountRepository,
                        TrustedDeviceRepository deviceRepository, OtpVerificationRepository verificationRepository,
                        OtpVerificationIssuer issuer, OtpTransmissionPort transmissionPort,
                        MembershipQueryPort membershipQueryPort, UserQueryPort userQueryPort,
                        SessionPort sessionPort, TokenPort tokenPort) {
        this.encoderPort = encoderPort;
        this.accountRepository = accountRepository;
        this.deviceRepository = deviceRepository;
        this.verificationRepository = verificationRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
        this.membershipQueryPort = membershipQueryPort;
        this.userQueryPort = userQueryPort;
        this.sessionPort = sessionPort;
        this.tokenPort = tokenPort;
    }

    @Override
    public LoginResponse execute(LoginCommand input) {
        AuthAccount account = accountRepository.findByEmail(input.email()).orElseThrow(InvalidCredentials::new);
        UserQueryPort.UserDto user = userQueryPort.findById(account.getUserId()).orElseThrow(() -> new NotFoundException("User not found"));

        if (account.isLocked()) {
            throw new AuthLocked();
        }

        if (!account.getEmailVerified()) {
            throw new EmailVerificationRequired();
        }

        if (!encoderPort.matches(input.password(), account.getPasswordHash())) {
            account.recordFailedLogin();
            accountRepository.save(account);
            throw new InvalidCredentials();
        }

        account.recordSuccessfulLogin(input.ipAddress());
        accountRepository.save(account);

        Optional<TrustedDevice> existingDevice = deviceRepository.findByUserIdAndDeviceFingerprint(account.getUserId(), input.deviceFingerprint());
        TrustedDevice device;

        if (existingDevice.isEmpty()) {
            if (!(account.getLastLoginAt() == null)) {
                OtpVerificationIssuer.IssuedToken issuedToken = issuer.issue(verificationRepository.nextIdentity(), account.getId(), OtpType.DEVICE_VERIFICATION);
                transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issuedToken.rawOtp());
                verificationRepository.save(issuedToken.token());
                return LoginResponse.otpRequired("Otp for device authorization sent");
            }
            device = TrustedDevice.create(deviceRepository.nextIdentity(), account.getUserId(), input.deviceFingerprint(), input.userAgent(), input.ipAddress());
        } else {
            device = existingDevice.get();
        }

        deviceRepository.save(device);

        Long orgId = user.activeOrganizationId();
        Set<String> permissions = membershipQueryPort.getPermissions(account.getUserId(), orgId);
        String refreshToken = UUID.randomUUID().toString();
        String refreshTokenHash = HashingUtils.sha256Hex(refreshToken);
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime accessTokenExpiresAt = now.plusMinutes(30);
        ZonedDateTime refreshTokenExpiresAt = now.plusDays(24);

        TokenPort.AccessTokenResult accessToken = tokenPort.generateAccessToken(
                new TokenPort.AccessTokenPayload(user.id().toString(), orgId.toString(), permissions));

        Session session = new Session(account.getId(), refreshTokenHash, accessToken.jti(),
                accessTokenExpiresAt, refreshTokenExpiresAt, device.getId(), orgId);
        sessionPort.save(session);

        return LoginResponse.success(accessToken.token(), refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
