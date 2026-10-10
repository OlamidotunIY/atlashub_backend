package com.atlashub.pay.charges.infrastructure.messaging.schedulers;

import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesCommand;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesHandler;
import com.atlashub.pay.charges.application.commands.ReconcilePendingCharges.ReconcilePendingChargesResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChargeReconciliationSchedulerTest {

    @Test
    @DisplayName("Should invoke ReconcilePendingChargesHandler when scheduled run is triggered")
    void shouldTriggerReconciliationCommand() {
        ReconcilePendingChargesHandler handler = mock(ReconcilePendingChargesHandler.class);
        when(handler.execute(any(ReconcilePendingChargesCommand.class)))
                .thenReturn(new ReconcilePendingChargesResult(5, 0));

        ChargeReconciliationScheduler scheduler = new ChargeReconciliationScheduler(handler);
        scheduler.run();

        ArgumentCaptor<ReconcilePendingChargesCommand> captor = ArgumentCaptor.forClass(ReconcilePendingChargesCommand.class);
        verify(handler).execute(captor.capture());
        assertNotNull(captor.getValue());
        assertNotNull(captor.getValue().createdBefore());
    }
}
