# atlashub Architecture & Coding Rules

This project uses Hexagonal Architecture (Ports and Adapters) combined with Domain-Driven Design (DDD) and CQRS. You MUST follow these rules strictly.

## 1. CQRS and Application Layer Structure
- **Commands**: DTOs that mutate state MUST be suffixed with `Command` (e.g., `CreateCustomerCommand`) and placed in `application/command`.
- **Queries**: DTOs that read state MUST be suffixed with `Query` (e.g., `GetCustomerQuery`) and placed in `application/query`.
- **Responses**: DTOs returned by use cases MUST be placed in `application/result`.
- **Use Cases**: All handlers MUST be placed in `application/usecase` and MUST extend `BaseUseCase<Input, Output>` from `atlashub-shared-kernel`.
  - For commands that do not return a result, extend `BaseUseCase<Input, Void>` and return `null`. Do NOT create or use a separate `BaseCommandUseCase`.

## 2. Hexagonal Architecture (Ports and Adapters)
- **Domain Layer (`domain`)**: Contains Aggregate Roots, Value Objects, Domain Events, Domain Exceptions, and **Repository Interfaces**.
  - Repositories are in `domain/repository`. They define *what* the domain needs to persist state.
- **Application Layer (`application`)**: Orchestrates use cases. Contains Commands, Queries, DTOs, Use Cases, and **Outbound Ports (`port/out`)**.
  - Query Services (e.g., `CustomerQueryService`) and external service interfaces (e.g., `AccountNameResolutionPort`) live in `application/port/out`.
  - Do NOT create `port.in` interfaces. The `BaseUseCase<I, O>` class effectively serves as the inbound port for the adapters.

## 3. Domain-Driven Design (DDD) Rules
- **Aggregates**:
  - MUST NOT have public setters. State changes must happen via explicit business methods (e.g., `deactivate()`, `verifyEmail()`).
  - Should expose necessary public getters for validation by use cases.
  - Must queue domain events internally (typically handled by a `BaseAggregateRoot` class).
- **Domain Events**:
  - Must be immutable (`record`) and implement the `DomainEvent` interface.
  - Must be wrapped in `EnvelopedDomainEvent` before publishing (to attach `correlationId` and `dispatchedAt`).
  - Do NOT write boilerplate code to pull, envelop, and publish events in individual use cases. `BaseUseCase` has a protected `publishEvents(aggregate, publisher)` method that does this automatically. Simply call this method after saving the aggregate.

## 4. Exceptions and Error Handling
- Use the unified module `ErrorCode` enum (e.g., `IdentityErrorCode`).
- Throw custom business exceptions (`BusinessRuleException`, `NotFoundException`, `ConflictException`, `ValidationException`) from the shared kernel, passing the `ErrorCode`.
- Do NOT throw generic `RuntimeException` or `IllegalArgumentException` for business errors.

## 5. Documentation
- ANY architectural changes, new use cases, new endpoints, or renamed concepts (e.g., `KycStatus` to `ComplianceStatus`) MUST be immediately reflected in the documentation (`docs/design.md`, `docs/modules/*.md`, `docs/domain-glossary.md`).
- Ensure folder structure changes are documented in `docs/design.md` and `docs/module-design-template.md`.

## 6. Git Commits
- Commit changes in small, logical, atomic chunks. Do not lump massive refactors and new features into a single commit.

## 7. Code Style
- **NO INLINE IMPORTS**: You must NEVER use inline imports in Java files (e.g., `java.util.Map<...>`). All imports MUST be placed at the top of the file. NEVER FORGET THIS RULE.
- **NO INLINE REQUESTS/RESPONSES**: You must NEVER define request or response DTOs inline inside Controllers. All requests and responses MUST be defined in their individual packages (e.g., `adapter/in/web/request` and `adapter/in/web/response`).

## 8. Entities and Mappers
- **JPA Entities**: MUST have `@AllArgsConstructor` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)`. They MUST have explicit database indexes (`@Table(indexes = {...})`) for all queryable fields (e.g., `organizationId`, `userId`, `email`). ONLY fields that can be legitimately updated should have a `@Setter`. Do NOT put `@Setter` at the class level unless every single field is mutable.
- **Mappers**: The `infrastructure` layer must contain a `mapper` package. For every Entity/Aggregate, you must define a specific Mapper class responsible for converting between Domain and JPA Entity. Adapters MUST use these mapper classes rather than mapping inline.
- **Mappers & Domain Events**: Domain events must ONLY be pulled from the application layer (e.g. inside Use Cases). You MUST NEVER call `.pullDomainEvents()` inside the Mappers.

## 9. Logging
- **Loggers in Use Cases**: Loggers MUST be located in the `shared` module and used exclusively inside Use Cases (Application layer). Do NOT place loggers inside Controllers (Web layer).

## 10. Audit & Event Listening
- **No Generic Listeners**: Audit modules and similar event subscribers MUST NOT use a single generic listener for all events. Define dedicated, specific listeners for each context or major event type.
- **No Mocks in Production Code**: When bridging modules in a modular monolith, do NOT use "dummy" or "mock" adapter implementations. You MUST build complete, functional bridges (e.g., shared interfaces in `shared` module implemented by the provider and injected into the consumer).
