---
name: create-query-port-adapter
description: Implement a shared synchronous query port inside the module that owns the queried data, using its own persistence/read model and returning shared DTOs.
---

# Create shared query-port adapter

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read the module and communication docs. The shared port must already exist; otherwise invoke `create-shared-query-port` first.

Rules:

- Implement the port in the data-owning module under `infrastructure/persistence/adapters`, alongside every other persistence-backed adapter. Never place a shared query-port adapter in `infrastructure/services` or in the consuming module.
- Annotate the implementation with `@Component` or `@Service` and ensure the executable application has a Gradle dependency on the owning module.
- It may read the owning module's Spring Data repository or cache. It never writes.
- Return only port DTOs/scalars. Never return domain or JPA entities.
- Do not create projection repositories in consuming modules. Consumers inject the shared port.
- Cache keys are owned and invalidated by the owning module's write adapter.
- Map `not found` according to the port contract (`Optional`, nullable status, or semantic exception); do not guess.

After implementation, search all consumers, compile the shared and owning modules, and perform an application-context test because bean discovery cannot be proven by Java compilation.
