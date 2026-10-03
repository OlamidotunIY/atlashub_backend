---
name: create-shared-query-port
description: Create or audit a synchronous cross-module read contract in com.atlashub.shared.application.port and require its owning-module implementation.
---

# Create shared query port

Load `atlashub-module-workflow`, resolve artifact `port`, and read module communication docs plus owner and consumer designs.

- A contract used for a read across module boundaries is always a shared application port. Its only valid source location and package are `atlashub-shared/src/main/java/com/atlashub/shared/application/port` and `com.atlashub.shared.application.port`.
- Never create a cross-module query port under an owning or consuming module's `domain/ports`, `application/port`, or any other package. Module-owned domain/application ports are reserved for capabilities that are not cross-module read contracts.
- Search the shared port directory first; update an existing coherent port instead of duplicating it.
- Port supports reads only. Cross-module writes use events.
- Return scalars, `Optional`, immutable nested DTO records, lists, or page contracts—never domain/JPA/provider entities.
- Keep the smallest stable consumer contract and AtlasHub vocabulary.
- No Spring annotations or module imports in shared. Put consumer-neutral request/result DTO records inside the shared contract when needed.
- After changing the port, invoke `create-query-port-adapter` in the data-owning module and update all consumers.
- Ensure `atlashub-main` depends on the owning module so Spring can discover the adapter bean.

Before completing, verify the port file path and `package` declaration both point to shared, and verify no duplicate `*QueryPort` remains in any module domain/application package. Compile shared, owner, and consumers, then run an application-context/startup test. Java compilation alone cannot prove bean registration.
