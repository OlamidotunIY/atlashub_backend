package com.atlashub.pay.controls.domain.valueobject;

public record EvidenceRequirement(
        String requirementType,
        String description,
        boolean mandatory
) {}
