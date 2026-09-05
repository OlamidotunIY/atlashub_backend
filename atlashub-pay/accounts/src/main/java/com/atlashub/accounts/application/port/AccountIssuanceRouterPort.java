package com.atlashub.accounts.application.port;

public interface AccountIssuanceRouterPort {
    AccountIssuancePort resolve(Long organizationId);
}
