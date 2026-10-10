package com.atlashub.anchor.infrastructure.external.anchor.client;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.infrastructure.external.anchor.dto.subaccount.CreateSubAccountData;
import com.atlashub.anchor.infrastructure.external.anchor.dto.subaccount.SubAccountResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Typed HTTP contract for Anchor SubAccount resources. */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorSubAccountClient {

    @PostExchange("/api/v1/sub-accounts")
    AnchorResponse<SubAccountResource> createSubAccount(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody AnchorRequest<CreateSubAccountData> request
    );

    @GetExchange("/api/v1/sub-accounts/{subAccountId}")
    AnchorResponse<SubAccountResource> getSubAccount(@PathVariable String subAccountId);
}
