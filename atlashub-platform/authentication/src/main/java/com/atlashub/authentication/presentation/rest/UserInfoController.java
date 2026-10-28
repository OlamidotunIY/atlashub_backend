package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.query.GetUserInfo.GetUserInfoHandler;
import com.atlashub.authentication.application.query.GetUserInfo.GetUserInfoQuery;
import com.atlashub.authentication.application.query.GetUserInfo.GetUserInfoResult;
import com.atlashub.authentication.presentation.dto.UserInfoResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/oauth2")
@Tag(name = "OIDC UserInfo", description = "OpenID Connect UserInfo Endpoint")
public class UserInfoController {

    private final GetUserInfoHandler handler;

    public UserInfoController(GetUserInfoHandler handler) {
        this.handler = handler;
    }

    @GetMapping("/userinfo")
    @Operation(summary = "Get OpenID Connect UserInfo claims for authenticated user")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserInfo(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.userId() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Authentication required", null, null));
        }

        GetUserInfoResult result = handler.execute(new GetUserInfoQuery(principal.userId()));

        UserInfoResponse response = new UserInfoResponse(
                result.sub(),
                result.email(),
                result.emailVerified(),
                result.name(),
                result.givenName(),
                result.familyName(),
                result.activeOrganizationId(),
                result.environment()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "User info retrieved", response, null));
    }
}
