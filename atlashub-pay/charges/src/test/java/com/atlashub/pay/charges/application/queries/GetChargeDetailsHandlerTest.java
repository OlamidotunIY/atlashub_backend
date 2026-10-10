package com.atlashub.pay.charges.application.queries;

import com.atlashub.pay.charges.application.queries.GetChargeDetails.ChargeResult;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsHandler;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsQuery;
import com.atlashub.pay.charges.domain.entities.Charge;
import com.atlashub.pay.charges.domain.exceptions.ChargeNotFoundException;
import com.atlashub.pay.charges.domain.repositories.ChargeRepository;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetChargeDetailsHandlerTest {

    @Mock
    private ChargeRepository chargeRepository;

    @InjectMocks
    private GetChargeDetailsHandler handler;

    private Charge createCharge() {
        Charge charge = Charge.initialize(
                200L, 10L, ApiEnvironment.TEST, "REF-QUERY-1",
                Money.of(new BigDecimal("7500.00"), CurrencyCode.NGN),
                ChargeChannel.CARD, 20L, "COMMERCE", "ORD-200"
        );
        charge.markPending("PAYSTACK-200", "https://checkout.auth", "CODE-200");
        return charge;
    }

    @Test
    @DisplayName("Should return ChargeResult for existing charge matching org and env")
    void shouldReturnChargeResult() {
        Charge charge = createCharge();
        when(chargeRepository.findById(200L)).thenReturn(Optional.of(charge));

        GetChargeDetailsQuery query = new GetChargeDetailsQuery(200L, 10L, ApiEnvironment.TEST);

        ChargeResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(200L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals(ApiEnvironment.TEST, result.environment());
        assertEquals("REF-QUERY-1", result.reference());
        assertEquals(ChargeChannel.CARD, result.channel());
        assertEquals(ChargeStatus.PENDING, result.status());
        assertEquals("PAYSTACK-200", result.providerReference());
        assertEquals("https://checkout.auth", result.authorizationUrl());
        assertEquals("CODE-200", result.accessCode());
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when charge ID not found")
    void shouldThrowWhenChargeNotFound() {
        when(chargeRepository.findById(999L)).thenReturn(Optional.empty());

        GetChargeDetailsQuery query = new GetChargeDetailsQuery(999L, 10L, ApiEnvironment.TEST);

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(query));
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when organizationId mismatches")
    void shouldThrowWhenOrganizationMismatches() {
        Charge charge = createCharge(); // org = 10L
        when(chargeRepository.findById(200L)).thenReturn(Optional.of(charge));

        GetChargeDetailsQuery query = new GetChargeDetailsQuery(200L, 999L, ApiEnvironment.TEST);

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(query));
    }

    @Test
    @DisplayName("Should throw ChargeNotFoundException when environment mismatches")
    void shouldThrowWhenEnvironmentMismatches() {
        Charge charge = createCharge(); // env = TEST
        when(chargeRepository.findById(200L)).thenReturn(Optional.of(charge));

        GetChargeDetailsQuery query = new GetChargeDetailsQuery(200L, 10L, ApiEnvironment.LIVE);

        assertThrows(ChargeNotFoundException.class, () -> handler.execute(query));
    }
}
