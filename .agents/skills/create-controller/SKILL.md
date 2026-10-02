---
name: create-controller
description: Create or audit a thin per-resource AtlasHub Spring REST controller using presentation DTOs, principal context, handlers, and ApiResponse envelopes.
---

# Create controller

Load `atlashub-module-workflow`, resolve artifact `controller`, and read endpoint docs, handlers, security, and sibling controllers. Invoke command/query skills first, then `create-presentation-dto` for missing DTOs.

- One controller per entity/resource. Never create a module-wide god controller.
- Controllers live in `presentation/rest`; all request/response records live in `presentation/dto`.
- Delegate every operation to one command/query handler. No repositories, provider clients, transactions, or business rules.
- For bearer-authenticated operations, accept `@AuthenticationPrincipal AuthenticatedPrincipal` and derive user ID and active organization ID from it. Do not accept those context IDs from request bodies.
- Use `@Valid`, accurate HTTP methods/statuses, OpenAPI annotations, and documented security requirements.
- Return `ResponseEntity<ApiResponse<T>>` and keep these helpers when applicable:

```java
private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
    return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
}

private ResponseEntity<ApiResponse<Void>> done(String message) {
    return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
}
```

Use `201` for creates when required while preserving the same envelope. Never return domain/JPA entities directly. Add MVC tests for validation, principal propagation, authorization, delegation, envelope, and status. Run validator and module tests.
