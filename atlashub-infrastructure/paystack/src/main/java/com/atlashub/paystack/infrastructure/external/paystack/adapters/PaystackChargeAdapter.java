package com.atlashub.paystack.infrastructure.external.paystack.adapters;

import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClientRegistry;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClients;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.InitializeTransactionData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.InitializeTransactionRequest;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.PaystackTransactionData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.refund.CreateRefundRequest;
import com.atlashub.paystack.infrastructure.external.paystack.dto.refund.PaystackRefundData;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.List;

@Component
public class PaystackChargeAdapter implements ChargeProviderPort {
    private final ObjectProvider<PaystackClientRegistry> registries;
    public PaystackChargeAdapter(ObjectProvider<PaystackClientRegistry> registries){this.registries=registries;}
    @Override public InitializationResult initialize(InitializationRequest request){
        InitializeTransactionRequest payload=new InitializeTransactionRequest(request.email(),
                request.amount().movePointRight(2).longValueExact(),request.currency().name(),request.reference(),
                List.of(request.channel().name().toLowerCase()),request.environment()==ApiEnvironment.LIVE?
                request.settlementMerchantId():null,request.environment()==ApiEnvironment.LIVE?"subaccount":null,
                null,request.metadata());
        InitializeTransactionData data=require(clients(request.environment()).transactions().initialize(
                request.reference(),payload),"initialize transaction");
        if(blank(data.reference())||blank(data.authorizationUrl())||blank(data.accessCode())||
                !request.reference().equals(data.reference())) throw new IllegalStateException("Invalid Paystack checkout response");
        return new InitializationResult(data.reference(),data.authorizationUrl(),data.accessCode());
    }
    @Override public ProviderStatus fetchStatus(String reference,ApiEnvironment environment){
        PaystackTransactionData data=require(clients(environment).transactions().verify(reference),"verify transaction");
        if(!reference.equals(data.reference()))throw new IllegalStateException("Paystack returned another reference");
        return new ProviderStatus(data.reference(),data.status(),BigDecimal.valueOf(data.amount(),2),
                CurrencyCode.valueOf(data.currency()),data.channel(),BigDecimal.valueOf(data.fees(),2));
    }
    @Override public RefundResult refund(RefundRequest request){
        PaystackRefundData data=require(clients(request.environment()).refunds().create(request.idempotencyKey(),
                new CreateRefundRequest(request.providerReference(),request.amount().movePointRight(2).longValueExact(),
                        request.currency().name(),request.reason(),request.reason())),"create refund");
        if(blank(data.resolvedReference())||blank(data.status()))
            throw new IllegalStateException("Invalid Paystack refund response");
        return new RefundResult(data.resolvedReference(),data.status());
    }
    private PaystackClients clients(ApiEnvironment env){PaystackClientRegistry registry=registries.getIfAvailable();
        if(registry==null)throw new IllegalStateException("Paystack integration is disabled");return registry.forEnvironment(
                env==ApiEnvironment.LIVE?PaystackEnvironment.LIVE:PaystackEnvironment.TEST);}
    private <T>T require(PaystackResponse<T> response,String operation){if(response==null||!response.status()||response.data()==null)
        throw new IllegalStateException("Paystack could not "+operation);return response.data();}
    private boolean blank(String value){return value==null||value.isBlank();}
}
