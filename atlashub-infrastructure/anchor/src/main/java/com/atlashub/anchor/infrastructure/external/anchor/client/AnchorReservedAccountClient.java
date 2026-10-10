package com.atlashub.anchor.infrastructure.external.anchor.client;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.infrastructure.external.anchor.dto.reservedaccount.CreateReservedAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.reservedaccount.ReservedAccountResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Typed HTTP contract for Anchor ReservedAccount resources. */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorReservedAccountClient {

    @PostExchange("/pay/reserved-account")
    AnchorResponse<ReservedAccountResource> createReservedAccount(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody AnchorRequest<CreateReservedAccountData> request
    );

    @GetExchange("/pay/reserved-account/{reservedAccountId}")
    AnchorResponse<ReservedAccountResource> getReservedAccount(@PathVariable String reservedAccountId);
}
