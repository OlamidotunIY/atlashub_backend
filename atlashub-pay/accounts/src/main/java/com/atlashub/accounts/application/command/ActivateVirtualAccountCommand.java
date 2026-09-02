package com.atlashub.accounts.application.command;

import com.atlashub.shared.application.usecase.Command;

public record ActivateVirtualAccountCommand(String referenceId, String nuban) implements Command {
}
