package com.atlashub.pay.ledger.application.commands.CreateBusinessAccount;
public record CreateBusinessAccountCommand(Long organizationId,Long requestedByUserId,String environment,String name,String accountType,String currency){}
