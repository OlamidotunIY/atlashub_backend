package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.CreateVerificationCommand;
import com.atlashub.auth.application.port.in.OtpGeneratorPort;
import com.atlashub.auth.application.port.in.PasswordEncoderPort;
import com.atlashub.auth.domain.model.Verification;
import com.atlashub.auth.domain.repository.VerificationRepository;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Service
public class CreateVerificationUseCase extends BaseUseCase<CreateVerificationCommand, ApiResponse<Void>> {

    private final VerificationRepository verificationRepository;
    private final OtpGeneratorPort otpGeneratorPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;
    
    @Value("${atlashub.auth.verification.expires-in-minutes:10}")
    private int expiresInMinutes;

    @Value("${atlashub.auth.verification.max-attempts:3}")
    private int maxAttempts;

    public CreateVerificationUseCase(
            VerificationRepository verificationRepository,
            OtpGeneratorPort otpGeneratorPort,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.verificationRepository = verificationRepository;
        this.otpGeneratorPort = otpGeneratorPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(CreateVerificationCommand input) {
        verificationRepository.invalidatePreviousVerifications(input.type(), input.identifier());

        String rawCode = otpGeneratorPort.generateOtp();
        String hashedCode = passwordEncoderPort.encode(rawCode);

        Verification verification = Verification.create(
                verificationRepository.nextIdentity(),
                input.authAccountId(),
                input.identifier(),
                input.identifier(),
                hashedCode,
                rawCode,
                input.type(),
                ZonedDateTime.now().plusMinutes(expiresInMinutes),
                maxAttempts
        );

        verificationRepository.save(verification);
        publishEvents(verification, eventPublisher);

        return new ApiResponse<>(true, "Verification created successfully", null, null);
    }
}
