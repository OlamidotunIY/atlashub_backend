package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.queries.ListPermissions.ListPermissionsHandler;
import com.atlashub.iam.application.queries.ListPermissions.ListPermissionsQuery;
import com.atlashub.iam.application.queries.ListPermissions.PermissionResult;
import com.atlashub.shared.application.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/iam/permissions")
public class PermissionController {
    private final ListPermissionsHandler listPermissionsHandler;

    public PermissionController(ListPermissionsHandler listPermissionsHandler) {
        this.listPermissionsHandler = listPermissionsHandler;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PermissionResult>>> list(
            @RequestParam(required = false) String module) {
        return ok(listPermissionsHandler.execute(new ListPermissionsQuery(module)));
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
