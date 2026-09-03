package com.atlashub.accounts.application.command;

import com.atlashub.shared.application.usecase.Command;

public record RequestClosureCommand(String accountId) implements Command {
}
