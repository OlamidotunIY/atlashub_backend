package com.atlashub.accounts.application.command;

import com.atlashub.shared.application.usecase.Command;

public record ForceCloseAccountsCommand(Long integration) implements Command {
}
