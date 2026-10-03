package com.atlashub.pay.accounts.application.commands.ChangeReservedAccountStatus;

public record ChangeReservedAccountStatusCommand(Long organizationId, String environment, Long reservedAccountId, Action action) {
    public enum Action { SUSPEND, REACTIVATE, CLOSE }
}
