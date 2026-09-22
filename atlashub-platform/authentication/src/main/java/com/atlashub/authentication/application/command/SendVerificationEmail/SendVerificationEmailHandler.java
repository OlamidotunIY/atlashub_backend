package com.atlashub.authentication.application.command.SendVerificationEmail;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;

@Component
public class SendVerificationEmailHandler extends Command<SendVerificationEmailCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final OtpVerificationRepository otpRepository;
    private final OtpVerificationIssuer issuer;
    private final OtpTransmissionPort transmissionPort;

    public SendVerificationEmailHandler(AuthAccountRepository accountRepository,
                                        OtpVerificationRepository otpRepository,
                                        OtpVerificationIssuer issuer,
                                        OtpTransmissionPort transmissionPort) {
        this.accountRepository = accountRepository;
        this.otpRepository = otpRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
    }

    @Override
    public Void execute(SendVerificationEmailCommand command) {
        AuthAccount account = accountRepository.findByEmail(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        OtpVerificationIssuer.IssuedToken issued = issuer.issue(
                otpRepository.nextIdentity(), account.getId(), OtpType.EMAIL_VERIFICATION);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issued.rawOtp());
        otpRepository.save(issued.token());

        return null;
    }
}
