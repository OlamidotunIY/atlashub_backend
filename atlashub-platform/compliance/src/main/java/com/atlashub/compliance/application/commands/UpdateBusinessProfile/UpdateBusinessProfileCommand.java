package com.atlashub.compliance.application.commands.UpdateBusinessProfile;

public record UpdateBusinessProfileCommand(
    Long organizationId,
    String businessRegistrationNumber,
    String businessBvn,
    String businessDescription,
    String website
) {
}
