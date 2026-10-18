package com.atlashub.pay.mandates.application.commands.ResumeMandate;

public record ResumeMandateCommand(
        Long mandateId,
        Long organizationId
) {}
