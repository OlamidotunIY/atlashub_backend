package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount.PaystackSubaccountData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount.PaystackSubaccountRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange
public interface PaystackSubaccountClient {
    @PostExchange("/subaccount") PaystackResponse<PaystackSubaccountData> create(
            @RequestHeader("Idempotency-Key") String key, @RequestBody PaystackSubaccountRequest request);
    @PutExchange("/subaccount/{code}") PaystackResponse<PaystackSubaccountData> update(
            @PathVariable String code, @RequestHeader("Idempotency-Key") String key,
            @RequestBody PaystackSubaccountRequest request);
}
