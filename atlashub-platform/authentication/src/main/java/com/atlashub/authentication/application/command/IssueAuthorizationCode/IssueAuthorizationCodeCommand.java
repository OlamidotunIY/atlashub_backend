package com.atlashub.authentication.application.command.IssueAuthorizationCode;

public record IssueAuthorizationCodeCommand(
        String clientId,
        String redirectUri,
        String responseType,
        String scope,
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        String nonce,
        Long userId
) {
}
