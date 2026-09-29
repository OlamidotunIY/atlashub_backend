package com.atlashub.compliance.application.commands.UpdateOwnerIdentity;

import com.atlashub.compliance.domain.valueobject.OwnerIdentityData;

public record UpdateOwnerIdentityCommand(
    Long organizationId,
    OwnerIdentityData data
) {
}
