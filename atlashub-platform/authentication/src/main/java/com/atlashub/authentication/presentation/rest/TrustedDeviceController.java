package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.AuthorizeDevice.AuthorizeDeviceCommand;
import com.atlashub.authentication.application.command.AuthorizeDevice.AuthorizeDeviceHandler;
import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceCommand;
import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceHandler;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesHandler;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesQuery;
import com.atlashub.authentication.presentation.dto.AuthorizeDeviceRequest;
import com.atlashub.authentication.presentation.dto.TrustedDeviceWebResponse;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class TrustedDeviceController {
    private final AuthorizeDeviceHandler authorizeHandler;
    private final RevokeTrustedDeviceHandler revokeHandler;
    private final GetTrustedDevicesHandler listHandler;

    public TrustedDeviceController(AuthorizeDeviceHandler authorizeHandler,
                                   RevokeTrustedDeviceHandler revokeHandler,
                                   GetTrustedDevicesHandler listHandler) {
        this.authorizeHandler = authorizeHandler;
        this.revokeHandler = revokeHandler;
        this.listHandler = listHandler;
    }

    @GetMapping
    @PreAuthorize("principal.userId() != null")
    public ResponseEntity<ApiResponse<List<TrustedDeviceWebResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<TrustedDeviceWebResponse> devices = listHandler
                .execute(new GetTrustedDevicesQuery(principal.userId())).stream()
                .map(device -> new TrustedDeviceWebResponse(
                        device.id(), device.deviceFingerprint(), device.deviceName(),
                        device.lastSeenIp(), device.trustedAt(), device.expiresAt()))
                .toList();
        return ok(devices);
    }

    @DeleteMapping
    @PreAuthorize("principal.userId() != null")
    public ResponseEntity<ApiResponse<Void>> revoke(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long id) {
        revokeHandler.execute(new RevokeTrustedDeviceCommand(principal.userId(), id));
        return done("Device revoked");
    }

    @PublicEndpoint
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<Void>> verify(
            @Valid @RequestBody AuthorizeDeviceRequest request,
            HttpServletRequest httpRequest) {
        authorizeHandler.execute(new AuthorizeDeviceCommand(
                request.email(), request.otp(), request.deviceFingerprint(),
                httpRequest.getHeader("User-Agent"), clientIp(httpRequest)));
        return done("Device verified. Login can now be completed.");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
