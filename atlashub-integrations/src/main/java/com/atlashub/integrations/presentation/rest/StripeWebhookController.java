package com.atlashub.integrations.presentation.rest;

import com.atlashub.integrations.application.command.ReceiveStripeWebhook.ReceiveStripeWebhookCommand;
import com.atlashub.integrations.application.command.ReceiveStripeWebhook.ReceiveStripeWebhookHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
public class StripeWebhookController {
    private final ReceiveStripeWebhookHandler handler;

    public StripeWebhookController(ReceiveStripeWebhookHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestHeader("Stripe-Signature") String signature, @RequestBody byte[] body) {
        handler.execute(new ReceiveStripeWebhookCommand(body, signature));
        return ResponseEntity.ok().build();
    }
}
