package com.atlashub.charges.adapter.in.web.controller;

import com.atlashub.charges.adapter.in.web.request.PaystackWebhookRequest;
import com.atlashub.charges.application.command.HandlePaystackWebhookCommand;
import com.atlashub.charges.application.usecase.HandlePaystackWebhookUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/paystack")
public class PaystackWebhookController {

    private final HandlePaystackWebhookUseCase useCase;

    public PaystackWebhookController(HandlePaystackWebhookUseCase useCase) {
        this.useCase = useCase;
    }

    @PostMapping
    public ResponseEntity<Void> handleWebhook(@RequestBody PaystackWebhookRequest payload) {
        if (payload.data() != null && payload.data().containsKey("reference")) {
            String reference = (String) payload.data().get("reference");
            useCase.execute(new HandlePaystackWebhookCommand(payload.event(), reference));
        }

        return ResponseEntity.ok().build();
    }
}