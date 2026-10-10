package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.InitializeTransactionData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.InitializeTransactionRequest;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.PaystackTransactionData;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface PaystackTransactionClient {
    @PostExchange("/transaction/initialize") PaystackResponse<InitializeTransactionData> initialize(
            @RequestHeader("Idempotency-Key") String key, @RequestBody InitializeTransactionRequest request);
    @GetExchange("/transaction/verify/{reference}") PaystackResponse<PaystackTransactionData> verify(
            @PathVariable String reference);
}
