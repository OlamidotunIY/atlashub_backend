package com.atlashub.commerce.storefront.application.commands.SendKitchenOrder;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.repositories.KitchenOrderTicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SendKitchenOrderHandlerTest {

    @Mock
    private KitchenOrderTicketRepository kotRepository;

    @InjectMocks
    private SendKitchenOrderHandler handler;

    @Test
    @DisplayName("Should create and send KOT")
    void execute_shouldSendKitchenOrder() {
        KotItemDto itemDto = new KotItemDto(10L, "Jollof Rice", 2);
        SendKitchenOrderCommand command = new SendKitchenOrderCommand(100L, 5L, 20L, List.of(itemDto));

        when(kotRepository.nextIdentity()).thenReturn(700L);
        when(kotRepository.save(any(KitchenOrderTicket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SendKitchenOrderResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(700L, result.kotId());
        verify(kotRepository).save(any(KitchenOrderTicket.class));
    }
}
