package com.atlashub.hr.payroll.domain.entities;

import com.atlashub.hr.payroll.domain.events.PayrollApprovalExpiredEvent;
import com.atlashub.hr.payroll.domain.events.PayrollApprovedEvent;
import com.atlashub.hr.payroll.domain.events.PayrollDisbursedEvent;
import com.atlashub.hr.payroll.domain.events.PayrollPendingApprovalEvent;
import com.atlashub.hr.payroll.domain.exceptions.InvalidPayrollStateException;
import com.atlashub.hr.payroll.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.hr.payroll.domain.valueobject.PayrollStatus;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PayrollRun extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final YearMonth period;
    private final List<Payslip> payslips;
    private final Money totalGross;
    private final Money totalDeductions;
    private final Money totalNet;
    private PayrollStatus status;
    private final Long initiatedBy;
    private Long approvedBy;
    private ZonedDateTime approvedAt;
    private final ZonedDateTime createdAt;

    public PayrollRun(
            Long id,
            Long organizationId,
            YearMonth period,
            List<Payslip> payslips,
            Money totalGross,
            Money totalDeductions,
            Money totalNet,
            PayrollStatus status,
            Long initiatedBy,
            Long approvedBy,
            ZonedDateTime approvedAt,
            ZonedDateTime createdAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.period = period;
        this.payslips = payslips != null ? new ArrayList<>(payslips) : new ArrayList<>();
        this.totalGross = totalGross;
        this.totalDeductions = totalDeductions;
        this.totalNet = totalNet;
        this.status = status;
        this.initiatedBy = initiatedBy;
        this.approvedBy = approvedBy;
        this.approvedAt = approvedAt;
        this.createdAt = createdAt != null ? createdAt : ZonedDateTime.now();
    }

    public static PayrollRun create(
            Long id,
            Long organizationId,
            YearMonth period,
            Long initiatorUserId,
            List<Payslip> payslips
    ) {
        if (organizationId == null) {
            throw new InvalidPayrollStateException("Organization ID must not be null");
        }
        if (period == null) {
            throw new InvalidPayrollStateException("Payroll period must not be null");
        }
        if (initiatorUserId == null) {
            throw new InvalidPayrollStateException("Initiator user ID must not be null");
        }

        List<Payslip> payslipList = payslips != null ? new ArrayList<>(payslips) : List.of();
        CurrencyCode currency = !payslipList.isEmpty() && payslipList.get(0).getNetPay() != null
                ? payslipList.get(0).getNetPay().currency()
                : CurrencyCode.NGN;

        Money totalGross = Money.zero(currency);
        Money totalDeductions = Money.zero(currency);
        Money totalNet = Money.zero(currency);

        for (Payslip p : payslipList) {
            if (p.getGrossPay() != null) {
                totalGross = totalGross.add(p.getGrossPay());
            }
            if (p.getTotalDeductions() != null) {
                totalDeductions = totalDeductions.add(p.getTotalDeductions());
            }
            if (p.getNetPay() != null) {
                totalNet = totalNet.add(p.getNetPay());
            }
        }

        return new PayrollRun(
                id,
                organizationId,
                period,
                payslipList,
                totalGross,
                totalDeductions,
                totalNet,
                PayrollStatus.DRAFT,
                initiatorUserId,
                null,
                null,
                ZonedDateTime.now()
        );
    }

    public void submitForApproval(Long initiatorId) {
        if (this.status != PayrollStatus.DRAFT) {
            throw new InvalidPayrollStateException("Payroll is not in DRAFT state — current status: " + this.status);
        }
        this.status = PayrollStatus.PENDING_APPROVAL;
        registerEvent(PayrollPendingApprovalEvent.of(this.id, this.organizationId, this.period.toString(), initiatorId));
    }

    public void approve(Long approverId) {
        if (this.status != PayrollStatus.PENDING_APPROVAL) {
            throw new InvalidPayrollStateException("Payroll is not pending approval — current status: " + this.status);
        }
        if (approverId != null && approverId.equals(this.initiatedBy)) {
            throw new SelfApprovalNotAllowedException("The approver cannot be the same person who initiated the payroll run");
        }
        this.approvedBy = approverId;
        this.approvedAt = ZonedDateTime.now();
        this.status = PayrollStatus.APPROVED;

        BigDecimal netAmount = this.totalNet != null ? this.totalNet.amount() : BigDecimal.ZERO;
        String currency = this.totalNet != null ? this.totalNet.currency().name() : CurrencyCode.NGN.name();

        registerEvent(PayrollApprovedEvent.of(
                this.id,
                this.organizationId,
                this.period.toString(),
                netAmount,
                currency,
                approverId
        ));
    }

    public void markProcessing() {
        if (this.status != PayrollStatus.APPROVED) {
            throw new InvalidPayrollStateException("Payroll must be in APPROVED state to process — current status: " + this.status);
        }
        this.status = PayrollStatus.PROCESSING;
    }

    public void markDisbursed() {
        if (this.status != PayrollStatus.PROCESSING) {
            throw new InvalidPayrollStateException("Payroll must be in PROCESSING state to disburse — current status: " + this.status);
        }
        this.status = PayrollStatus.DISBURSED;
        for (Payslip payslip : this.payslips) {
            payslip.markDisbursed();
        }

        BigDecimal grossAmount = this.totalGross != null ? this.totalGross.amount() : BigDecimal.ZERO;
        BigDecimal deductionAmount = this.totalDeductions != null ? this.totalDeductions.amount() : BigDecimal.ZERO;
        BigDecimal netAmount = this.totalNet != null ? this.totalNet.amount() : BigDecimal.ZERO;
        String currency = this.totalNet != null ? this.totalNet.currency().name() : CurrencyCode.NGN.name();

        registerEvent(PayrollDisbursedEvent.of(
                this.id,
                this.organizationId,
                this.period.toString(),
                grossAmount,
                deductionAmount,
                netAmount,
                this.payslips.size(),
                currency,
                ZonedDateTime.now()
        ));
    }

    public void revertToApproved(String reason) {
        if (this.status == PayrollStatus.PROCESSING) {
            this.status = PayrollStatus.APPROVED;
        }
    }

    public void markFailed(String reason) {
        this.status = PayrollStatus.FAILED;
        for (Payslip payslip : this.payslips) {
            payslip.markFailed();
        }
    }

    public void expire() {
        if (this.status == PayrollStatus.PENDING_APPROVAL) {
            this.status = PayrollStatus.DRAFT;
            registerEvent(PayrollApprovalExpiredEvent.of(this.id, this.organizationId, this.period.toString(), this.initiatedBy));
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
