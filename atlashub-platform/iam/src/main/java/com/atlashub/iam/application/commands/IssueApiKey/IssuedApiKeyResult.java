package com.atlashub.iam.application.commands.IssueApiKey;

public record IssuedApiKeyResult(String publicKey, String secretKey, String environment) {
}
