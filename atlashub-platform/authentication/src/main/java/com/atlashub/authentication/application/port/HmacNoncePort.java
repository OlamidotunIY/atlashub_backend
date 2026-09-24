package com.atlashub.authentication.application.port;

import java.time.Duration;

public interface HmacNoncePort {
    boolean checkAndStoreNonce(String nonce, Duration timeToLive);
}
