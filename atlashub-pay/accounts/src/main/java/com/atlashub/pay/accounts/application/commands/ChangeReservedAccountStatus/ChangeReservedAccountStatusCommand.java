package com.atlashub.pay.accounts.application.commands.ChangeReservedAccountStatus;

public record ChangeReservedAccountStatusCommand(Long organizationId, Long reservedAccountId, Action action) {
    public enum Action { SUSPEND, REACTIVATE, CLOSE }
}
