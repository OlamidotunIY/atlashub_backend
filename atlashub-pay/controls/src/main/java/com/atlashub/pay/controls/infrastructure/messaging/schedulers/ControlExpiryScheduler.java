package com.atlashub.pay.controls.infrastructure.messaging.schedulers;

import com.atlashub.pay.controls.application.commands.ExpireControlledFundsPolicy.ExpireControlledFundsPolicyCommand;
import com.atlashub.pay.controls.application.commands.ExpireControlledFundsPolicy.ExpireControlledFundsPolicyHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ControlExpiryScheduler {

    private final ExpireControlledFundsPolicyHandler expirePolicyHandler;

    public ControlExpiryScheduler(ExpireControlledFundsPolicyHandler expirePolicyHandler) {
        this.expirePolicyHandler = expirePolicyHandler;
    }

    @Scheduled(cron = "${atlashub.controls.expiry-cron:0 0 * * * *}")
    public void processExpirations() {
        expirePolicyHandler.execute(new ExpireControlledFundsPolicyCommand(null));
    }
}
