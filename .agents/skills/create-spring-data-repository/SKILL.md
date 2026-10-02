---
name: create-spring-data-repository
description: Create or audit an AtlasHub Spring Data JPA repository that exactly supports one owning domain repository and valid JPA fields.
---

# Create Spring Data repository

Load `atlashub-module-workflow`, resolve artifact `repository`, and read the JPA record, domain repository, adapter, and docs. Invoke `create-jpa-entity` first if absent.

- Repository lives in `infrastructure/persistence/repositories` and extends `JpaRepository<ActualJpaType, Long>`.
- Add only methods required by the adapter/query-port implementation.
- Every derived-query property must exist with the exact Java field path on the JPA type. After model renames, delete stale methods rather than guessing replacements.
- Use explicit JPQL for ambiguous/complex queries and native SQL only when justified. Keep organization scope, ordering, pagination, and lock annotations accurate.
- Spring Data repository remains owned by the module; never expose/inject it into another module.
- Do not duplicate inherited CRUD methods.

Add repository tests for custom methods. Run validator, module compile/tests, and application-context startup because invalid derived methods often fail only at runtime.
