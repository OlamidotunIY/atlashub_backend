---
name: create-domain-service
description: Create or audit a stateless AtlasHub domain service for business rules spanning multiple entities or value objects.
---

# Create domain service

Load `atlashub-module-workflow`, resolve artifact `entity`, and read the module docs and involved entities completely.

Create a domain service only when the rule does not naturally belong to one aggregate or value object. Otherwise put the behavior on that type. A domain service:

- Lives in `domain/services` (or the module's existing singular convention).
- Has no Spring, JPA, web, Kafka, Redis, or provider imports.
- Is stateless and deterministic unless it receives information through a domain port.
- Uses domain types and throws module-specific domain exceptions.
- Does not save entities or publish events; the application handler performs orchestration and persistence.

If external information is required, invoke `create-domain-port` first. If a new failure mode is introduced, invoke `create-domain-error` first. Add focused unit tests with `create-module-test`.
