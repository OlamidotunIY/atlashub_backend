package com.atlashub.compliance.application.commands.UpdateContactInfo;

import com.atlashub.compliance.domain.valueobject.ContactInfoData;

public record UpdateContactInfoCommand(
    Long organizationId,
    ContactInfoData data
) {
}
