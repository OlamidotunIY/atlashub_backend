package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.queries.ListPermissions.ListPermissionsHandler;
import com.atlashub.iam.application.queries.ListPermissions.ListPermissionsQuery;
import com.atlashub.iam.application.queries.ListPermissions.PermissionResult;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@Tag(name = "Permissions", description = "Platform permission catalog")
public class PermissionController {
    private final ListPermissionsHandler listPermissionsHandler;

    public PermissionController(ListPermissionsHandler listPermissionsHandler) {
        this.listPermissionsHandler = listPermissionsHandler;
    }

    @GetMapping
    @Operation(summary = "List available permissions")
    public ResponseEntity<ApiResponse<List<PermissionResult>>> list(
            @RequestParam(required = false) String module) {
        return ok(listPermissionsHandler.execute(new ListPermissionsQuery(module)));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
