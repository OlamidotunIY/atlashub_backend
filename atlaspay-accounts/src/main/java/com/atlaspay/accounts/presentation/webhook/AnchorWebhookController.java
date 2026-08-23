package com.atlaspay.accounts.presentation.webhook;

import com.atlaspay.accounts.application.command.ActivateVirtualAccountCommand;
import com.atlaspay.accounts.application.usecase.ActivateVirtualAccountUseCase;
import com.atlaspay.accounts.presentation.webhook.request.AnchorWebhookPayloadDto;
import com.atlaspay.shared.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts/webhooks/anchor")
public class AnchorWebhookController {

    private static final Logger log = LoggerFactory.getLogger(AnchorWebhookController.class);
    private final ActivateVirtualAccountUseCase activateUseCase;

    public AnchorWebhookController(ActivateVirtualAccountUseCase activateUseCase) {
        this.activateUseCase = activateUseCase;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> handleAnchorWebhook(@RequestBody AnchorWebhookPayloadDto payload) {
        log.info("Received Anchor Webhook Type: {}", payload.getType());

        try {
            if ("PayWithTransfer".equals(payload.getType())) {
                if (payload.getData() != null && payload.getData().getAttributes() != null) {
                    String referenceId = payload.getData().getAttributes().getReference();
                    String nuban = payload.getData().getAttributes().getAccountNumber();
                    
                    if (referenceId != null && nuban != null) {
                        activateUseCase.execute(new ActivateVirtualAccountCommand(referenceId, nuban));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to process Anchor webhook: {}", e.getMessage(), e);
        }

        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook processed", null, null));
    }
}
