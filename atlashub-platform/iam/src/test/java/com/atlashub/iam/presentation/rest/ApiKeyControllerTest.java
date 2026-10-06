package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.commands.IssueApiKey.IssueApiKeyHandler;
import com.atlashub.iam.application.commands.IssueApiKey.IssuedApiKeyResult;
import com.atlashub.iam.application.commands.RevokeApiKey.RevokeApiKeyHandler;
import com.atlashub.iam.application.commands.RotateApiKey.RotateApiKeyCommand;
import com.atlashub.iam.application.commands.RotateApiKey.RotateApiKeyHandler;
import com.atlashub.iam.application.queries.ListApiKeys.ListApiKeysHandler;
import com.atlashub.iam.presentation.dto.RotateApiKeyRequest;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ApiKeyControllerTest {
    @Test
    void rotation_uses_active_organization_and_authenticated_user() {
        IssueApiKeyHandler issue = mock(IssueApiKeyHandler.class);
        RevokeApiKeyHandler revoke = mock(RevokeApiKeyHandler.class);
        ListApiKeysHandler list = mock(ListApiKeysHandler.class);
        RotateApiKeyHandler rotate = mock(RotateApiKeyHandler.class);
        var expected = new IssuedApiKeyResult("public", "secret", "TEST");
        var command = new RotateApiKeyCommand(5L, 20L, 10L, "Rotated");
        when(rotate.execute(command)).thenReturn(expected);
        var principal = new AuthenticatedPrincipal(10L, 20L, "TEST", "session", "token", ZonedDateTime.now());

        var response = new ApiKeyController(issue, revoke, list, rotate)
                .rotate(principal, 5L, new RotateApiKeyRequest("Rotated"));

        assertEquals(expected, response.getBody().data());
        verify(rotate).execute(command);
    }
}
