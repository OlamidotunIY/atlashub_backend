package com.atlashub.commerce.storefront.application.commands.MarkKotReady;

import com.atlashub.commerce.storefront.domain.entities.KitchenOrderTicket;
import com.atlashub.commerce.storefront.domain.entities.KotItem;
import com.atlashub.commerce.storefront.domain.exceptions.KitchenOrderTicketNotFoundException;
import com.atlashub.commerce.storefront.domain.repositories.KitchenOrderTicketRepository;
import com.atlashub.commerce.storefront.domain.valueobject.KotStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkKotReadyHandlerTest {

    @Mock
    private KitchenOrderTicketRepository kotRepository;

    @InjectMocks
    private MarkKotReadyHandler handler;

    @Test
    @DisplayName("Should mark KOT ready")
    void execute_shouldMarkKotReady() {
        KotItem item = KotItem.create(1L, 700L, 10L, "Jollof Rice", 2);
        KitchenOrderTicket kot = KitchenOrderTicket.create(700L, 100L, 5L, 20L, List.of(item));
        kot.markInProgress();
        when(kotRepository.findById(700L)).thenReturn(Optional.of(kot));

        handler.execute(new MarkKotReadyCommand(700L));

        assertEquals(KotStatus.READY, kot.getStatus());
        verify(kotRepository).save(kot);
    }

    @Test
    @DisplayName("Should throw KitchenOrderTicketNotFoundException when KOT not found")
    void execute_shouldThrowException_whenNotFound() {
        when(kotRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(KitchenOrderTicketNotFoundException.class, () ->
                handler.execute(new MarkKotReadyCommand(999L))
        );
    }
}
