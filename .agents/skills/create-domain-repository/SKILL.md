---
name: create-domain-repository
description: Create or audit an AtlasHub aggregate repository contract owned by its domain module.
---

# Create domain repository

Load `atlashub-module-workflow`, resolve artifact `repository`, and read the aggregate, callers, and module docs.

- Expose domain types only and extend `Repository<T>` without redeclaring base methods.
- Add only methods with real current callers.
- Scope organization-owned lookups in the contract where required; never load globally and filter later.
- Express required locking semantics explicitly.
- Use shared `PageResult` for documented pagination.
- Cross-module consumers never import this repository; use `create-shared-query-port` for reads.

After a contract change, invoke the persistence chain as needed: `create-jpa-entity`, `create-domain-mapper`, `create-spring-data-repository`, `create-repository-adapter`. Compile only when every implementation matches.
