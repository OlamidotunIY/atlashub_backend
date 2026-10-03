---
name: create-domain-entity
description: Create, update, or audit an AtlasHub domain entity or aggregate root together with required value objects, semantic exceptions, events, and repository contract.
---

# Create domain entity

Load `atlashub-module-workflow`, resolve artifact `entity`, and read all returned docs and the complete existing entity slice.

Before editing, enumerate every field, invariant, state transition, and documented event:

- Invoke `create-domain-valueobject` for missing domain types.
- For every invariant failure, search the module's `domain/exception` or `domain/exceptions`. If no precise semantic exception exists, invoke `create-domain-error` first. An entity must never instantiate a shared exception.
- For every documented event-producing transition, search `domain/events`; invoke `create-domain-event` before registering a missing event.
- If persisted and lacking a repository contract, invoke `create-domain-repository`.

Rules:

- Domain code has no Spring, JPA, JSON, web, provider, or infrastructure dependencies.
- Annotate every entity with Lombok `@Getter` and import `lombok.Getter`; do not hand-write field getters. An aggregate root may keep only the required `getId()` override. Computed domain predicates such as `isExpired()` are behavior, not field getters, and remain explicit.
- Aggregate roots extend `AggregateRoot<Long>` and expose `getId()` through the required override; all other field access comes from Lombok `@Getter`.
- The application generates IDs. A `create` factory accepts the generated ID and never creates or nulls it.
- Factories accept caller-known creation data only and set defaults/timestamps internally.
- Mapper rehydration does not emit creation events or reset state.
- State changes use intention-revealing methods that enforce invariants and update owned timestamps.
- Fields unchanged after construction are `final`; boolean fields do not start with `is`.
- No public setters, hand-written field getters, or Lombok `@Data`.
- Event payloads are minimal and do not duplicate `aggregateId`.

Use the scaffold only for a new file and complete it immediately. Read back the full slice, run the architecture validator, domain tests, and target module compile. Never commit or push unless explicitly requested.
