package com.atlashub.compliance.application.commands.UpdateBusinessProfile;

import com.atlashub.compliance.domain.valueobject.BusinessProfileData;

public record UpdateBusinessProfileCommand(
    Long organizationId,
    BusinessProfileData data
) {
}
