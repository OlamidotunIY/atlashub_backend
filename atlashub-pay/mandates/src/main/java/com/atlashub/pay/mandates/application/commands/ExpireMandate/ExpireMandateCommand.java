package com.atlashub.pay.mandates.application.commands.ExpireMandate;

public record ExpireMandateCommand(Long mandateId, String reason) {}
