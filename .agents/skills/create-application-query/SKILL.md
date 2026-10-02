---
name: create-application-query
description: Create or audit an AtlasHub CQRS read query and handler using same-module repositories or shared cross-module query ports only.
---

# Create application query

Load `atlashub-module-workflow`, resolve artifact `query`, and read docs, repository/port contracts, caller, and tenancy/RBAC rules.

- Queries are read-only and never call save/delete, publish events, or invoke command handlers.
- Same-module reads use its repository/read adapter. Cross-module reads use `com.atlashub.shared.application.port`; invoke shared-port and adapter skills if absent.
- Never create JPA projections/repositories in a consuming module for another module's tables.
- Scope organization-owned results using active organization context.
- If input has `page` and `size`, return `PageResult<T>` with real totals. Otherwise return the documented scalar/optional/list contract.
- Results are immutable records and do not expose domain/JPA/provider objects.
- Apply documented `@PreAuthorize` to user/API-triggered handlers.
- Map not-found and empty-list behavior exactly as docs specify.

Use the scaffold only as a starting layout; remove all placeholders. Add tests for scope, mapping, pagination, and no-write behavior. Run validator and targeted compile/tests.
