package com.atlashub.authentication.application.command.ResetPassword;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.OtpVerification;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.exceptions.VerifyTokenError;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.valueobject.OtpStatus;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class ResetPasswordHandler extends Command<ResetPasswordCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final OtpVerificationRepository otpRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public ResetPasswordHandler(AuthAccountRepository accountRepository,
                                OtpVerificationRepository otpRepository,
                                PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Void execute(ResetPasswordCommand command) {
        AuthAccount account = accountRepository.findByEmail(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        OtpVerification otp = otpRepository
                .findByAuthAccountIdAndTypeAndStatus(account.getId(), OtpType.PASSWORD_RESET, OtpStatus.PENDING)
                .orElseThrow(VerifyTokenError::new);

        if (ZonedDateTime.now().isAfter(otp.getExpiresAt())) {
            throw new VerifyTokenError();
        }

        if (!passwordEncoderPort.matches(command.rawOtp(), otp.getCodeHash())) {
            throw new InvalidCredentials();
        }

        otp.verifyToken();
        otpRepository.save(otp);

        String newHash = passwordEncoderPort.encode(command.newPassword());
        account.resetPassword(newHash);
        accountRepository.save(account);

        return null;
    }
}
