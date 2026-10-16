package com.atlashub.pay.mandates.application.commands.RevokeMandate;

public record RevokeMandateCommand(
        Long mandateId,
        Long organizationId
) {}
