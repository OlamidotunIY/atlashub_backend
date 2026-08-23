package com.atlaspay.accounts.presentation.webhook.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnchorWebhookPayloadDto {
    private String type;
    private AnchorWebhookDataDto data;
}
