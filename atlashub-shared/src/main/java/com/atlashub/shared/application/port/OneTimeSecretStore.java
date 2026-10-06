package com.atlashub.shared.application.port;

import java.time.Duration;
import java.util.Optional;

/**
 * Claim-check storage for secrets that must cross an asynchronous boundary
 * without being written to an event payload.
 */
public interface OneTimeSecretStore {
    String store(String secret, Duration timeToLive);

    Optional<String> claim(String reference);
}
