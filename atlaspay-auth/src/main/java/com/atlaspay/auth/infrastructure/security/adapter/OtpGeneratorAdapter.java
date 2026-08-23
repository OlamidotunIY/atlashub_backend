package com.atlaspay.auth.infrastructure.security.adapter;

import com.atlaspay.auth.application.port.out.OtpGeneratorPort;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class OtpGeneratorAdapter implements OtpGeneratorPort {
    private final SecureRandom random = new SecureRandom();

    @Override
    public String generateOtp() {
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}
