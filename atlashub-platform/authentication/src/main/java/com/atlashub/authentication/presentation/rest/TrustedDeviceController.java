package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceCommand;
import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceHandler;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesHandler;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesQuery;
import com.atlashub.authentication.presentation.dto.TrustedDeviceWebResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/devices")
public class TrustedDeviceController {
    private final RevokeTrustedDeviceHandler revokeHandler;
    private final GetTrustedDevicesHandler listHandler;

    public TrustedDeviceController(RevokeTrustedDeviceHandler revokeHandler,
                                   GetTrustedDevicesHandler listHandler) {
        this.revokeHandler = revokeHandler;
        this.listHandler = listHandler;
    }

    @GetMapping
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
    public ResponseEntity<ApiResponse<Void>> revoke(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long id) {
        revokeHandler.execute(new RevokeTrustedDeviceCommand(principal.userId(), id));
        return done("Device revoked");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
