package com.atlashub.paystack.presentation.rest;

import com.atlashub.paystack.application.commands.ReceivePaystackWebhook.ReceivePaystackWebhookCommand;
import com.atlashub.paystack.application.commands.ReceivePaystackWebhook.ReceivePaystackWebhookHandler;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.shared.application.dto.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/paystack/{environment}")
@ConditionalOnProperty(prefix = "atlashub.integrations.paystack", name = "enabled", havingValue = "true")
public class PaystackWebhookController {
    private final ReceivePaystackWebhookHandler handler;

    public PaystackWebhookController(ReceivePaystackWebhookHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> receive(@PathVariable String environment,
                                                     @RequestHeader("x-paystack-signature") String signature,
                                                     @RequestBody byte[] body) {
        handler.execute(new ReceivePaystackWebhookCommand(parse(environment), body, signature));
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook received", null, null));
    }

    private PaystackEnvironment parse(String value) {
        if ("test".equalsIgnoreCase(value)) return PaystackEnvironment.TEST;
        if ("live".equalsIgnoreCase(value)) return PaystackEnvironment.LIVE;
        throw new IllegalArgumentException("Unsupported Paystack environment");
    }
}
