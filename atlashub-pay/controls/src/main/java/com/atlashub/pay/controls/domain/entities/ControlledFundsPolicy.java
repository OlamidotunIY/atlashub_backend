package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.events.ControlledFundsPolicyActivatedEvent;
import com.atlashub.pay.controls.domain.exceptions.InvalidPolicyException;
import com.atlashub.pay.controls.domain.exceptions.PolicyNotActiveException;
import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
public class ControlledFundsPolicy extends AggregateRoot<Long> {

    private final Long id;
    private final Long facilityId;
    private final Long organizationId;
    private final Environment environment;
    private final String policyVersion;
    private ControlMode mode;
    private Set<PermittedCategory> permittedCategories;
    private Set<RestrictedCategory> restrictedCategories;
    private BeneficiaryRuleSet beneficiaryRules;
    private WithdrawalRuleSet withdrawalRules;
    private AmountFrequencyRuleSet amountAndFrequencyRules;
    private Set<ProviderControlCapability> providerCapabilities;
    private PolicyStatus status;
    private ZonedDateTime effectiveFrom;
    private ZonedDateTime effectiveUntil;
    private String activatedBy;

    public ControlledFundsPolicy(Long id, Long facilityId, Long organizationId, Environment environment,
                                 String policyVersion, ControlMode mode,
                                 Set<PermittedCategory> permittedCategories,
                                 Set<RestrictedCategory> restrictedCategories,
                                 BeneficiaryRuleSet beneficiaryRules,
                                 WithdrawalRuleSet withdrawalRules,
                                 AmountFrequencyRuleSet amountAndFrequencyRules,
                                 Set<ProviderControlCapability> providerCapabilities,
                                 PolicyStatus status, ZonedDateTime effectiveFrom,
                                 ZonedDateTime effectiveUntil, String activatedBy) {
        this.id = id;
        this.facilityId = facilityId;
        this.organizationId = organizationId;
        this.environment = environment;
        this.policyVersion = policyVersion;
        this.mode = mode;
        this.permittedCategories = permittedCategories != null ? new HashSet<>(permittedCategories) : new HashSet<>();
        this.restrictedCategories = restrictedCategories != null ? new HashSet<>(restrictedCategories) : new HashSet<>();
        this.beneficiaryRules = beneficiaryRules;
        this.withdrawalRules = withdrawalRules;
        this.amountAndFrequencyRules = amountAndFrequencyRules;
        this.providerCapabilities = providerCapabilities != null ? new HashSet<>(providerCapabilities) : new HashSet<>();
        this.status = status;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.activatedBy = activatedBy;
    }

    public static ControlledFundsPolicy create(Long id, Long facilityId, Long organizationId, Environment environment,
                                               String policyVersion, ControlMode mode,
                                               Set<PermittedCategory> permittedCategories,
                                               Set<RestrictedCategory> restrictedCategories,
                                               BeneficiaryRuleSet beneficiaryRules,
                                               WithdrawalRuleSet withdrawalRules,
                                               AmountFrequencyRuleSet amountAndFrequencyRules,
                                               Set<ProviderControlCapability> providerCapabilities,
                                               ZonedDateTime effectiveFrom, ZonedDateTime effectiveUntil) {
        if (id == null) {
            throw new InvalidPolicyException("Policy ID must not be null");
        }
        if (facilityId == null) {
            throw new InvalidPolicyException("Facility ID must not be null");
        }
        if (organizationId == null) {
            throw new InvalidPolicyException("Organization ID must not be null");
        }
        if (policyVersion == null || policyVersion.isBlank()) {
            throw new InvalidPolicyException("Policy version must not be blank");
        }
        if (mode == null) {
            throw new InvalidPolicyException("Control mode must not be null");
        }
        if (effectiveFrom == null) {
            effectiveFrom = ZonedDateTime.now();
        }

        return new ControlledFundsPolicy(
                id,
                facilityId,
                organizationId,
                environment != null ? environment : Environment.LIVE,
                policyVersion,
                mode,
                permittedCategories != null ? permittedCategories : Collections.emptySet(),
                restrictedCategories != null ? restrictedCategories : Collections.emptySet(),
                beneficiaryRules,
                withdrawalRules,
                amountAndFrequencyRules,
                providerCapabilities != null ? providerCapabilities : Collections.emptySet(),
                PolicyStatus.DRAFT,
                effectiveFrom,
                effectiveUntil,
                null
        );
    }

    public void activate(String activatedBy) {
        if (activatedBy == null || activatedBy.isBlank()) {
            throw new InvalidPolicyException("ActivatedBy identity is required");
        }
        this.status = PolicyStatus.ACTIVE;
        this.activatedBy = activatedBy;
        this.effectiveFrom = ZonedDateTime.now();

        registerEvent(new ControlledFundsPolicyActivatedEvent(
                UUID.randomUUID().toString(),
                this.id,
                ZonedDateTime.now(),
                UUID.randomUUID().toString(),
                new ControlledFundsPolicyActivatedEvent.Payload(
                        this.id,
                        this.facilityId,
                        this.organizationId,
                        this.policyVersion,
                        this.mode.name(),
                        this.effectiveFrom,
                        this.activatedBy
                )
        ));
    }

    public void suspend(String reason) {
        if (this.status != PolicyStatus.ACTIVE) {
            throw new PolicyNotActiveException("Only an active policy can be suspended");
        }
        this.status = PolicyStatus.SUSPENDED;
    }

    public void replaceRules(BeneficiaryRuleSet beneficiaryRules,
                             WithdrawalRuleSet withdrawalRules,
                             AmountFrequencyRuleSet amountAndFrequencyRules,
                             Set<PermittedCategory> permittedCategories,
                             Set<RestrictedCategory> restrictedCategories) {
        this.beneficiaryRules = beneficiaryRules;
        this.withdrawalRules = withdrawalRules;
        this.amountAndFrequencyRules = amountAndFrequencyRules;
        if (permittedCategories != null) {
            this.permittedCategories = new HashSet<>(permittedCategories);
        }
        if (restrictedCategories != null) {
            this.restrictedCategories = new HashSet<>(restrictedCategories);
        }
    }

    public void expire() {
        this.status = PolicyStatus.EXPIRED;
        this.effectiveUntil = ZonedDateTime.now();
    }

    public boolean supports(ControlAction action, PaymentRail rail) {
        if (action == ControlAction.ALLOW || action == ControlAction.REVIEW) {
            return true;
        }
        if (rail == PaymentRail.INTERNAL_LEDGER) {
            return true;
        }
        if (rail == PaymentRail.EXTERNAL_ACCOUNT) {
            return false;
        }
        if (action == ControlAction.HOLD) {
            return this.providerCapabilities.contains(ProviderControlCapability.HOLD_INSTRUCTION);
        }
        if (action == ControlAction.BLOCK) {
            return this.providerCapabilities.contains(ProviderControlCapability.BLOCK_INSTRUCTION);
        }
        if (action == ControlAction.FREEZE) {
            return this.providerCapabilities.contains(ProviderControlCapability.FREEZE_FUNDS);
        }
        return false;
    }

    public boolean isEffective(ZonedDateTime time) {
        if (status != PolicyStatus.ACTIVE) {
            return false;
        }
        if (time.isBefore(effectiveFrom)) {
            return false;
        }
        return effectiveUntil == null || !time.isAfter(effectiveUntil);
    }

    @Override
    public Long getId() {
        return id;
    }
}
