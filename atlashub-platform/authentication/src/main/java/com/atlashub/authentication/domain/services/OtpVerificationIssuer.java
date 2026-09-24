package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.ports.OtpGenerator;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.PasswordEncoderPort;

import java.time.ZonedDateTime;

public class OtpVerificationIssuer {

    private final OtpGenerator otpGenerator;
    private final PasswordEncoderPort otpHasher;

    public OtpVerificationIssuer(OtpGenerator otpGenerator, PasswordEncoderPort otpHasher) {
        this.otpGenerator = otpGenerator;
        this.otpHasher = otpHasher;
    }

    public IssuedToken issue(Long id, String identifier, VerificationType type) {
        String rawOtp = otpGenerator.generate();
        String valueHash = otpHasher.encode(rawOtp);

        Verification verification = Verification.create(
                id, identifier, valueHash, type, ZonedDateTime.now().plusMinutes(10));

        return new IssuedToken(verification, rawOtp);
    }

    public record IssuedToken(Verification token, String rawOtp) {
    }
}
