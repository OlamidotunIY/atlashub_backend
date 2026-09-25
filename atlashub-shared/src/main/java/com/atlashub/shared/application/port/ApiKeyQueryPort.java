package com.atlashub.shared.application.port;

import java.util.Optional;

public interface ApiKeyQueryPort {
    Optional<ApiKeyDto> findByPublicKey(String publicKey);

    record ApiKeyDto() {}
}
