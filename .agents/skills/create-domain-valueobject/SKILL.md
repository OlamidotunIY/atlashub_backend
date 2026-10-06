---
name: create-domain-valueobject
description: Create or audit an immutable AtlasHub domain value object or enum with semantic validation and correct ownership.
---

# Create domain value object

Load `atlashub-module-workflow`, resolve artifact `value-object`, and read the owning module docs.

Search shared and module value objects first. Use shared only for a universal provider-neutral concept used consistently across modules. Business registration types, industries, statuses, and provider mappings remain module-owned.

- Prefer records for immutable structures and enums for documented closed sets.
- Validate in compact constructors and normalize only when docs allow it.
- Invoke `create-domain-error` for validation failures; do not instantiate shared exceptions directly.
- Keep Spring, JPA, Jackson, and provider annotations out of module domain types.
- Do not invent `UNKNOWN`/`OTHER` values.
- Provider vocabulary mapping belongs in an external adapter.

Generate complete fields/constants, then test valid values, boundaries, normalization, equality, and mappings. Run validator and module compile.
