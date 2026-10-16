package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ControlEscalationService {

    public record EscalationOutcome(
            ControlAction suggestedAction,
            Set<ControlReason> reasons
    ) {}

    public EscalationOutcome evaluateAnomaly(ControlledFundsPolicy policy, String anomalySeverity) {
        Set<ControlReason> reasons = new HashSet<>();
        reasons.add(ControlReason.ANOMALY_DETECTED);

        if (anomalySeverity == null) {
            return new EscalationOutcome(ControlAction.REVIEW, reasons);
        }

        String severity = anomalySeverity.toUpperCase();
        ControlMode mode = policy.getMode();

        if ("CRITICAL".equals(severity)) {
            if (mode == ControlMode.PURPOSE_RESTRICTED || mode == ControlMode.HYBRID) {
                return new EscalationOutcome(ControlAction.FREEZE, reasons);
            }
            if (mode == ControlMode.MONITORED) {
                return new EscalationOutcome(ControlAction.HOLD, reasons);
            }
            return new EscalationOutcome(ControlAction.REVIEW, reasons);
        }

        if ("HIGH".equals(severity)) {
            if (mode == ControlMode.PURPOSE_RESTRICTED) {
                return new EscalationOutcome(ControlAction.HOLD, reasons);
            }
            return new EscalationOutcome(ControlAction.REVIEW, reasons);
        }

        if ("MEDIUM".equals(severity)) {
            return new EscalationOutcome(ControlAction.REVIEW, reasons);
        }

        // LOW or unknown
        return new EscalationOutcome(mode == ControlMode.UNRESTRICTED ? ControlAction.ALLOW : ControlAction.REVIEW, reasons);
    }
}
