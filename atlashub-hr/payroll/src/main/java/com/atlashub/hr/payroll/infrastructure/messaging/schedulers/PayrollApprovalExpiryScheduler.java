package com.atlashub.hr.payroll.infrastructure.messaging.schedulers;

import com.atlashub.hr.payroll.application.command.ExpirePayrollApproval.ExpirePayrollApprovalCommand;
import com.atlashub.hr.payroll.application.command.ExpirePayrollApproval.ExpirePayrollApprovalHandler;
import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

@Component
public class PayrollApprovalExpiryScheduler {

    private final PayrollRunRepository payrollRunRepository;
    private final ExpirePayrollApprovalHandler expirePayrollApprovalHandler;

    public PayrollApprovalExpiryScheduler(
            PayrollRunRepository payrollRunRepository,
            ExpirePayrollApprovalHandler expirePayrollApprovalHandler
    ) {
        this.payrollRunRepository = payrollRunRepository;
        this.expirePayrollApprovalHandler = expirePayrollApprovalHandler;
    }

    @Scheduled(cron = "0 0 * * * *")
    public void expireStaleApprovals() {
        ZonedDateTime threshold = ZonedDateTime.now().minusHours(72);
        List<PayrollRun> staleRuns = payrollRunRepository.findByStatusAndCreatedAtBefore(
                PayrollStatus.PENDING_APPROVAL,
                threshold
        );

        for (PayrollRun run : staleRuns) {
            expirePayrollApprovalHandler.execute(new ExpirePayrollApprovalCommand(run.getId()));
        }
    }
}
