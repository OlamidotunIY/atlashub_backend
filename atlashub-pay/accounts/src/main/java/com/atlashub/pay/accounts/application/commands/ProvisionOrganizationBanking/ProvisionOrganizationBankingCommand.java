package com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking;

public record ProvisionOrganizationBankingCommand(Long organizationId, String anchorBusinessCustomerId) {
}
