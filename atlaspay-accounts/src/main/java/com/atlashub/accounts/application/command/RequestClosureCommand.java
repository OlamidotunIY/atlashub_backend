package com.atlashub.accounts.application.command;

import com.atlashub.shared.usecase.Command;

public record RequestClosureCommand(String accountId) implements Command {
}
