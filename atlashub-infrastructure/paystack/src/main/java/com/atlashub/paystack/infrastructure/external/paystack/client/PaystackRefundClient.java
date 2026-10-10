package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.refund.CreateRefundRequest;
import com.atlashub.paystack.infrastructure.external.paystack.dto.refund.PaystackRefundData;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface PaystackRefundClient {
    @PostExchange("/refund")
    PaystackResponse<PaystackRefundData> create(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestBody CreateRefundRequest request
    );
}
