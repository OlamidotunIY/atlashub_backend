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
import com.atlashub.integrations.application.port.ConnectedChargeAdapter;
import com.atlashub.integrations.application.port.ConnectionVerifier;
import com.atlashub.integrations.domain.valueobject.Capability;
import com.atlashub.integrations.domain.valueobject.Provider;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

@Component
public class PaystackChargeAdapter implements ConnectedChargeAdapter, ConnectionVerifier {
    private final ObjectProvider<PaystackClientRegistry> registries;
    public PaystackChargeAdapter(ObjectProvider<PaystackClientRegistry> registries){this.registries=registries;}
    @Override public Provider provider(){return Provider.PAYSTACK;}
    @Override public Verification verify(ApiEnvironment environment,String merchantCountry,String credential,
                                         Set<Capability> documentedCapabilities){
        String country=switch(merchantCountry){case "NG"->"nigeria";case "GH"->"ghana";case "KE"->"kenya";
            case "ZA"->"south africa";case "CI"->"cote d'ivoire";case "EG"->"egypt";
            default->throw new IllegalArgumentException("Unsupported Paystack merchant country");};
        require(clients(credential).banks().list(country),"verify merchant credential");
        try{
            String fingerprint=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(credential.getBytes(StandardCharsets.UTF_8))).substring(0,16);
            return new Verification("paystack:"+fingerprint,documentedCapabilities);
        }catch(java.security.NoSuchAlgorithmException impossible){throw new IllegalStateException(impossible);}
    }
    @Override public ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest request,
                                                                         String credential){
        return initialize(request, clients(credential));
    }
    public ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest request){
        return initialize(request, clients(request.environment()));
    }
    private ChargeProviderPort.InitializationResult initialize(ChargeProviderPort.InitializationRequest request,
                                                                PaystackClients merchantClients){
        InitializeTransactionRequest payload=new InitializeTransactionRequest(request.email(),
                request.amount().movePointRight(2).longValueExact(),request.currency().name(),request.reference(),
                List.of(request.channel().name().toLowerCase()),null,null,null,request.metadata());
        InitializeTransactionData data=require(merchantClients.transactions().initialize(
                request.reference(),payload),"initialize transaction");
        if(blank(data.reference())||blank(data.authorizationUrl())||blank(data.accessCode())||
                !request.reference().equals(data.reference())) throw new IllegalStateException("Invalid Paystack checkout response");
        return new ChargeProviderPort.InitializationResult(data.reference(),data.authorizationUrl(),data.accessCode());
    }
    @Override public ChargeProviderPort.ProviderStatus fetchStatus(String reference,String credential){
        return fetchStatus(reference, clients(credential));
    }
    public ChargeProviderPort.ProviderStatus fetchStatus(String reference,ApiEnvironment environment){
        return fetchStatus(reference, clients(environment));
    }
    private ChargeProviderPort.ProviderStatus fetchStatus(String reference, PaystackClients merchantClients){
        PaystackTransactionData data=require(merchantClients.transactions().verify(reference),"verify transaction");
        if(!reference.equals(data.reference()))throw new IllegalStateException("Paystack returned another reference");
        return new ChargeProviderPort.ProviderStatus(data.reference(),data.status(),BigDecimal.valueOf(data.amount(),2),
                CurrencyCode.valueOf(data.currency()),data.channel(),BigDecimal.valueOf(data.fees(),2));
    }
    @Override public ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request,String credential){
        return refund(request, clients(credential));
    }
    public ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request){
        return refund(request, clients(request.environment()));
    }
    private ChargeProviderPort.RefundResult refund(ChargeProviderPort.RefundRequest request,
                                                    PaystackClients merchantClients){
        PaystackRefundData data=require(merchantClients.refunds().create(request.idempotencyKey(),
                new CreateRefundRequest(request.providerReference(),request.amount().movePointRight(2).longValueExact(),
                        request.currency().name(),request.reason(),request.reason())),"create refund");
        if(blank(data.resolvedReference())||blank(data.status()))
            throw new IllegalStateException("Invalid Paystack refund response");
        return new ChargeProviderPort.RefundResult(data.resolvedReference(),data.status());
    }
    private PaystackClients clients(ApiEnvironment env){PaystackClientRegistry registry=registries.getIfAvailable();
        if(registry==null)throw new IllegalStateException("Paystack integration is disabled");return registry.forEnvironment(
                env==ApiEnvironment.LIVE?PaystackEnvironment.LIVE:PaystackEnvironment.TEST);}
    private PaystackClients clients(String credential){
        if(credential==null||credential.isBlank())throw new IllegalArgumentException("Paystack credential is required");
        RestClient client=RestClient.builder().baseUrl("https://api.paystack.co")
                .defaultHeader(HttpHeaders.AUTHORIZATION,"Bearer "+credential)
                .defaultHeader(HttpHeaders.ACCEPT,MediaType.APPLICATION_JSON_VALUE).build();
        HttpServiceProxyFactory proxy=HttpServiceProxyFactory.builderFor(RestClientAdapter.create(client)).build();
        return new PaystackClients(proxy.createClient(com.atlashub.paystack.infrastructure.external.paystack.client.PaystackBankClient.class),
                proxy.createClient(com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSubaccountClient.class),
                proxy.createClient(com.atlashub.paystack.infrastructure.external.paystack.client.PaystackTransactionClient.class),
                proxy.createClient(com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSettlementClient.class),
                proxy.createClient(com.atlashub.paystack.infrastructure.external.paystack.client.PaystackRefundClient.class));
    }
    private <T>T require(PaystackResponse<T> response,String operation){if(response==null||!response.status()||response.data()==null)
        throw new IllegalStateException("Paystack could not "+operation);return response.data();}
    private boolean blank(String value){return value==null||value.isBlank();}
}
