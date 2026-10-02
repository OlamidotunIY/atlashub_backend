---
name: create-module-test
description: Create focused AtlasHub module tests for domain invariants, handlers, adapters, controllers, messaging, and Spring wiring.
---

# Create module tests

Load `atlashub-module-workflow`, resolve artifact `test`, and read the behavior being tested plus its docs. Test observable contracts, not implementation trivia.

Tests are not optional companion work. Whenever any leaf skill creates an artifact, create its corresponding test in the same run. Whenever production behavior or a contract changes, update the affected tests in the same run. Never finish or commit a production change with stale, missing, disabled, or ignored tests.

Required coverage by artifact:

- Entity/value object: factory defaults, every invariant failure using the semantic module exception, valid state transitions, emitted event type/payload, timestamps where material.
- Command: authorization annotation where required, repository/port orchestration, save/event outcome, idempotency, and failure paths.
- Query: organization scoping, pagination contract, mapping, empty/not-found behavior, and no writes.
- Repository adapter: mapping, custom queries, sequence usage, optimistic locking where applicable, and event publication after persistence.
- Shared query-port adapter: bean creation, DTO mapping, cache fallback/invalidation contract, and read-only behavior.
- Listener/scheduler: builds the correct command and invokes the handler once; no repository mocks are permitted because those dependencies are forbidden.
- Controller: principal-derived user/organization context, validation, handler delegation, `ApiResponse<T>` envelope, and authorization.
- External adapter: provider mapping, safe error translation, timeout, idempotency, and malformed responses.
- Domain exception/event/DTO/port: constructor or validation contract, payload/serialization compatibility, DTO validation/mapping, and port contract through its owning adapter/consumer.
- JPA/Spring Data/configuration/cache: real persistence or application-context integration tests; mocks alone are insufficient.

Use the module's existing test style and fixtures. Run the narrow test task and every required integration/context test. Never weaken production visibility or validation solely to make a test pass. Successful tests are followed by the workflow's scoped completion commit; failed tests leave the change uncommitted.
