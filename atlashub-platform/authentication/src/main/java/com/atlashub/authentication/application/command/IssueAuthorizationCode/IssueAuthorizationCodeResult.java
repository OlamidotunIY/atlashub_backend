package com.atlashub.authentication.application.command.IssueAuthorizationCode;

public record IssueAuthorizationCodeResult(
        String code,
        String redirectUri,
        String state
) {
}
