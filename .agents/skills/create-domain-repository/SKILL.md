---
name: create-domain-repository
description: Create or audit an AtlasHub aggregate repository contract owned by its domain module.
---

# Create domain repository

Load `atlashub-module-workflow`, resolve artifact `repository`, and read the aggregate, callers, and module docs.

- Read `atlashub-shared/src/main/java/com/atlashub/shared/domain/repository/Repository.java` before writing the contract.
- Expose domain types only and extend `Repository<T>` without redeclaring or overriding any inherited method. At present, never add `nextIdentity`, `save`, `findById`, `deleteById`, `existsById`, or `findAll` to a module repository; inheritance already supplies those signatures.
- Add only aggregate-specific queries or commands whose signatures are not already satisfied by the full inherited interface hierarchy. Do not redeclare a base method merely to rename its parameter, narrow its generic return to the aggregate type, add documentation, or make the method visible.
- Add only methods with real current callers.
- Scope organization-owned lookups in the contract where required; never load globally and filter later.
- Express required locking semantics explicitly.
- Use shared `PageResult` for documented pagination.
- Cross-module consumers never import this repository; use `create-shared-query-port` for reads.

Before completing, compare every declared method against `Repository<T>` and all other parent interfaces and remove duplicates. Then invoke the persistence chain as needed: `create-jpa-entity`, `create-domain-mapper`, `create-spring-data-repository`, `create-repository-adapter`. Compile only when every implementation matches.
