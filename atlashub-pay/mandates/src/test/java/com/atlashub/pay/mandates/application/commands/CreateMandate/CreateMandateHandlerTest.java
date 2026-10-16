package com.atlashub.pay.mandates.application.commands.CreateMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.repositories.PaymentMandateRepository;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateMandateHandlerTest {

    @Mock
    private PaymentMandateRepository mandateRepository;

    private CreateMandateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CreateMandateHandler(mandateRepository);
    }

    @Test
    @DisplayName("Should create mandate successfully")
    void shouldCreateMandate() {
        when(mandateRepository.nextIdentity()).thenReturn(100L);

        CreateMandateCommand command = new CreateMandateCommand(
                10L,
                "CUST-001",
                "customer@example.com",
                new BigDecimal("25000.00"),
                "NGN",
                MandateFrequency.MONTHLY,
                "AUTH_123456",
                LocalDate.of(2026, 12, 1)
        );

        CreateMandateResponse response = handler.execute(command);

        assertNotNull(response);
        assertEquals(100L, response.mandateId());
        assertEquals("ACTIVE", response.status());
        assertEquals(LocalDate.of(2026, 12, 1), response.nextChargeDate());
        verify(mandateRepository).save(any(PaymentMandate.class));
    }
}
