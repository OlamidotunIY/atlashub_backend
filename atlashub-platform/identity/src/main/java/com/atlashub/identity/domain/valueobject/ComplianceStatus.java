package com.atlashub.identity.domain.valueobject;

/**
 * The Organization's compliance verification status.
 * Transitions are strictly one-way and ordered.
 *
 * NOT_STARTED → IN_PROGRESS → SUBMITTED → UNDER_REVIEW → APPROVED
 *                                                       ↘ REJECTED
 *
 * APPROVED is the terminal success state that unlocks LIVE mode API keys.
 */
public enum ComplianceStatus {
    NOT_STARTED,
    IN_PROGRESS,
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED
}
