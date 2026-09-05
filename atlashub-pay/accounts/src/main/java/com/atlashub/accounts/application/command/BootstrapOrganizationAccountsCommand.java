package com.atlashub.accounts.application.command;

import com.atlashub.shared.domain.money.CurrencyCode;

public record BootstrapOrganizationAccountsCommand(Long organizationId, CurrencyCode currency) {}
