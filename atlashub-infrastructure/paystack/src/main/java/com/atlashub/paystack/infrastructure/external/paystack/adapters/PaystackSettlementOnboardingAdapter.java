package com.atlashub.paystack.infrastructure.external.paystack.adapters;

import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClientRegistry;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClients;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount.PaystackSubaccountData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount.PaystackSubaccountRequest;
import com.atlashub.shared.application.port.PaystackSettlementOnboardingPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class PaystackSettlementOnboardingAdapter implements PaystackSettlementOnboardingPort {
    private final ObjectProvider<PaystackClientRegistry> registries;
    public PaystackSettlementOnboardingAdapter(ObjectProvider<PaystackClientRegistry> registries){this.registries=registries;}
    @Override public Result configure(Request request){
        if(request.environment()!=ApiEnvironment.LIVE)throw new IllegalArgumentException("Paystack settlement onboarding is live-only");
        PaystackClientRegistry registry=registries.getIfAvailable();if(registry==null)throw new IllegalStateException("Paystack integration is disabled");
        PaystackClients clients=registry.forEnvironment(PaystackEnvironment.LIVE);
        var resolved=require(clients.banks().resolve(request.accountNumber(),request.bankCode()),"resolve account");
        if(!request.accountNumber().equals(resolved.accountNumber()))throw new IllegalStateException("Paystack resolved another account number");
        PaystackSubaccountRequest payload=new PaystackSubaccountRequest(request.accountName(),request.bankCode(),
                request.accountNumber(),BigDecimal.ZERO,"auto");
        PaystackResponse<PaystackSubaccountData>response=request.existingSubaccountCode()==null||request.existingSubaccountCode().isBlank()
                ?clients.subaccounts().create(request.idempotencyReference(),payload)
                :clients.subaccounts().update(request.existingSubaccountCode(),request.idempotencyReference(),payload);
        PaystackSubaccountData data=require(response,"configure settlement subaccount");
        if(data.subaccountCode()==null||data.subaccountCode().isBlank())throw new IllegalStateException("Paystack did not return a subaccount code");
        return new Result(data.subaccountCode(),resolved.accountName(),data.settlementBank(),data.active());}
    private <T>T require(PaystackResponse<T>response,String action){if(response==null||!response.status()||response.data()==null)
        throw new IllegalStateException("Paystack could not "+action);return response.data();}
}
