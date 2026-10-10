package com.atlashub.paystack.infrastructure.external.paystack.adapters;

import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackBankClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClientRegistry;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackClients;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSettlementClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackSubaccountClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackTransactionClient;
import com.atlashub.paystack.infrastructure.external.paystack.client.PaystackRefundClient;
import com.atlashub.paystack.infrastructure.external.paystack.configuration.PaystackEnvironment;
import com.atlashub.paystack.infrastructure.external.paystack.dto.bank.ResolvedAccountData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.common.PaystackResponse;
import com.atlashub.paystack.infrastructure.external.paystack.dto.settlement.PaystackSettlementData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.subaccount.PaystackSubaccountData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.InitializeTransactionData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.transaction.PaystackTransactionData;
import com.atlashub.paystack.infrastructure.external.paystack.dto.refund.PaystackRefundData;
import com.atlashub.shared.application.port.ChargeProviderPort;
import com.atlashub.shared.application.port.PaystackSettlementOnboardingPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PaystackPortAdaptersTest {
    private PaystackBankClient banks;private PaystackSubaccountClient subaccounts;
    private PaystackTransactionClient transactions;private PaystackSettlementClient settlements;
    private PaystackRefundClient refunds;
    private ObjectProvider<PaystackClientRegistry> provider;
    @BeforeEach @SuppressWarnings("unchecked") void setUp(){banks=mock(PaystackBankClient.class);subaccounts=mock(PaystackSubaccountClient.class);
        transactions=mock(PaystackTransactionClient.class);settlements=mock(PaystackSettlementClient.class);
        refunds=mock(PaystackRefundClient.class);
        PaystackClients clients=new PaystackClients(banks,subaccounts,transactions,settlements,refunds);
        provider=mock(ObjectProvider.class);when(provider.getIfAvailable()).thenReturn(new PaystackClientRegistry(
                Map.of(PaystackEnvironment.TEST,clients,PaystackEnvironment.LIVE,clients)));}

    @Test void initializesAndVerifiesChargeThroughTypedClient(){
        when(transactions.initialize(eq("REF"),any())).thenReturn(new PaystackResponse<>(true,"ok",
                new InitializeTransactionData("https://checkout","ACCESS","REF"),null));
        when(transactions.verify("REF")).thenReturn(new PaystackResponse<>(true,"ok",
                new PaystackTransactionData(1L,"success","REF",10000L,200L,"NGN","card",ZonedDateTime.now(),null),null));
        var adapter=new PaystackChargeAdapter(provider);var initialized=adapter.initialize(new ChargeProviderPort.InitializationRequest(
                ApiEnvironment.TEST,"buyer@example.com",new BigDecimal("100.00"),CurrencyCode.NGN,
                ChargeProviderPort.ChargeChannel.CARD,"REF",null,Map.of()));
        assertEquals("ACCESS",initialized.accessCode());assertEquals(new BigDecimal("100.00"),
                adapter.fetchStatus("REF",ApiEnvironment.TEST).amount());}

    @Test void createsRefundThroughTypedClient(){
        when(refunds.create(eq("refund-100"),any())).thenReturn(new PaystackResponse<>(true,"queued",
                new PaystackRefundData(44L,"pending",10000L,"NGN",null),null));
        var result=new PaystackChargeAdapter(provider).refund(new ChargeProviderPort.RefundRequest(
                ApiEnvironment.TEST,"REF",new BigDecimal("100.00"),CurrencyCode.NGN,"Cancelled","refund-100"));
        assertEquals("44",result.refundReference());assertEquals("pending",result.status());}

    @Test void resolvesAccountAndCreatesLiveSettlementSubaccount(){
        when(banks.resolve("0123456789","090")).thenReturn(new PaystackResponse<>(true,"ok",
                new ResolvedAccountData("0123456789","Atlas Shop"),null));
        when(subaccounts.create(eq("route-10"),any())).thenReturn(new PaystackResponse<>(true,"ok",
                new PaystackSubaccountData("ACCT_sub","Atlas Shop","0123456789","090","auto",true),null));
        var result=new PaystackSettlementOnboardingAdapter(provider).configure(
                new PaystackSettlementOnboardingPort.Request(10L,ApiEnvironment.LIVE,"0123456789","090",
                        "Atlas Shop",null,"route-10"));
        assertEquals("ACCT_sub",result.subaccountCode());assertTrue(result.active());}

    @Test void mapsSuccessfulSettlementAndItsTransactions(){
        ZonedDateTime settledAt=ZonedDateTime.now();
        when(settlements.list("ACCT_sub",1,100)).thenReturn(new PaystackResponse<>(true,"ok",List.of(
                new PaystackSettlementData(123L,"success",9800L,10000L,200L,"NGN",settledAt,null)),null));
        when(settlements.transactions("123",1,100)).thenReturn(new PaystackResponse<>(true,"ok",List.of(
                new PaystackTransactionData(1L,"success","TX",10000L,200L,"NGN","card",settledAt,null)),null));
        var batches=new PaystackSettlementAdapter(provider).fetchSuccessfulSettlements(ApiEnvironment.LIVE,"ACCT_sub",null);
        assertEquals(1,batches.size());assertEquals("123",batches.getFirst().providerSettlementId());
        assertEquals(List.of("TX"),batches.getFirst().transactionReferences());}
}
