package com.atlashub.pay.accounts.application.commands;

import com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute.ConfigurePaystackSettlementRouteCommand;
import com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute.ConfigurePaystackSettlementRouteHandler;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationProviderProfile;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationProviderProfileRepository;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.pay.accounts.domain.valueobject.PaymentCapability;
import com.atlashub.pay.accounts.domain.valueobject.PaymentProvider;
import com.atlashub.pay.accounts.domain.valueobject.ProviderProfileStatus;
import com.atlashub.pay.accounts.domain.events.PaystackSettlementRouteConfiguredEvent;
import com.atlashub.shared.application.port.PaystackSettlementOnboardingPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ConfigurePaystackSettlementRouteHandlerTest {
    @Test void recordsConfirmedRouteAndWaitsForOnboardingApproval(){
        var deposits=mock(BusinessDepositAccountRepository.class);
        var profiles=mock(OrganizationProviderProfileRepository.class);
        var onboarding=mock(PaystackSettlementOnboardingPort.class);
        BusinessDepositAccount account=BusinessDepositAccount.request(30L,10L,ApiEnvironment.LIVE,20L,"CUS",CurrencyCode.NGN);
        account.markSubmitted("ANCHOR_ACC");
        account.activate(new ConfirmedBankingDetails("Atlas Shop","0123456789","******6789","Anchor","090"));
        when(deposits.findByOrganizationIdAndEnvironment(10L,ApiEnvironment.LIVE)).thenReturn(Optional.of(account));
        when(profiles.findByOrganizationIdAndEnvironmentAndProvider(10L,ApiEnvironment.LIVE,PaymentProvider.PAYSTACK))
                .thenReturn(Optional.empty());when(profiles.nextIdentity()).thenReturn(40L);
        when(profiles.save(any())).thenAnswer(i->i.getArgument(0));
        when(onboarding.configure(any())).thenReturn(new PaystackSettlementOnboardingPort.Result(
                "ACCT_sub","Atlas Shop","090",true));
        var handler=new ConfigurePaystackSettlementRouteHandler(deposits,profiles,onboarding);
        handler.execute(new ConfigurePaystackSettlementRouteCommand(10L,ApiEnvironment.LIVE));
        var captor=org.mockito.ArgumentCaptor.forClass(OrganizationProviderProfile.class);
        verify(profiles,org.mockito.Mockito.atLeast(2)).save(captor.capture());
        OrganizationProviderProfile profile=captor.getAllValues().getLast();
        assertFalse(profile.supports(PaymentCapability.CARD_COLLECTION));
        assertFalse(profile.supports(PaymentCapability.USSD_COLLECTION));
        assertEquals(ProviderProfileStatus.PROVISIONING, profile.getStatus());
        assertEquals("ACCT_sub", profile.getExternalMerchantId());
        assertEquals("30", profile.getSettlementAccountReference());
        assertInstanceOf(PaystackSettlementRouteConfiguredEvent.class, profile.pullDomainEvents().getLast());
    }

    @Test void ignoresTestEnvironment(){
        var deposits=mock(BusinessDepositAccountRepository.class);var profiles=mock(OrganizationProviderProfileRepository.class);
        var onboarding=mock(PaystackSettlementOnboardingPort.class);
        new ConfigurePaystackSettlementRouteHandler(deposits,profiles,onboarding)
                .execute(new ConfigurePaystackSettlementRouteCommand(10L,ApiEnvironment.TEST));
        verify(onboarding,never()).configure(any());
    }
}
