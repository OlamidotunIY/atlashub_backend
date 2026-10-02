package com.atlashub.anchor.client;

import com.atlashub.anchor.dto.common.AnchorRequest;
import com.atlashub.anchor.dto.common.AnchorResponse;
import com.atlashub.anchor.dto.deposit.CreateDepositAccountData;
import com.atlashub.anchor.dto.deposit.DepositAccountResource;
import com.atlashub.anchor.dto.deposit.FreezeDepositAccountData;
import com.atlashub.anchor.dto.deposit.UnfreezeDepositAccountData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/** Typed HTTP contract for Anchor DepositAccount resources. */
@HttpExchange(accept = MediaType.APPLICATION_JSON_VALUE, contentType = MediaType.APPLICATION_JSON_VALUE)
public interface AnchorDepositAccountClient {

    @PostExchange("/api/v1/accounts")
    AnchorResponse<DepositAccountResource> createDepositAccount(
            @RequestBody AnchorRequest<CreateDepositAccountData> request
    );

    @GetExchange("/api/v1/accounts/{accountId}")
    AnchorResponse<DepositAccountResource> getDepositAccount(@PathVariable String accountId);

    @PostExchange("/api/v1/accounts/{accountId}/freeze")
    AnchorResponse<DepositAccountResource> freezeDepositAccount(
            @PathVariable String accountId,
            @RequestBody AnchorRequest<FreezeDepositAccountData> request
    );

    @PostExchange("/api/v1/accounts/unfreeze")
    AnchorResponse<DepositAccountResource> unfreezeDepositAccount(
            @RequestBody AnchorRequest<UnfreezeDepositAccountData> request
    );
}
