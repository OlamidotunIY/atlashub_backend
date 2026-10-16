package com.atlashub.pay.mandates.application.commands.PauseMandate;

public record PauseMandateCommand(
        Long mandateId,
        Long organizationId
) {}
