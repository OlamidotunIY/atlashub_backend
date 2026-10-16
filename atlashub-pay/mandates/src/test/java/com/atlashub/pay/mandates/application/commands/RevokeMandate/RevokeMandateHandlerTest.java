package com.atlashub.pay.mandates.application.commands.RevokeMandate;

import com.atlashub.pay.mandates.domain.entities.PaymentMandate;
import com.atlashub.pay.mandates.domain.exceptions.MandateNotFoundException;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RevokeMandateHandlerTest {

    @Mock
    private PaymentMandateRepository mandateRepository;

    private RevokeMandateHandler handler;

    @BeforeEach
    void setUp() {
        handler = new RevokeMandateHandler(mandateRepository);
    }

    @Test
    @DisplayName("Should revoke mandate successfully")
    void shouldRevokeMandate() {
        PaymentMandate mandate = PaymentMandate.create(
                1L, 10L, "CUST-1", new EmailAddress("user@example.com"),
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        );

        when(mandateRepository.findByIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(mandate));

        handler.execute(new RevokeMandateCommand(1L, 10L));

        assertEquals(MandateStatus.REVOKED, mandate.getStatus());
        verify(mandateRepository).save(mandate);
    }

    @Test
    @DisplayName("Should throw MandateNotFoundException when mandate not found")
    void shouldThrowWhenNotFound() {
        when(mandateRepository.findByIdAndOrganizationId(99L, 10L)).thenReturn(Optional.empty());

        assertThrows(MandateNotFoundException.class, () -> handler.execute(new RevokeMandateCommand(99L, 10L)));
    }
}
