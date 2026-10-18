package com.atlashub.pay.mandates.application.queries.GetMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetMandateHandlerTest {

    @Mock
    private PaymentMandateRepository mandateRepository;

    private GetMandateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GetMandateHandler(mandateRepository);
    }

    @Test
    @DisplayName("Should retrieve mandate result successfully")
    void shouldGetMandate() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.of(2026, 12, 1)
        );

        when(mandateRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(mandate));

        MandateResult result = handler.execute(new GetMandateQuery(1L, 10L));

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals("CUST-1", result.customerId());
        assertEquals("user@example.com", result.email());
        assertEquals(0, new BigDecimal("5000.00").compareTo(result.amount()));
        assertEquals("ACTIVE", result.status());
    }

    @Test
    @DisplayName("Should throw MandateNotFoundException when not found")
    void shouldThrowWhenNotFound() {
        when(mandateRepository.findByIdAndOrganizationId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(MandateNotFoundException.class, () -> handler.execute(new GetMandateQuery(99L, 10L)));
    }
}
