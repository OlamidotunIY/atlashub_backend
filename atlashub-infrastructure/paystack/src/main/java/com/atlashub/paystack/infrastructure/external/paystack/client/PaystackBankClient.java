package com.atlashub.paystack.infrastructure.external.paystack.client;

import com.atlashub.paystack.infrastructure.external.paystack.dto.bank.PaystackBankData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.bank.ResolvedAccountData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import java.util.List;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface PaystackBankClient {
    @GetExchange("/bank") PaystackResponse<List<PaystackBankData>> list(@RequestParam String country);
    @GetExchange("/bank/resolve") PaystackResponse<ResolvedAccountData> resolve(
            @RequestParam("account_number") String accountNumber, @RequestParam("bank_code") String bankCode);
}
