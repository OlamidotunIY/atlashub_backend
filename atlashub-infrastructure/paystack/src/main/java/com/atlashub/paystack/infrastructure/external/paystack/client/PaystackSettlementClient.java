package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.settlement.PaystackSettlementData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.PaystackTransactionData;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface PaystackSettlementClient {
    @GetExchange("/settlement") PaystackResponse<List<PaystackSettlementData>> list(
            @RequestParam String subaccount, @RequestParam(required=false) Integer page,
            @RequestParam(name="perPage", required=false) Integer perPage);
    @GetExchange("/settlement/{settlementId}/transactions") PaystackResponse<List<PaystackTransactionData>> transactions(
            @PathVariable String settlementId, @RequestParam(required=false) Integer page,
            @RequestParam(name="perPage", required=false) Integer perPage);
}
