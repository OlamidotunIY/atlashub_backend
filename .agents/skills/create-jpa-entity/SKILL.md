---
name: create-jpa-entity
description: Create or audit an AtlasHub JPA persistence record matching a domain aggregate and MySQL conventions.
---

# Create JPA entity

Load `atlashub-module-workflow`, resolve artifact `adapter`, and read the domain entity, repository, docs, and sibling JPA conventions.

- Preserve the module's `Jpa`/`JPA` suffix and Lombok style.
- Implement `BaseJpaEntity` when required by base persistence contracts.
- IDs use `DomainSequenceGenerator`; never `@GeneratedValue`.
- Store foreign IDs as scalars; ORM relationship annotations are forbidden.
- Derive nullability from all valid lifecycle states.
- Add unique constraints/indexes for documented invariants and real lookups.
- Use `@Version` when required by base contracts or concurrency design.
- MySQL JSON uses `@JdbcTypeCode(SqlTypes.JSON)` and `columnDefinition = "json"`, never `jsonb`.
- Persist required domain state but no provider secret.

Then invoke mapper, Spring Data repository, and repository adapter skills. Add persistence tests and run validator/module compile.
