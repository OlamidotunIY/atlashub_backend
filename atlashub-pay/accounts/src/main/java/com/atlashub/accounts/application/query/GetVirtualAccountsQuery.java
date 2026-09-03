package com.atlashub.accounts.application.query;

import com.atlashub.shared.application.usecase.Query;

public record GetVirtualAccountsQuery(Long integration) implements Query {
}
