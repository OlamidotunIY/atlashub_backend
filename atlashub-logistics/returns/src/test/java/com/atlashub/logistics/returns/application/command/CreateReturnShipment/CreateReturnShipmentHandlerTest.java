package com.atlashub.logistics.returns.application.command.CreateReturnShipment;

import com.atlashub.logistics.returns.domain.entities.ReturnShipment;
import com.atlashub.logistics.returns.domain.repositories.ReturnShipmentRepository;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
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
class CreateReturnShipmentHandlerTest {

    @Mock
    private ReturnShipmentRepository returnShipmentRepository;

    private CreateReturnShipmentHandler handler;

    private final Address address = new Address("45 Palm Ave", "Surulere", "Lagos", "Nigeria", "100001");

    @BeforeEach
    void setUp() {
        handler = new CreateReturnShipmentHandler(returnShipmentRepository);
    }

    @Test
    @DisplayName("Should create return shipment and generate tracking number")
    void shouldCreateReturnShipment() {
        when(returnShipmentRepository.nextIdentity()).thenReturn(701L);

        CreateReturnShipmentCommand command = new CreateReturnShipmentCommand(
                100L,
                1001L,
                2001L,
                555L,
                address,
                10L,
                ReturnReasonType.CUSTOMER_RETURN,
                ReturnFaultType.MERCHANT_FAULT,
                0,
                Money.of(new BigDecimal("15000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2500"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("15000"), CurrencyCode.NGN),
                "corr-cmd-1"
        );

        CreateReturnShipmentResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(701L, result.returnShipmentId());
        assertEquals("CREATED", result.status());
        assertEquals("MERCHANT_FAULT", result.faultType());
        // Refund = 15,000 + 2,500 = 17,500
        assertEquals(0, new BigDecimal("17500").compareTo(result.customerRefundAmount().amount()));

        verify(returnShipmentRepository).save(any(ReturnShipment.class));
    }
}
