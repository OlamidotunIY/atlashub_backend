package com.atlashub.anchor.infrastructure.external.anchor.client;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.infrastructure.external.anchor.dto.webhook.AnchorWebhookResource;
import com.atlashub.anchor.infrastructure.external.anchor.dto.webhook.CreateAnchorWebhookData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;

/** Typed HTTP contract for configuring Anchor's outbound webhook subscriptions. */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorWebhookClient {

    @PostExchange("/api/v1/webhooks")
    AnchorResponse<AnchorWebhookResource> createWebhook(
            @RequestBody AnchorRequest<CreateAnchorWebhookData> request
    );

    @GetExchange("/api/v1/webhooks")
    AnchorResponse<List<AnchorWebhookResource>> listWebhooks();

    @GetExchange("/api/v1/webhooks/{webhookId}")
    AnchorResponse<AnchorWebhookResource> getWebhook(@PathVariable String webhookId);

    @DeleteExchange("/api/v1/webhooks/{webhookId}")
    void deleteWebhook(@PathVariable String webhookId);
}
