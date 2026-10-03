---
name: atlashub-module-workflow
description: Mandatory governance workflow for any AtlasHub backend module creation, change, audit, or deletion. Resolves module docs, enforces boundaries, routes dependent skills, and validates changed Java files.
---

# AtlasHub Module Workflow

This skill is mandatory whenever code under `atlashub-platform`, `atlashub-pay`, `atlashub-infrastructure`, `atlashub-main`, or `atlashub-shared` is created, changed, audited, or removed. It governs every other project skill. If a leaf skill conflicts with this workflow, this workflow wins.

## 1. Establish scope before code

Run the context resolver from the repository root:

```powershell
.\.agents\skills\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1 `
  -Module "<module>" -SubModule "<optional-submodule>" -Artifact "<artifact-kind>"
```

Read every file listed under `requiredDocs` completely before modifying code. Also read the target module's existing implementation for the same artifact type and the directly connected types. Do not infer a contract from a generator template.

If docs conflict with current code, do not overwrite the code blindly. Report the exact conflict and follow the user's resolved decision. Newer explicit user decisions override old docs; update docs when the task includes documentation changes.

## 2. Non-negotiable architecture

- AtlasHub is the source of truth. Provider models and names do not leak into public domain/API contracts unless the docs explicitly require them.
- A module owns its domain entities and repositories. No module imports another module's domain, application, infrastructure, or presentation packages.
- Cross-module reads may be synchronous only through an interface whose file is in `atlashub-shared/src/main/java/com/atlashub/shared/application/port` and whose package is `com.atlashub.shared.application.port`. Never place that contract in an owning or consuming module's domain/application packages. Its implementation belongs to the owning module under `infrastructure/persistence/adapters`.
- Cross-module writes are asynchronous domain/integration events. Never call another module's repository, command handler, controller, or implementation directly.
- Kafka listeners and schedulers are infrastructure triggers under `infrastructure/messaging/listeners` and `infrastructure/messaging/schedulers`. They may construct a command and call exactly the appropriate command handler. They never use repositories, domain services, external clients, or business rules directly.
- Controllers are per resource/entity, live in `presentation/rest`, and delegate to handlers. Request and response records live in `presentation/dto`; requests are never declared inside controllers.
- Authenticated operations obtain the user ID and active organization ID from `AuthenticatedPrincipal`. Do not accept those context IDs from request bodies. Target IDs that are the subject of an operation are allowed when the docs require them.
- Domain entities contain invariants and state transitions. They never throw shared base exceptions directly. Create a semantic exception in that module's `domain/exception` or existing `domain/exceptions` package and extend a shared base exception there.
- Domain entities use Lombok `@Getter` for field access and do not contain hand-written field getters; aggregate roots retain only the required explicit `getId()` override. Computed predicates/domain behavior remain explicit methods.
- Module repository contracts extend the shared `Repository<T>` and never redeclare inherited methods, including `nextIdentity`, `save`, `findById`, `deleteById`, `existsById`, and `findAll`.
- Domain entities publish only documented or explicitly approved events. Events are created before registering them.
- JPA is an infrastructure detail: no JPA/Spring imports in domain code, no ORM relationship annotations, no generated IDs, and MySQL uses `json`, never `jsonb`.
- Preserve the target module's established singular/plural package names and `Jpa`/`JPA` naming. Do not introduce a second convention.
- Delete newly obsolete files and imports after replacing a design. Never leave parallel projections, adapters, DTOs, or old endpoints unless they still have callers.
- Every leaf artifact skill must finish its own production change, create or update the corresponding tests, run the required unit and integration verification, and make one scoped commit containing only that skill's files. Never commit failing or untested code. Never push unless the user explicitly asks.

Read [references/dependency-routing.md](references/dependency-routing.md) before creating a new vertical slice.

## 3. Dependency closure

Before editing, inventory the complete slice: domain types, errors, events, repository contract, command/query, persistence, cross-module ports, messaging, presentation, and tests. For every missing dependency, invoke the skill named in the routing reference before continuing. Do not create improvised substitutes.

Entity-specific rule: inspect every invariant and state transition. If a semantic domain exception or required event does not exist, invoke `create-domain-error` or `create-domain-event` first. Shared exceptions are allowed only as base classes of module-specific exceptions, never as the exception instantiated by an entity.

## 4. Mandatory tests

Creating an artifact requires creating its test in the same skill run. Updating an artifact requires updating its existing tests in the same skill run. If no test exists, create it; never use that absence as a reason to skip testing. Invoke `create-module-test` before completion.

Unit tests are mandatory for every artifact with behavior or a contract. Integration tests are additionally mandatory when the change touches persistence/JPA, Spring Data derived queries, repository adapters, shared query-port adapters, Redis/cache behavior, external providers, serialization/messaging, controllers/security, bean configuration, or application wiring. Spring wiring changes require an application-context test or startup verification.

Tests must assert meaningful behavior and failure cases. A compile-only check, empty context test, mock-only persistence test, or test that merely checks a class exists does not satisfy this gate.

## 5. Verification and commit gate

Read back every changed file, then run:

```powershell
.\.agents\skills\atlashub-module-workflow\scripts\Test-AtlashubArchitecture.ps1 `
  -Module "<module>" -SubModule "<optional-submodule>"
```

Fix every reported error. Then run the narrow Gradle tasks for the changed module: `compileJava`, relevant tests, and—when Spring bean/repository/security wiring changed—application-context or startup verification. A compile is not sufficient for Spring Data derived queries or bean discovery.

After every required test passes, invoke the scoped completion script with the exact production and test paths changed by this leaf skill:

```powershell
.\.agents\skills\atlashub-module-workflow\scripts\Complete-AtlashubSkill.ps1 `
  -Module "<module>" -SubModule "<optional-submodule>" `
  -Paths @("<production-file>", "<test-file>") `
  -TestPaths @("<test-file>") `
  -CommitMessage "feat(<scope>): <description>"
```

Use `-RequiresIntegrationTest`, `-IntegrationTestPaths`, and `-AdditionalTasks` when the integration rules above apply. The script must refuse unrelated pre-staged changes and must never push.

Do not claim success or commit if validation or required tests did not run. If blocked, report the failure and leave the work uncommitted.
