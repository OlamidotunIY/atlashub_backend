package com.atlashub.iam.application.commands.InitializeOrganizationIam;

public record InitializeOrganizationIamCommand(
        Long orgId,
        Long foundingUserId
) {
}
