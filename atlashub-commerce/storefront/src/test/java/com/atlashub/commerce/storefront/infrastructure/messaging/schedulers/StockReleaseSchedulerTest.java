package com.atlashub.commerce.storefront.infrastructure.messaging.schedulers;

import com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders.ReleaseTimedOutOrdersCommand;
import com.atlashub.commerce.storefront.application.commands.ReleaseTimedOutOrders.ReleaseTimedOutOrdersHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StockReleaseSchedulerTest {

    @Mock
    private ReleaseTimedOutOrdersHandler handler;

    @InjectMocks
    private StockReleaseScheduler scheduler;

    @Test
    @DisplayName("Should invoke ReleaseTimedOutOrdersHandler with 15 minutes timeout")
    void shouldExecuteReleaseTimedOutOrders() {
        scheduler.releaseTimedOutOrders();

        verify(handler).execute(new ReleaseTimedOutOrdersCommand(15));
    }
}
