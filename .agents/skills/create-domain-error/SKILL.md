---
name: create-domain-error
description: Create or audit a semantic module-owned AtlasHub domain exception for an invariant or business failure.
---

# Create domain error

Load `atlashub-module-workflow`, resolve artifact `error`, and read the invariant and module docs before naming the error.

- Search both `domain/exception` and `domain/exceptions`; preserve the module's convention and never create a parallel package.
- Reuse an exception only when meaning and API status are identical.
- Name the failed condition (`LastOwnerRemovalException`, not `InvalidOperationException`).
- Extend the appropriate shared base: validation for malformed domain input, conflict for state/uniqueness conflict, not-found for absence, authorization for forbidden action, business-rule for another invariant.
- Shared exceptions are base types only. Entities instantiate the module exception.
- Provide a stable default message and message constructor. Add cause only if the base supports it and it is needed.
- Never expose provider details, secrets, or personal data in messages.

Use the generator with the existing package convention and semantic default message. Verify actual base constructors, run validator, tests, and module compile.
