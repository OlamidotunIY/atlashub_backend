package com.atlashub.auth.application.port;

import java.util.Optional;

public interface PreAuthTokenStorePort {
    void store(String token, Long authAccountId);
    Optional<Long> consume(String token);
}
