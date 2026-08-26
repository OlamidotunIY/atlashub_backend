package com.atlashub.accounts.application.command;

import com.atlashub.shared.usecase.Command;

public record ActivateVirtualAccountCommand(String referenceId, String nuban) implements Command {
}
