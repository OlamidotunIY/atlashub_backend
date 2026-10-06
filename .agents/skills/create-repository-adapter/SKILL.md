---
name: create-repository-adapter
description: Create or audit an AtlasHub persistence adapter implementing a domain repository through JPA mapping and domain-event publication.
---

# Create repository adapter

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read the domain repository, entity, JPA record, mapper, base repository, and docs. Invoke missing prerequisites in order: JPA entity, mapper, Spring Data repository.

- Adapter lives in `infrastructure/persistence/adapters`, is a Spring bean, and implements only its own module repository.
- Extend `JpaBaseRepository` when the aggregate/base contracts fit; inject Spring Data repository, mapper, sequence generator, and event publisher as required by the actual base constructor.
- Do not reimplement inherited CRUD/identity behavior.
- Implement every custom domain repository method and no unused extras.
- Map only through the mapper; never return JPA types.
- Preserve organization scope, pagination, locking, ordering, and uniqueness semantics.
- Domain events publish through the base/event publisher after successful local persistence; do not publish from a listener.
- Invalidate owning-module caches after successful writes where a query-port adapter caches data.

Add adapter persistence tests, run validator and module tests/compile.
