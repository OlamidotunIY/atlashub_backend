package com.atlashub.anchor.application.commands.ReceiveAnchorWebhook;

import com.atlashub.anchor.configuration.AnchorEnvironment;
import com.atlashub.anchor.configuration.AnchorWebhookConsumer;

import java.util.Arrays;

/** Raw webhook data is retained only long enough for signature verification and parsing. */
public record ReceiveAnchorWebhookCommand(
        AnchorEnvironment environment,
        AnchorWebhookConsumer consumer,
        byte[] rawBody,
        String signature
) {
    public ReceiveAnchorWebhookCommand {
        rawBody = rawBody == null ? null : Arrays.copyOf(rawBody, rawBody.length);
    }

    @Override
    public byte[] rawBody() {
        return rawBody == null ? null : Arrays.copyOf(rawBody, rawBody.length);
    }
}
