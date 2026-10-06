---
name: create-external-adapter
description: Implement an AtlasHub domain/application port for an external provider with strict mapping, idempotency, timeout, error, and secret-handling rules.
---

# Create external adapter

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read the module design and the relevant file under `docs/setup` completely. The module-owned port must exist; otherwise invoke `create-domain-port` first.

Rules:

- Adapter lives under `infrastructure/external/<provider>` and implements exactly one coherent port.
- Provider DTOs remain private to infrastructure. Map to/from AtlasHub records at the boundary.
- Configure base URL, keys, timeouts, and retry policy through typed configuration. Never hardcode secrets or log credentials, BVNs, tokens, signatures, or full bank-account data.
- Retry only operations proven idempotent; use documented idempotency keys. Bound retries and timeouts.
- Translate provider failures into module-specific exceptions with stable AtlasHub error codes while retaining safe diagnostic context.
- Validate response identity and status before mutating domain state. Provider callbacks/events enter through commands and idempotency tracking.
- Add contract tests for success, validation failure, provider rejection, timeout, malformed response, and duplicate request.

Do not expose provider registration types, industries, statuses, or names in public APIs when the module docs define AtlasHub-owned equivalents.
