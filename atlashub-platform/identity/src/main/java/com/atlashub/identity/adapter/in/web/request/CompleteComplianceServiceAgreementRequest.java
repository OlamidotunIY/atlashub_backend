package com.atlashub.identity.adapter.in.web.request;

import jakarta.validation.constraints.AssertTrue;

public record CompleteComplianceServiceAgreementRequest(
    @AssertTrue(message = "You must agree to the terms")
    boolean agreed
) {}
