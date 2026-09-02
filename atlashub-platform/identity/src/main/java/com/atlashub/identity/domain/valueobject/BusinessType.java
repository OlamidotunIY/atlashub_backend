package com.atlashub.identity.domain.valueobject;

import lombok.Getter;

/**
 * Indicates whether a Organization is an informal/unregistered business (STARTER)
 * or a CAC-registered business (REGISTERED).
 * REGISTERED Organizations must supply an rcNumber before completing the OWNER compliance step.
 */
@Getter
public enum BusinessType {

    RETAIL("Retail & E-commerce"),
    FINANCE("Finance & Fintech"),
    EDUCATION("Education"),
    HEALTHCARE("Healthcare"),
    HOSPITALITY("Hospitality"),
    LOGISTICS("Logistics & Transportation"),
    REAL_ESTATE("Real Estate"),
    OTHER("Other");

    private final String displayName;

    BusinessType(String displayName) {
        this.displayName = displayName;
    }

}
