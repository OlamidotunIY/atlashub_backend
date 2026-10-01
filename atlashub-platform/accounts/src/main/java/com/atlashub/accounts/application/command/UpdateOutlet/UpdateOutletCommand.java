package com.atlashub.accounts.application.command.UpdateOutlet;

public record UpdateOutletCommand(
        Long outletId,
        String name,
        String address,
        String city,
        String state,
        Long managerId
) {}
