package com.atlashub.logistics.returns.application.command.ReceiveReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiveReturnShipmentHandlerTest {

    @Mock
    private ReturnShipmentRepository returnShipmentRepository;

    private ReceiveReturnShipmentHandler handler;

    private final Address address = new Address("45 Palm Ave", "Surulere", "Lagos", "Nigeria", "100001");

    @BeforeEach
    void setUp() {
        handler = new ReceiveReturnShipmentHandler(returnShipmentRepository);
    }

    @Test
    @DisplayName("Should receive return shipment and transition to RECEIVED status")
    void shouldReceiveReturn() {
        ReturnShipment shipment = ReturnShipment.create(
                1L, 100L, 1001L, 2001L, 555L, address, 10L, "RET-101",
                ReturnReasonType.CUSTOMER_RETURN, ReturnFaultType.CUSTOMER_FAULT, 0,
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                "corr-cmd-1"
        );

        when(returnShipmentRepository.findById(1L)).thenReturn(Optional.of(shipment));

        ZonedDateTime now = ZonedDateTime.now();
        ReceiveReturnShipmentCommand command = new ReceiveReturnShipmentCommand(
                100L, 1L, "Package intact, verified sealed", now, "corr-rec-1"
        );

        ReceiveReturnShipmentResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(1L, result.returnShipmentId());
        assertEquals("RECEIVED", result.status());
        assertEquals(0, new BigDecimal("8500").compareTo(result.customerRefundAmount().amount()));

        verify(returnShipmentRepository).save(any(ReturnShipment.class));
    }
}
