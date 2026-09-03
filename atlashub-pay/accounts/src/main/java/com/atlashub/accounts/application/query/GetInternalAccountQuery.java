package com.atlashub.accounts.application.query;

import com.atlashub.accounts.domain.valueobject.InternalAccountType;
import com.atlashub.shared.application.usecase.Query;

public record GetInternalAccountQuery(Long organizationId, InternalAccountType accountType) implements Query {}