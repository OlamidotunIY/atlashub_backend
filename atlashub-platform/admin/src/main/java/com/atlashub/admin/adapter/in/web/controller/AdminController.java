package com.atlashub.admin.adapter.in.web.controller;

import com.atlashub.admin.application.command.BootstrapMasterAdminCommand;
import com.atlashub.admin.application.command.CreateAdminCommand;
import com.atlashub.admin.application.result.AdminCreationResult;
import com.atlashub.admin.application.usecase.BootstrapMasterAdminUseCase;
import com.atlashub.admin.application.usecase.CreateAdminUseCase;
import com.atlashub.admin.domain.valueobject.AdminPermission;
import com.atlashub.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Set;
import com.atlashub.admin.adapter.in.web.request.BootstrapRequestDto;
import com.atlashub.admin.adapter.in.web.request.CreateAdminRequestDto;

@RestController
@RequestMapping("/api/v1/admins")
@Tag(name = "Admins", description = "Admin management endpoints")
public class AdminController {

    private final BootstrapMasterAdminUseCase bootstrapMasterAdminUseCase;
    private final CreateAdminUseCase createAdminUseCase;

    public AdminController(BootstrapMasterAdminUseCase bootstrapMasterAdminUseCase,
                           CreateAdminUseCase createAdminUseCase) {
        this.bootstrapMasterAdminUseCase = bootstrapMasterAdminUseCase;
        this.createAdminUseCase = createAdminUseCase;
    }

    @PostMapping("/auth/bootstrap")
    @Operation(summary = "Bootstrap Master Admin", description = "Creates the first master admin. Can only be run once.")
    public ResponseEntity<ApiResponse<AdminCreationResult>> bootstrap(@Valid @RequestBody BootstrapRequestDto request) {
        BootstrapMasterAdminCommand command = new BootstrapMasterAdminCommand(request.username());
        AdminCreationResult result = bootstrapMasterAdminUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Master admin created successfully", result, null));
    }

    @PostMapping
    @Operation(summary = "Create an admin", description = "Creates a new standard admin. Requires MANAGE_ADMINS permission.")
    public ResponseEntity<ApiResponse<AdminCreationResult>> createAdmin(@Valid @RequestBody CreateAdminRequestDto request, Principal principal) {
        Long requestingAdminId = Long.valueOf(principal.getName());
        CreateAdminCommand command = new CreateAdminCommand(
                requestingAdminId,
                request.username(),
                request.destinationEmail(),
                request.permissions()
        );
        AdminCreationResult result = createAdminUseCase.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Admin created successfully", result, null));
    }
}


