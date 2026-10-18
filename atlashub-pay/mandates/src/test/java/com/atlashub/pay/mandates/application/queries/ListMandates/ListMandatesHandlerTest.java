package com.atlashub.pay.mandates.application.queries.ListMandates;

import com.atlashub.pay.mandates.application.queries.GetMandate.MandateResult;
import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListMandatesHandlerTest {

    @Mock
    private PaymentMandateRepository mandateRepository;

    private ListMandatesHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ListMandatesHandler(mandateRepository);
    }

    @Test
    @DisplayName("Should list mandates with filters")
    void shouldListWithFilters() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        );

        when(mandateRepository.findByOrganizationIdAndCustomerIdAndStatus(10L, "CUST-1", MandateStatus.ACTIVE))
                .thenReturn(List.of(mandate));

        List<MandateResult> results = handler.execute(new ListMandatesQuery(10L, "CUST-1", MandateStatus.ACTIVE));

        assertEquals(1, results.size());
        assertEquals("CUST-1", results.get(0).customerId());
    }

    @Test
    @DisplayName("Should list mandates without filters")
    void shouldListWithoutFilters() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        );

        when(mandateRepository.findByOrganizationId(10L)).thenReturn(List.of(mandate));

        List<MandateResult> results = handler.execute(new ListMandatesQuery(10L, null, null));

        assertEquals(1, results.size());
    }
}
