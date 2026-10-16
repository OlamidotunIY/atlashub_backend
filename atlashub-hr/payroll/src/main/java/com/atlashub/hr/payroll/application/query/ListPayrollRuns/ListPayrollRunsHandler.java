package com.atlashub.hr.payroll.application.query.ListPayrollRuns;

import com.atlashub.hr.payroll.domain.entities.PayrollRun;
import com.atlashub.hr.payroll.domain.repositories.PayrollRunRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ListPayrollRunsHandler extends Query<ListPayrollRunsQuery, List<PayrollRunResult>> {

    private final PayrollRunRepository payrollRunRepository;

    public ListPayrollRunsHandler(PayrollRunRepository payrollRunRepository) {
        this.payrollRunRepository = payrollRunRepository;
    }

    @Override
    public List<PayrollRunResult> execute(ListPayrollRunsQuery query) {
        List<PayrollRun> runs;
        if (query.organizationId() != null && query.status() != null) {
            runs = payrollRunRepository.findByOrganizationIdAndStatus(query.organizationId(), query.status());
        } else if (query.organizationId() != null) {
            runs = payrollRunRepository.findByOrganizationId(query.organizationId());
        } else {
            runs = List.of();
        }

        return runs.stream()
                .map(this::toResult)
                .toList();
    }

    private PayrollRunResult toResult(PayrollRun run) {
        return new PayrollRunResult(
                run.getId(),
                run.getPeriod(),
                run.getTotalGross(),
                run.getTotalDeductions(),
                run.getTotalNet(),
                run.getStatus(),
                run.getInitiatedBy(),
                run.getApprovedBy(),
                run.getApprovedAt(),
                run.getCreatedAt()
        );
    }
}
