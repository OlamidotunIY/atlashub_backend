package com.atlashub.paystack.infrastructure.external.paystack.configuration;

import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PaystackPropertiesTest {
    @Test void selects_environment_credentials() {
        PaystackProperties properties=properties();
        assertEquals("test-key", properties.forEnvironment(PaystackEnvironment.TEST).secretKey());
        assertEquals("live-key", properties.forEnvironment(PaystackEnvironment.LIVE).secretKey());
    }
    @Test void rejects_non_https_url() {
        assertThrows(IllegalArgumentException.class, () -> endpoint("http://api.paystack.co", "key"));
    }
    @Test void rejects_missing_secret() {
        assertThrows(IllegalArgumentException.class, () -> endpoint("https://api.paystack.co", " "));
    }
    private PaystackProperties properties() { return new PaystackProperties(Duration.ofSeconds(5),
            endpoint("https://api.paystack.co", "test-key"), endpoint("https://api.paystack.co", "live-key")); }
    private PaystackProperties.EnvironmentProperties endpoint(String url,String key) {
        return new PaystackProperties.EnvironmentProperties(URI.create(url),"public-key",key,"webhook-secret",
                Duration.ofSeconds(2),Duration.ofSeconds(10));
    }
}
