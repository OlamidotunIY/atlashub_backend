package com.atlashub.authentication.application.command.AuthAccount;

public record AuthAccountCommand(Long userId, String email, String passwordHash) {
}
