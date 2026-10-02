package com.atlashub.anchor.presentation.rest;

import com.atlashub.anchor.application.commands.ReceiveAnchorWebhook.ReceiveAnchorWebhookCommand;
import com.atlashub.anchor.application.commands.ReceiveAnchorWebhook.ReceiveAnchorWebhookHandler;
import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorWebhookConsumer;
import com.atlashub.shared.application.dto.ApiResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

/** Public provider callback endpoint. It receives raw bytes because Anchor signs the raw body. */
@RestController
@RequestMapping("/api/v1/webhooks/anchor/{environment}/{consumer}")
@ConditionalOnProperty(prefix = "atlashub.integrations.anchor", name = "enabled", havingValue = "true")
public class AnchorWebhookController {
    private final ReceiveAnchorWebhookHandler handler;

    public AnchorWebhookController(ReceiveAnchorWebhookHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> receive(
            @PathVariable String environment,
            @PathVariable String consumer,
            @RequestBody byte[] rawBody,
            @RequestHeader("x-anchor-signature") String signature
    ) {
        handler.execute(new ReceiveAnchorWebhookCommand(
                AnchorEnvironment.fromCallbackPath(environment),
                AnchorWebhookConsumer.fromCallbackPath(consumer),
                rawBody,
                signature
        ));
        return done("Webhook received");
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
