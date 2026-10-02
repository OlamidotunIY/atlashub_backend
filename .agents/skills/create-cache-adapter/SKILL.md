---
name: create-cache-adapter
description: Create or audit an AtlasHub Redis/cache adapter behind an owning-module port with safe keys, TTLs, serialization, and invalidation.
---

# Create cache adapter

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read module, Redis, tenancy, and data-sensitivity docs. Create/use a module-owned port where callers need a cache abstraction.

- Cache is never the source of truth unless the specific authentication/session design explicitly says so.
- Keys include environment and organization/user scope needed to prevent tenant leakage.
- Define TTL from docs/configuration; no unexplained hardcoded lifetime.
- Store immutable DTOs or safe serialized values, never domain/JPA entities, raw access/refresh tokens, passwords, provider secrets, or unencrypted sensitive identity data.
- Read-through failures fall back only when the documented consistency model allows it. Do not swallow corruption silently; evict invalid values and emit safe diagnostics.
- Owning-module writes invalidate/update every related key after successful persistence.
- Avoid wildcard deletion/`KEYS`; use explicit keys or bounded scan strategy.
- Test hit, miss, expiry, malformed value, Redis outage, tenant isolation, and invalidation.

Run validator, module tests, and application-context verification.
