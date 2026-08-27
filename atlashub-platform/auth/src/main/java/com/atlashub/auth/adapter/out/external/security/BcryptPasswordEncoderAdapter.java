package com.atlashub.auth.adapter.out.external.security;

import com.atlashub.auth.application.port.in.PasswordEncoderPort;
import com.atlashub.auth.domain.service.VerificationCodeHasher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class BcryptPasswordEncoderAdapter implements PasswordEncoderPort, VerificationCodeHasher {
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public String encode(String raw) {
        return passwordEncoder.encode(raw);
    }

    @Override
    public boolean matches(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}
