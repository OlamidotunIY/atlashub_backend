package com.atlashub.pay.accounts.infrastructure.messaging.schedulers;

import com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts.ReconcileAnchorAccountsCommand;
import com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts.ReconcileAnchorAccountsHandler;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AnchorAccountReconciliationSchedulerTest {
    @Test
    void delegates_only_to_the_reconciliation_command() {
        ReconcileAnchorAccountsHandler handler = mock(ReconcileAnchorAccountsHandler.class);

        new AnchorAccountReconciliationScheduler(handler).reconcile();

        ArgumentCaptor<ReconcileAnchorAccountsCommand> command =
                ArgumentCaptor.forClass(ReconcileAnchorAccountsCommand.class);
        verify(handler).execute(command.capture());
        assertEquals(100, command.getValue().batchSize());
    }
}
