---
name: create-application-command
description: Create or audit an AtlasHub CQRS write command and handler with complete domain, port, authorization, and persistence dependencies.
---

# Create application command

Load `atlashub-module-workflow`, resolve artifact `command`, and read all docs, target aggregate methods, repositories, ports, and caller.

Dependency rules:

- Same-module state uses that module's domain repository. Invoke `create-domain-repository` and its persistence chain when missing.
- Cross-module reads use a shared application query port; invoke `create-shared-query-port` and `create-query-port-adapter` when missing.
- Cross-module writes publish events; never inject another module's handler/repository.
- External effects use module-owned ports; invoke `create-domain-port`/`create-external-adapter` when missing.
- Missing aggregate invariants/events/errors are fixed through their domain skills before handler code.

Handler rules:

- Handler orchestrates: validate context, load, call domain behavior, save, return result. Business rules do not live here.
- New aggregate IDs come from repository `nextIdentity()` and are passed to the entity factory.
- User/API-triggered commands have documented `@PreAuthorize`; listener/scheduler system commands do not fake a user principal.
- Organization/user context comes from the controller/principal command fields where appropriate, never trusted body fields.
- Use a specific result record when the caller needs output; `Void` only when no synchronous result exists.
- Transactions cover the atomic local write. Network calls are not hidden inside a DB transaction without documented orchestration.
- No placeholders, catch-all exception swallowing, or logging secrets.

Use the scaffold for file layout only and complete it immediately. Add handler/domain tests, run validator, targeted compile/tests, and runtime wiring verification when dependencies changed.
