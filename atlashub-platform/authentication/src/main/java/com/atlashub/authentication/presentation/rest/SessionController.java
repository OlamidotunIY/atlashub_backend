package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsHandler;
import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsQuery;
import com.atlashub.authentication.presentation.dto.SessionWebResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {
    private final GetActiveSessionsHandler getActiveSessionsHandler;

    public SessionController(GetActiveSessionsHandler getActiveSessionsHandler) {
        this.getActiveSessionsHandler = getActiveSessionsHandler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<SessionWebResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<SessionWebResponse> sessions = getActiveSessionsHandler
                .execute(new GetActiveSessionsQuery(principal.userId())).stream()
                .map(session -> new SessionWebResponse(
                        session.id(), session.organizationId(), session.environment(),
                        session.expiresAt(), session.ipAddress(), session.userAgent()))
                .toList();
        return ok(sessions);
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
