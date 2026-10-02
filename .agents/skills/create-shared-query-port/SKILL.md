---
name: create-shared-query-port
description: Create or audit a synchronous cross-module read contract in com.atlashub.shared.application.port and require its owning-module implementation.
---

# Create shared query port

Load `atlashub-module-workflow`, resolve artifact `port`, and read module communication docs plus owner and consumer designs.

- Search `atlashub-shared/src/main/java/com/atlashub/shared/application/port`; update an existing coherent port instead of duplicating it.
- Port supports reads only. Cross-module writes use events.
- Return scalars, `Optional`, immutable nested DTO records, lists, or page contracts—never domain/JPA/provider entities.
- Keep the smallest stable consumer contract and AtlasHub vocabulary.
- No Spring annotations or module imports in shared.
- After changing the port, invoke `create-query-port-adapter` in the data-owning module and update all consumers.
- Ensure `atlashub-main` depends on the owning module so Spring can discover the adapter bean.

Compile shared, owner, and consumers, then run an application-context/startup test. Java compilation alone cannot prove bean registration.
