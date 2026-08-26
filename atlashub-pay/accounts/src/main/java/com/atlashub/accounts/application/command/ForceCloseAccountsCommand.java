package com.atlashub.accounts.application.command;

import com.atlashub.shared.usecase.Command;

public record ForceCloseAccountsCommand(Long integration) implements Command {
}
