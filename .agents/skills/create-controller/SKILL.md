---
name: create-controller
description: >-
  Use this skill to create a Spring REST controller in the presentation/rest/ layer. Controllers are thin — no business logic, delegate everything to Command/Query handlers.
---

# Create Controller

## Overview
Controllers live in `presentation/rest/` and are annotated `@RestController`. They are the entry point for HTTP requests and must be kept thin:
- No business logic.
- No direct repository access.
- All request/response data moves through DTO objects in `presentation/dto/`.
- Delegate all work to Command or Query Handlers.

## URL Conventions
- Base path: `/api/v1/<resource>`
- If the last segment is a resource ID, use it as a **query parameter** (not a path variable) when the endpoint is a single-resource lookup by ID — unless the ID is part of a nested resource (e.g., `/api/v1/organizations/{orgId}/members`).
- POST → create; PUT/PATCH → update; DELETE → remove; GET → read.

## Pre-Requisites
1. The relevant Command/Query Handlers must already exist (or be created via `create-application-command` / `create-application-query`).
2. Request/Response DTOs must already exist in `presentation/dto/` (or create via `create-dto`).
3. Confirm the Swagger `@Tag` name for this controller.

## Generation Mode

**Step 1: Scaffold skeleton**
```powershell
.\.agents\skills\create-controller\scripts\scaffold-controller.ps1 `
    -Module "<module>" `
    -ControllerName "<ControllerName>" `
    -TagName "<Swagger Tag Name>"
```

Example:
```powershell
.\.agents\skills\create-controller\scripts\scaffold-controller.ps1 `
    -Module "iam" `
    -ControllerName "CustomRole" `
    -TagName "Custom Roles"
```

Creates: `presentation/rest/CustomRoleController.java`

**Step 2: Inject endpoints**
Use `replace_file_content` to add the actual endpoint methods, injecting:
1. Required handler dependencies in the constructor.
2. Proper `@Operation`, `@ApiResponse`, and `@SecurityRequirement` annotations per endpoint.
3. `@PublicEndpoint` for unauthenticated endpoints; `@AuthenticationPrincipal` for authenticated ones.

**Canonical authenticated endpoint pattern:**
```java
@Operation(summary = "Create a custom role")
@ApiResponse(responseCode = "201", description = "Role created")
@SecurityRequirement(name = "bearerAuth")
@PostMapping
public ResponseEntity<CustomRoleResponse> createRole(
        @AuthenticationPrincipal AuthenticatedUser user,
        @Valid @RequestBody CreateCustomRoleRequest request) {
    CustomRole role = createCustomRoleHandler.execute(new CreateCustomRoleCommand(/* ... */));
    return ResponseEntity.status(201).body(new CustomRoleResponse(/* ... */));
}
```

## CRITICAL: Self-Correction & Verification Before Gradle
- No wildcard imports
- No business logic in controller methods
- All dependencies constructor-injected
- DTOs are imported from `presentation.dto` package, not defined inline

## Step 3: Gradle Verification
```
.\gradlew :atlashub-platform:<module>:compileJava
```

## Final Step: Git Commit & Push
```
git add <paths>
git commit -m "feat(<module>): add <ControllerName> REST controller"
git push origin HEAD
```
