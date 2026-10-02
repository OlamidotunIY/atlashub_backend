package com.atlashub.pay.splits.domain.entities;

import com.atlashub.pay.splits.domain.exceptions.InvalidSplitPercentagesException;
import com.atlashub.pay.splits.domain.exceptions.MissingSubaccountException;
import com.atlashub.pay.splits.domain.exceptions.SplitRuleInactiveException;
import com.atlashub.pay.splits.domain.valueobject.SplitType;
import com.atlashub.shared.domain.entities.AggregateRoot;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class SplitRule extends AggregateRoot<Long> {
    private final Long id;
    private final Long organizationId;
    private String name;
    private SplitType type;
    private BigDecimal platformFeePercentage;
    private List<SplitSubaccount> subaccounts;
    private boolean active;
    private final ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;

    public SplitRule(Long id, Long organizationId, String name, SplitType type, BigDecimal platformFeePercentage, List<SplitSubaccount> subaccounts, boolean active, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.platformFeePercentage = platformFeePercentage != null ? platformFeePercentage : BigDecimal.ZERO;
        this.subaccounts = subaccounts != null ? new ArrayList<>(subaccounts) : new ArrayList<>();
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        validateInvariants();
    }

    public static SplitRule create(Long id, Long organizationId, String name, SplitType type, BigDecimal platformFeePercentage, List<SplitSubaccount> subaccounts) {
        SplitRule rule = new SplitRule(id, organizationId, name, type, platformFeePercentage, subaccounts, true, ZonedDateTime.now(), ZonedDateTime.now());
        return rule;
    }

    public void update(String name, SplitType type, BigDecimal platformFeePercentage, List<SplitSubaccount> subaccounts) {
        if (!this.active) {
            throw new SplitRuleInactiveException();
        }
        this.name = name;
        this.type = type;
        this.platformFeePercentage = platformFeePercentage != null ? platformFeePercentage : BigDecimal.ZERO;
        this.subaccounts = new ArrayList<>(subaccounts);
        this.updatedAt = ZonedDateTime.now();
        validateInvariants();
    }

    public void deactivate() {
        if (!this.active) {
            throw new SplitRuleInactiveException();
        }
        this.active = false;
        this.updatedAt = ZonedDateTime.now();
    }

    public void addSubaccount(SplitSubaccount subaccount) {
        if (!this.active) {
            throw new SplitRuleInactiveException();
        }
        this.subaccounts.add(subaccount);
        this.updatedAt = ZonedDateTime.now();
        validateInvariants();
    }

    public void removeSubaccount(Long subaccountId) {
        if (!this.active) {
            throw new SplitRuleInactiveException();
        }
        boolean removed = this.subaccounts.removeIf(sub -> sub.getId() != null && sub.getId().equals(subaccountId));
        if (removed) {
            this.updatedAt = ZonedDateTime.now();
            validateInvariants();
        }
    }

    private void validateInvariants() {
        if (this.subaccounts == null || this.subaccounts.isEmpty()) {
            throw new MissingSubaccountException();
        }
        if (this.type == SplitType.PERCENTAGE) {
            BigDecimal sum = this.platformFeePercentage;
            for (SplitSubaccount sub : this.subaccounts) {
                sum = sum.add(sub.getShare() != null ? sub.getShare() : BigDecimal.ZERO);
            }
            if (sum.compareTo(new BigDecimal("100")) != 0) {
                throw new InvalidSplitPercentagesException();
            }
        }
    }

    @Override
    public Long getId() {
        return id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public String getName() {
        return name;
    }

    public SplitType getType() {
        return type;
    }

    public BigDecimal getPlatformFeePercentage() {
        return platformFeePercentage;
    }

    public List<SplitSubaccount> getSubaccounts() {
        return new ArrayList<>(subaccounts);
    }

    public boolean isActive() {
        return active;
    }

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }
}
