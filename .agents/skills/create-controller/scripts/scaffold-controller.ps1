param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$ControllerName,
    [Parameter(Mandatory=$true)][string]$TagName
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\\main\\java\\com\\atlashub\\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$TargetDir  = Join-Path -Path $ModulePath -ChildPath "presentation\rest"

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "${ControllerName}Controller.java"

if (Test-Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

$Content = @"
package com.atlashub.$Module.presentation.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// TODO: Import DTOs from com.atlashub.$Module.presentation.dto.*
// TODO: Import Command/Query Handlers
// TODO: Import AuthenticatedUser or equivalent principal class
// TODO: Import @PublicEndpoint if any public endpoints exist

@RestController
@RequestMapping("/api/v1/${ControllerName.ToLower()}s")
@Tag(name = "$TagName")
public class ${ControllerName}Controller {

    // TODO: Inject Command/Query Handlers via constructor
    public ${ControllerName}Controller() {
    }

    // --- Public endpoint example (remove @SecurityRequirement, add @PublicEndpoint) ---
    // @Operation(summary = "List all ${ControllerName.ToLower()}s")
    // @ApiResponse(responseCode = "200", description = "OK")
    // @GetMapping
    // public ResponseEntity<?> listAll() {
    //     return ResponseEntity.ok(handler.execute(new ListQuery()));
    // }

    // --- Authenticated endpoint example ---
    // @Operation(summary = "Create a ${ControllerName.ToLower()}")
    // @ApiResponse(responseCode = "201", description = "Created")
    // @SecurityRequirement(name = "bearerAuth")
    // @PostMapping
    // public ResponseEntity<?> create(
    //         @AuthenticationPrincipal AuthenticatedUser user,
    //         @Valid @RequestBody ${ControllerName}Request request) {
    //     return ResponseEntity.status(201).body(handler.execute(new CreateCommand(...)));
    // }
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Controller created at $TargetFile"
