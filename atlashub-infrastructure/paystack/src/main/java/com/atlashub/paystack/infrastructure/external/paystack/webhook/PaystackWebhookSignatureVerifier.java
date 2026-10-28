package com.atlashub.paystack.infrastructure.external.paystack.webhook;

import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackProperties;
import com.atlashub.paystack.exception.InvalidPaystackWebhookSignatureException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class PaystackWebhookSignatureVerifier {
    private final PaystackProperties properties;

    public PaystackWebhookSignatureVerifier(PaystackProperties properties) {
        this.properties = properties;
    }

    @Autowired
    public PaystackWebhookSignatureVerifier(ObjectProvider<PaystackProperties> properties) {
        this.properties = properties.getIfAvailable();
    }

    public void verify(byte[] body, String signature, PaystackEnvironment environment) {
        if (properties == null) throw new IllegalStateException("Legacy Paystack webhook configuration is disabled");
        if (body == null || signature == null || environment == null ||
                !MessageDigest.isEqual(sign(body, properties.forEnvironment(environment).webhookSecret()),
                        hex(signature))) throw new InvalidPaystackWebhookSignatureException();
    }

    public void verify(byte[] body, String signature, String merchantSecret) {
        if (body == null || signature == null || merchantSecret == null || merchantSecret.isBlank()
                || !MessageDigest.isEqual(sign(body, merchantSecret), hex(signature)))
            throw new InvalidPaystackWebhookSignatureException();
    }

    private byte[] sign(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            return mac.doFinal(body);
        } catch (Exception ex) {
            throw new IllegalStateException("Could not verify Paystack webhook", ex);
        }
    }

    private byte[] hex(String value) {
        try {
            if ((value.length() & 1) != 0) return new byte[0];
            byte[] result = new byte[value.length() / 2];
            for (int i = 0; i < result.length; i++)
                result[i] = (byte) Integer.parseInt(value.substring(i * 2, i * 2 + 2), 16);
            return result;
        } catch (NumberFormatException ignored) {
            return new byte[0];
        }
    }
}
