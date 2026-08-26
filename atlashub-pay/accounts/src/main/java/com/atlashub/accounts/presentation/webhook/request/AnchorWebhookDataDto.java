package com.atlashub.accounts.presentation.webhook.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AnchorWebhookDataDto {
    private AnchorWebhookAttributesDto attributes;
}
