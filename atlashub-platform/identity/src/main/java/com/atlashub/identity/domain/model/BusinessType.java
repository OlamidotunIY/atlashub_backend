package com.atlashub.identity.domain.model;

/**
 * Indicates whether a Organization is an informal/unregistered business (STARTER)
 * or a CAC-registered business (REGISTERED).
 * REGISTERED Organizations must supply an rcNumber before completing the OWNER compliance step.
 */
public enum BusinessType {
    STARTER,
    REGISTERED
}
