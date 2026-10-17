package com.atlashub.logistics.shipping.infrastructure.messaging.listeners;

import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentCommand;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentHandler;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.infrastructure.messaging.events.OnlineOrderCreatedPayload;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OnlineOrderCreatedListenerTest {

    @Mock
    private CreateShipmentHandler createShipmentHandler;

    private OnlineOrderCreatedListener listener;

    @BeforeEach
    void setUp() {
        listener = new OnlineOrderCreatedListener(createShipmentHandler);
    }

    @Test
    @DisplayName("Should receive order created payload, build CreateShipmentCommand and invoke handler")
    void shouldHandleOrderCreatedEvent() {
        Address origin = new Address("Store 1", "Ikeja", "Lagos", "Nigeria", "100001");
        Address destination = new Address("House 2", "Lekki", "Lagos", "Nigeria", "100001");
        Dimensions dimensions = new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN);
        Money value = Money.of(new BigDecimal("15000"), CurrencyCode.NGN);

        OnlineOrderCreatedPayload payload = new OnlineOrderCreatedPayload(
                100L,
                555L,
                DeliveryTier.STANDARD,
                origin,
                destination,
                777L,
                new BigDecimal("3.5"),
                dimensions,
                value,
                "corr-online-order"
        );

        listener.onOrderCreated(payload);

        ArgumentCaptor<CreateShipmentCommand> captor = ArgumentCaptor.forClass(CreateShipmentCommand.class);
        verify(createShipmentHandler).execute(captor.capture());

        CreateShipmentCommand command = captor.getValue();
        assertEquals(100L, command.organizationId());
        assertEquals(555L, command.orderId());
        assertEquals(ShipmentType.CUSTOMER_ORDER, command.type());
        assertEquals(DeliveryTier.STANDARD, command.serviceTier());
        assertEquals(777L, command.receiverId());
        assertEquals(new BigDecimal("3.5"), command.weightKg());
    }
}
