package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;

@Getter
public class HeadcountProjection extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final String environment;
    private int total;
    private int active;
    private int onLeave;
    private int suspended;
    private int terminated;
    private int newHiresThisMonth;
    private ZonedDateTime updatedAt;

    public HeadcountProjection(
            Long id,
            Long organizationId,
            String environment,
            int total,
            int active,
            int onLeave,
            int suspended,
            int terminated,
            int newHiresThisMonth,
            ZonedDateTime updatedAt
    ) {
        this.id = id;
        this.organizationId = organizationId;
        this.environment = environment;
        this.total = total;
        this.active = active;
        this.onLeave = onLeave;
        this.suspended = suspended;
        this.terminated = terminated;
        this.newHiresThisMonth = newHiresThisMonth;
        this.updatedAt = updatedAt;
    }

    public static HeadcountProjection create(
            Long id,
            Long organizationId,
            String environment
    ) {
        if (organizationId == null) {
            throw new AnalyticsInvariantException("Organization ID must not be null");
        }
        return new HeadcountProjection(
                id,
                organizationId,
                environment != null ? environment : "live",
                0,
                0,
                0,
                0,
                0,
                0,
                ZonedDateTime.now()
        );
    }

    public void applyEmployeeChange(String changeType) {
        if ("HIRED".equalsIgnoreCase(changeType)) {
            this.total += 1;
            this.active += 1;
            this.newHiresThisMonth += 1;
        } else if ("TERMINATED".equalsIgnoreCase(changeType)) {
            this.active = Math.max(0, this.active - 1);
            this.terminated += 1;
        } else if ("SUSPENDED".equalsIgnoreCase(changeType)) {
            this.active = Math.max(0, this.active - 1);
            this.suspended += 1;
        } else if ("ON_LEAVE".equalsIgnoreCase(changeType)) {
            this.active = Math.max(0, this.active - 1);
            this.onLeave += 1;
        } else if ("RETURNED".equalsIgnoreCase(changeType)) {
            this.onLeave = Math.max(0, this.onLeave - 1);
            this.active += 1;
        }
        this.updatedAt = ZonedDateTime.now();
    }

    @Override
    public Long getId() {
        return id;
    }
}
