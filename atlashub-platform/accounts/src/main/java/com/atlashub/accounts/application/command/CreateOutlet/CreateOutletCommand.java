package com.atlashub.accounts.application.command.CreateOutlet;

public record CreateOutletCommand(
        Long organizationId,
        String name,
        String address,
        String city,
        String state,
        String country,
        Long managerId
) {}
