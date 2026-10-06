package com.atlashub.iam.presentation.rest;

import com.atlashub.iam.application.commands.CreateCustomRole.CreateCustomRoleCommand;
import com.atlashub.iam.application.commands.CreateCustomRole.CreateCustomRoleHandler;
import com.atlashub.iam.application.commands.DeleteCustomRole.DeleteCustomRoleCommand;
import com.atlashub.iam.application.commands.DeleteCustomRole.DeleteCustomRoleHandler;
import com.atlashub.iam.application.commands.UpdateCustomRole.UpdateCustomRoleCommand;
import com.atlashub.iam.application.commands.UpdateCustomRole.UpdateCustomRoleHandler;
import com.atlashub.iam.application.queries.GetCustomRolePermissions.CustomRolePermissionsResult;
import com.atlashub.iam.application.queries.GetCustomRolePermissions.GetCustomRolePermissionsHandler;
import com.atlashub.iam.application.queries.GetCustomRolePermissions.GetCustomRolePermissionsQuery;
import com.atlashub.iam.application.queries.ListCustomRoles.CustomRoleResult;
import com.atlashub.iam.application.queries.ListCustomRoles.ListCustomRolesHandler;
import com.atlashub.iam.application.queries.ListCustomRoles.ListCustomRolesQuery;
import com.atlashub.iam.presentation.dto.RoleRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@Tag(name = "Custom Roles", description = "Organization role and permission management")
public class CustomRoleController {
    private final ListCustomRolesHandler listHandler;
    private final GetCustomRolePermissionsHandler permissionsHandler;
    private final CreateCustomRoleHandler createHandler;
    private final UpdateCustomRoleHandler updateHandler;
    private final DeleteCustomRoleHandler deleteHandler;

    public CustomRoleController(ListCustomRolesHandler listHandler,
                                GetCustomRolePermissionsHandler permissionsHandler,
                                CreateCustomRoleHandler createHandler,
                                UpdateCustomRoleHandler updateHandler,
                                DeleteCustomRoleHandler deleteHandler) {
        this.listHandler = listHandler;
        this.permissionsHandler = permissionsHandler;
        this.createHandler = createHandler;
        this.updateHandler = updateHandler;
        this.deleteHandler = deleteHandler;
    }

    @GetMapping
    @Operation(summary = "List custom roles")
    public ResponseEntity<ApiResponse<List<CustomRoleResult>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ok(listHandler.execute(new ListCustomRolesQuery(principal.activeOrganizationId())));
    }

    @GetMapping("/{id}/permissions")
    @Operation(summary = "Get role permissions")
    public ResponseEntity<ApiResponse<CustomRolePermissionsResult>> permissions(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        return ok(permissionsHandler.execute(
                new GetCustomRolePermissionsQuery(id, principal.activeOrganizationId())));
    }

    @PostMapping
    @Operation(summary = "Create a custom role")
    public ResponseEntity<ApiResponse<Void>> create(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RoleRequest request) {
        createHandler.execute(new CreateCustomRoleCommand(
                principal.activeOrganizationId(), request.name(), request.description(),
                request.permissionIds(), principal.userId()));
        return done("Role created");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a custom role")
    public ResponseEntity<ApiResponse<Void>> update(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {
        updateHandler.execute(new UpdateCustomRoleCommand(
                id, principal.activeOrganizationId(), request.name(), request.description(),
                request.permissionIds()));
        return done("Role updated");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a custom role")
    public ResponseEntity<ApiResponse<Void>> delete(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id) {
        deleteHandler.execute(new DeleteCustomRoleCommand(
                id, principal.userId(), principal.activeOrganizationId()));
        return done("Role deleted");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
