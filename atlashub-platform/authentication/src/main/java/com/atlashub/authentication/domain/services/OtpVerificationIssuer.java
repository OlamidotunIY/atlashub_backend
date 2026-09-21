package com.atlashub.authentication.domain.services;

import com.atlashub.authentication.domain.entities.OtpVerification;
import com.atlashub.authentication.domain.ports.OtpGenerator;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.application.port.PasswordEncoderPort;

import java.time.ZonedDateTime;

public class OtpVerificationIssuer {
    private final OtpGenerator otpGenerator;
    private final PasswordEncoderPort otpHasher;

    public OtpVerificationIssuer(OtpGenerator otpGenerator, PasswordEncoderPort otpHasher) {
        this.otpGenerator = otpGenerator;
        this.otpHasher = otpHasher;
    }

    public IssuedToken issue(Long id, Long authAccountId, OtpType type) {
        String rawOtp = otpGenerator.generate();
        String codeHash = otpHasher.encode(rawOtp);

        OtpVerification otpVerification = OtpVerification.create(id, authAccountId, codeHash, type, ZonedDateTime.now().plusMinutes(10));

        return new IssuedToken(otpVerification, rawOtp);
    }

    public record IssuedToken(OtpVerification token, String rawOtp) {
    }
}
