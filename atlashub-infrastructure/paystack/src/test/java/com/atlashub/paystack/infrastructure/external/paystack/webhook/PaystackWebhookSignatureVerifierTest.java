package com.atlashub.paystack.infrastructure.external.paystack.webhook;

import com.atlashub.paystack.exception.InvalidPaystackWebhookSignatureException;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.*;
import java.net.URI; import java.nio.charset.StandardCharsets; import java.time.Duration; import javax.crypto.Mac; import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;

class PaystackWebhookSignatureVerifierTest {
    private static final byte[] BODY="{\"event\":\"charge.success\"}".getBytes(StandardCharsets.UTF_8);
    @Test void accepts_valid_hmac_sha512_signature() throws Exception { new PaystackWebhookSignatureVerifier(properties()).verify(BODY,signature(),PaystackEnvironment.TEST); }
    @Test void rejects_invalid_or_malformed_signature() { PaystackWebhookSignatureVerifier verifier=new PaystackWebhookSignatureVerifier(properties()); assertThrows(InvalidPaystackWebhookSignatureException.class,()->verifier.verify(BODY,"xyz",PaystackEnvironment.TEST)); }
    private String signature() throws Exception {Mac mac=Mac.getInstance("HmacSHA512");mac.init(new SecretKeySpec("webhook-secret".getBytes(StandardCharsets.UTF_8),"HmacSHA512"));return java.util.HexFormat.of().formatHex(mac.doFinal(BODY));}
    private PaystackProperties properties(){var endpoint=new PaystackProperties.EnvironmentProperties(URI.create("https://api.paystack.co"),"public-key","key","webhook-secret",Duration.ofSeconds(2),Duration.ofSeconds(10));return new PaystackProperties(Duration.ofSeconds(5),endpoint,endpoint);}
}
