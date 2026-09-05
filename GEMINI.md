# atlashub Backend — Architecture & Coding Rules

**CRITICAL AI INTERACTION RULE:**
**NEVER EDIT THE CODEBASE DIRECTLY WITHOUT EXPLICIT PERMISSION FROM THE USER.**
If the user asks a question, requests a bug fix, or asks to implement a feature, you MUST:
1. Thoroughly research the codebase.
2. Provide a detailed IMPLEMENTATION PLAN first.
3. STOP and WAIT for the user to explicitly say "implement it" or give approval before making ANY file modifications.
**NO EXCEPTIONS.**

This project is a **modular monolith** using **Hexagonal Architecture** (Ports and Adapters), **Domain-Driven Design (DDD)**, and **CQRS**. Every rule below is mandatory. No exceptions.

---

## 1. Module & Folder Structure

Each feature submodule (e.g., `atlashub-platform/billing`, `atlashub-pay/ledger`) follows a strict internal layout:

```
<module>/
  src/main/java/com/atlashub/<module-name>/
    adapter/
      in/
        messaging/          <- Kafka listeners (inbound events)
        web/
          controller/       <- REST controllers (optional subfolder)
          request/          <- Request DTOs (one class per file)
          response/         <- Response DTOs (one class per file)
      out/
        persistence/
          entity/           <- JPA entities
          mapper/           <- Domain <-> Entity mappers
          repository/       <- Spring Data interfaces + repository adapters
        external/           <- Adapters for third-party APIs (e.g., Paystack)
        catalog/            <- Cross-module query adapters (e.g., ProductQueryAdapter)
    application/
      command/              <- Mutation input DTOs (suffixed Command)
      query/                <- Read input DTOs (suffixed Query)
      result/               <- Output DTOs from use cases
      port/
        out/                <- Outbound ports (interfaces for external services / cross-module queries)
      usecase/              <- Use case implementations
    domain/
      event/                <- Domain event records
      exception/            <- Module-specific ErrorCode enum
      model/                <- Aggregate roots and entity models
      repository/           <- Repository interfaces (domain-owned)
      valueobject/          <- Enums, value objects, type-safe wrappers
```

**Rules:**
- Never nest adapters inside `application` or `domain`.
- Never put business logic inside `adapter`.
- Never put infrastructure concerns inside `domain`.
- The `domain/repository` package contains **interfaces only**. Implementations live in `adapter/out/persistence/repository`.

---

## 2. Domain Layer

### 2.1 Aggregate Roots

- Every aggregate root MUST extend `AggregateRoot<ID>` from `atlashub-shared`.
- MUST implement `public ID getId()` — the `AggregateRoot` base class has a NO-ARG constructor. Do NOT call `super(id)`.
- MUST NOT have any `public` setters. All state changes happen through explicit business methods (e.g., `activate()`, `cancel(reason)`, `markAsPaid()`).
- Business method names MUST be intention-revealing verbs describing the business event, not generic setters.
- MUST use `registerEvent(new SomeEvent(...))` inside business methods whenever a state change worth broadcasting occurs.
- MUST NOT call `pullDomainEvents()` on itself — that is the application layer's responsibility.
- Static factory methods (e.g., `create(...)`, `initiate(...)`) should be used for initial creation. They register the creation event internally.

```java
// CORRECT
public class PaystackCharge extends AggregateRoot<Long> {
    private final Long id;
    private ChargeStatus status;

    public static PaystackCharge initiate(Long id, Long invoiceId, ...) {
        PaystackCharge charge = new PaystackCharge(id, invoiceId, ..., ChargeStatus.INITIATED);
        charge.registerEvent(new ExternalChargeInitiatedEvent(...));
        return charge;
    }

    public void markSuccessful() {
        this.status = ChargeStatus.SUCCESSFUL;
        this.completedAt = ZonedDateTime.now();
        registerEvent(new PaymentSuccessfulEvent(...));
    }

    @Override
    public Long getId() { return id; }
}

// WRONG - never do this
public void setStatus(ChargeStatus status) { this.status = status; } // NO
publisher.publish(EnvelopedDomainEvent.wrap(new PaymentSuccessfulEvent(...))); // NO - events must come from domain
```

### 2.2 Value Objects

- Must be `record` types unless mutable state is required (it almost never is).
- Must validate their own invariants in the compact constructor and throw the appropriate domain exception (`ValidationException`, `BusinessRuleException`).
- Must NEVER reference JPA, Spring, or any infrastructure class.
- Shared value objects (e.g., `EmailAddress`, `PhoneNumber`, `NUBAN`, `Money`) live in `atlashub-shared/domain/valueobject` and `atlashub-shared/domain/money`. Do NOT duplicate them per-module.
- Module-specific value objects (e.g., `ChargeStatus`, `InvoiceStatus`, `PaymentMethod`) live in the module's `domain/valueobject` package.

```java
// CORRECT - self-validating record
public record EmailAddress(String value) {
    public EmailAddress {
        if (value == null || value.isBlank())
            throw new ValidationException(SharedErrorCode.INVALID_EMAIL_FORMAT, "Email cannot be empty");
    }
}
```

### 2.3 Enums as Value Objects

- **NEVER use raw strings where an enum should exist.** If something can only take a finite set of values, it MUST be an enum.
- Enums live in `domain/valueobject`.
- Enums that model external system concepts (e.g., Paystack event types) may carry a `value` field and a factory method:

```java
public enum PaystackEventType {
    CHARGE_SUCCESS("charge.success"),
    CHARGE_FAILURE("charge.failure");

    private final String value;

    PaystackEventType(String value) { this.value = value; }

    public String value() { return value; }

    public static PaystackEventType from(String raw) {
        for (PaystackEventType t : values()) {
            if (t.value.equals(raw)) return t;
        }
        throw new BusinessRuleException(ChargesErrorCode.UNKNOWN_EVENT_TYPE, "Unknown Paystack event: " + raw);
    }
}
```

### 2.4 Domain Events

- Must be immutable `record` types.
- Must implement `DomainEvent<T>` from `atlashub-shared`.
- Must follow the signature: `(String eventId, String aggregateId, ZonedDateTime occurredAt, T payload)`.
- The `payload` is a nested `record` named `Payload` inside the event record.
- Event names MUST use **past-tense** business language: `InvoicePaidEvent`, `SubscriptionCanceledEvent`, `WalletChargeSuccessfulEvent`.
- Events live in `domain/event` of the module that **owns** the aggregate that emits them.
- **Cross-module event decoupling**: If module B listens to events emitted by module A, module B defines its own **identical copy** of the event record in its own `domain/event` package. This avoids coupling modules via shared types.
- `DomainEvent` records MUST NEVER be constructed and published manually inside use cases or controllers. Only `publishEvents(aggregate, publisher)` is permitted. The only place event records are instantiated is inside domain aggregate methods via `registerEvent(...)`.

```java
// Event record - in domain/event
public record PaymentSuccessfulEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<PaymentSuccessfulEvent.Payload> {
    public record Payload(String reference, ChargePurpose purpose, Long invoiceId) {}
}

// CORRECT - event raised inside aggregate
public void markSuccessful() {
    this.status = ChargeStatus.SUCCESSFUL;
    registerEvent(new PaymentSuccessfulEvent(
        UUID.randomUUID().toString(),
        String.valueOf(this.id),
        ZonedDateTime.now(),
        new PaymentSuccessfulEvent.Payload(this.reference, this.purpose, this.invoiceId)
    ));
}
```

---

## 3. Domain Repository Interfaces

- Defined in `domain/repository`.
- Must NOT extend any Spring interface.
- Must NOT reference JPA, Hibernate, or any infrastructure type.
- Must always include `Long nextIdentity()` for aggregates that need a generated ID.
- Must always include `T save(T aggregate)` and `Optional<T> findById(Long id)`.
- Optionally extend `Repository<T>` from `atlashub-shared` to inherit the standard `save`, `findById`, `deleteById`, `existsById` methods.
- Custom query methods use meaningful names describing **intent**: `findByOrganizationIdAndType(...)` not `selectWhereOrgIdAndType(...)`.

```java
// CORRECT
public interface PaystackChargeRepository extends Repository<PaystackCharge> {
    Long nextIdentity();
    Optional<PaystackCharge> findByReference(String reference);
    List<PaystackCharge> findByInvoiceId(Long invoiceId);
}
```

---

## 4. Application Layer

### 4.1 Use Cases

- All use cases MUST extend `BaseUseCase<Input, Output>` from `atlashub-shared`.
- For write operations with no result: extend `BaseUseCase<Command, Void>` and `return null`.
- MUST be annotated `@Service`.
- Must be annotated `@Transactional` if they write to the database.
- MUST call `repository.nextIdentity()` for ID generation — NEVER inject or call `DomainSequenceGenerator` directly.
- MUST call `publishEvents(aggregate, publisher)` after saving an aggregate. Never manually construct and publish events.
- MUST NOT contain business logic. Business logic belongs in domain aggregates. Use cases orchestrate: fetch -> command aggregate method -> save -> publish events.
- MUST NOT inject or call `DomainSequenceGenerator`. This is an infrastructure concern and belongs only in repository adapter implementations.
- `BaseUseCase` has a NO-ARG constructor. Do NOT call `super(publisher)`.

```java
@Service
public class InitiateExternalChargeUseCase extends BaseUseCase<InitiateExternalChargeCommand, Void> {

    private final PaystackChargeRepository chargeRepository;
    private final PaymentGatewayPort paymentGatewayPort;
    private final DomainEventPublisher publisher;

    public InitiateExternalChargeUseCase(PaystackChargeRepository chargeRepository,
                                          PaymentGatewayPort paymentGatewayPort,
                                          DomainEventPublisher publisher) {
        this.chargeRepository = chargeRepository;
        this.paymentGatewayPort = paymentGatewayPort;
        this.publisher = publisher;
    }

    @Override
    @Transactional
    public Void execute(InitiateExternalChargeCommand command) {
        Long id = chargeRepository.nextIdentity();
        String reference = "INV-" + command.invoiceId() + "-" + UUID.randomUUID().toString().substring(0, 8);

        String checkoutUrl = paymentGatewayPort.initializeCharge(
            command.amount(), command.currency(), command.email(),
            reference, ChargePurpose.PLATFORM_INVOICE, command.redirectUrl()
        );

        PaystackCharge charge = PaystackCharge.initiate(
            id, command.invoiceId(), command.organizationId(),
            reference, checkoutUrl, command.amount(), command.currency(), ChargePurpose.PLATFORM_INVOICE
        );

        chargeRepository.save(charge);
        publishEvents(charge, publisher); // always after save
        return null;
    }
}
```

### 4.2 Commands

- Suffix: `Command` (e.g., `CheckoutSubscriptionCommand`).
- Package: `application/command`.
- Must be `record` types.
- Must carry typed fields — NO `String` where an enum exists.
- Fields that carry domain concepts must use domain types: `PaymentMethod paymentMethod`, not `String paymentMethod`.

### 4.3 Queries

- Suffix: `Query` (e.g., `GetOrganizationInvoicesQuery`).
- Package: `application/query`.
- Must be `record` types.

### 4.4 Results / DTOs

- Package: `application/result`.
- Must be `record` types.
- These are the output of use cases, returned through the controller as response bodies.

### 4.5 Outbound Ports (`application/port/out`)

- Interfaces that the application layer needs but whose implementation lives in infrastructure.
- Covers: external API integrations (e.g., `PaymentGatewayPort`), and infrastructure services (e.g., `EmailSenderPort`).
- Also used for Consumer-Driven cross-module queries where strict decoupling is required.

### 4.6 Inbound Ports for Cross-Module APIs (`application/port/in`)

- Used for **Provider-Driven Cross-Module Contracts** (Open Host Service).
- When a module (e.g., `identity`) needs to expose a stable public contract for other modules to query synchronously, it defines interfaces and DTOs in `application/port/in` (e.g., `OrganizationQueryPort`).
- Other modules declare a dependency on this module and call the Inbound Port to fetch data without coupling to internal `BaseUseCase` implementations.

```java
// Cross-module query port
public interface ProductQueryPort {
    Optional<ProductPricingDto> getProductPricing(Long productId);
}

// External service port
public interface PaymentGatewayPort {
    String initializeCharge(BigDecimal amount, String currency, String email,
                            String reference, ChargePurpose purpose, String redirectUrl);
}
```

---

## 5. Adapter Layer (Infrastructure)

### 5.1 JPA Entities

- Package: `adapter/out/persistence/entity`.
- Class naming: `<AggregateName>JpaEntity` (e.g., `BillingInvoiceJpaEntity`).
- MUST have `@AllArgsConstructor` and `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
- MUST have `@Getter` at class level (all fields are readable via getter).
- MUST have `@Table(name = "...", indexes = { @Index(...) })` with explicit indexes for every queryable column (foreign keys, enums used in WHERE clauses, email, reference, etc.).
- MUST use `@Id` with `@Column(nullable = false)` — IDs are application-managed (from `nextIdentity()`). NEVER use `@GeneratedValue`.
- `@Version` for optimistic locking MUST live ONLY on the JPA entity, never on the domain model.
- `@Setter` is placed ONLY on mutable fields. NEVER at class level unless every field is truly mutable.
- Enum columns MUST use `@Enumerated(EnumType.STRING)`.
- `createdAt` fields MUST have `updatable = false`.
- `Money` is stored as two separate columns: `amount DECIMAL(19,4)` and `currency VARCHAR(3)`. Never serialize Money as a JSON blob in the DB.
- `@ManyToOne` and `@OneToMany` are FORBIDDEN. All cross-aggregate relationships are modeled as IDs only.

```java
@Entity
@Table(name = "paystack_charges", indexes = {
    @Index(name = "idx_charge_reference", columnList = "reference", unique = true),
    @Index(name = "idx_charge_invoice_id", columnList = "invoice_id"),
    @Index(name = "idx_charge_organization_id", columnList = "organization_id")
})
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaystackChargeJpaEntity {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(name = "invoice_id", nullable = false)
    private Long invoiceId;

    @Column(name = "reference", nullable = false, unique = true)
    private String reference;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ChargeStatus status;

    @Setter
    @Column(name = "completed_at")
    private ZonedDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @Version
    private Long version;
}
```

### 5.2 Mappers

- Package: `adapter/out/persistence/mapper`.
- Class naming: `<AggregateName>Mapper` (e.g., `PaystackChargeMapper`).
- Annotated `@Component`.
- MUST have explicit `toDomain(JpaEntity)` and `toEntity(Domain)` methods.
- MUST NEVER call `aggregate.pullDomainEvents()`. Only use cases may pull domain events.
- MUST NOT contain any business logic.

```java
@Component
public class PaystackChargeMapper {
    public PaystackCharge toDomain(PaystackChargeJpaEntity entity) {
        return new PaystackCharge(entity.getId(), entity.getInvoiceId(), ...);
    }
    public PaystackChargeJpaEntity toEntity(PaystackCharge domain) {
        return new PaystackChargeJpaEntity(domain.getId(), domain.getInvoiceId(), ...);
    }
}
```

### 5.3 Repository Adapters

- Package: `adapter/out/persistence/repository`.
- Class naming: `<AggregateName>RepositoryAdapter` (e.g., `PaystackChargeRepositoryAdapter`).
- Implements the domain repository interface.
- Annotated `@Component`.
- Injects the Spring Data JPA interface and the mapper.
- `nextIdentity()` is implemented HERE by calling `sequenceGenerator.nextIdentity("sequence_name")`. This is the ONLY place `DomainSequenceGenerator` may be called.
- Sequence names must be seeded in `DomainSequenceGenerator.init()` via `seedSequence(...)`.

```java
@Component
public class PaystackChargeRepositoryAdapter implements PaystackChargeRepository {

    private final SpringDataPaystackChargeRepository jpa;
    private final PaystackChargeMapper mapper;
    private final DomainSequenceGenerator sequenceGenerator;

    public PaystackChargeRepositoryAdapter(SpringDataPaystackChargeRepository jpa,
                                            PaystackChargeMapper mapper,
                                            DomainSequenceGenerator sequenceGenerator) {
        this.jpa = jpa;
        this.mapper = mapper;
        this.sequenceGenerator = sequenceGenerator;
    }

    @Override
    public Long nextIdentity() {
        return sequenceGenerator.nextIdentity("paystack_charge_seq");
    }

    @Override
    public PaystackCharge save(PaystackCharge charge) {
        return mapper.toDomain(jpa.save(mapper.toEntity(charge)));
    }

    @Override
    public Optional<PaystackCharge> findByReference(String reference) {
        return jpa.findByReference(reference).map(mapper::toDomain);
    }
}
```

### 5.4 Spring Data JPA Interfaces

- Package: `adapter/out/persistence/repository`.
- Class naming: `SpringData<AggregateName>Repository` (e.g., `SpringDataPaystackChargeRepository`).
- Extends `JpaRepository<JpaEntity, Long>`.
- Annotated `@Repository`.
- Kept thin — NO business logic.

### 5.5 External Adapters

- Package: `adapter/out/external`.
- Implements outbound ports defined in `application/port/out`.
- Class naming: descriptive of the provider (e.g., `PaystackGatewayAdapter`).
- MUST handle HTTP errors and translate them into `ExternalServiceException`.
- MUST NOT let raw HTTP client exceptions propagate to the application layer.

---

## 6. REST Controllers

- Package: `adapter/in/web` (or `adapter/in/web/controller`).
- Annotated `@RestController` and `@RequestMapping("/api/v1/...")`.
- MUST inject and call the use case directly — no business logic in the controller.
- MUST use `ApiResponse<T>` wrapper for all responses.
- MUST use dedicated request/response DTOs — **NEVER define request or response classes inline inside a controller**.
- Request DTOs live in `adapter/in/web/request`. Response DTOs live in `adapter/in/web/response`.
- MUST NOT log.
- MUST NOT contain business logic.
- Authentication context (principal ID, org ID) is extracted from the security context or JWT, not from request bodies.

```java
@RestController
@RequestMapping("/api/v1/billing")
public class BillingController {

    private final CheckoutSubscriptionUseCase checkoutUseCase;

    public BillingController(CheckoutSubscriptionUseCase checkoutUseCase) {
        this.checkoutUseCase = checkoutUseCase;
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutSubscriptionResponse>> checkout(
            @RequestBody @Valid CheckoutSubscriptionRequest request,
            @AuthenticationPrincipal JwtAuthPrincipal principal) {

        CheckoutSubscriptionCommand command = new CheckoutSubscriptionCommand(
            principal.organizationId(),
            request.productId(),
            request.paymentMethod() // typed PaymentMethod enum
        );

        checkoutUseCase.execute(command);
        return ResponseEntity.accepted().body(ApiResponse.accepted("Checkout initiated"));
    }
}
```

---

## 7. Kafka Listeners (Inbound Messaging Adapters)

- Package: `adapter/in/messaging`.
- Extend `BaseKafkaEventListener` from `atlashub-shared`.
- Annotated `@Component`.
- Each listener class handles **one logical concern** (one event type or one tightly related group of events).
- MUST use `@RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)` above `@KafkaListener` for all critical consumers.
- MUST use the **typed overload** of `processEventIfMatches`:

```java
processEventIfMatches(payload, "EventTypeName", EventRecord.class, log, GROUP_ID, event -> {
    // event is fully typed - access event.payload().fieldName() directly
    useCase.execute(new SomeCommand(event.payload().invoiceId()));
});
```

- MUST NEVER use the `JsonNode` overload and then extract fields via `.path("payload").path("...").asLong()`. That overload only exists for cases where the payload type cannot be known statically.
- The Kafka consumer group ID MUST be a `private static final String GROUP_ID = "..."` constant defined at the top of the class.
- MUST register the subscription in the module's `*SubscriptionRegistrar` class (implements `ApplicationRunner` and calls `eventTrackerApi.registerSubscription(eventType, groupId)`).
- Listeners MUST NOT emit new events by calling `publisher.publish(...)` directly. If a downstream event must be published as a result of receiving an event, the listener calls a use case, and the use case publishes events via `publishEvents(aggregate, publisher)`.

```java
@Slf4j
@Component
public class WalletChargeResultListener extends BaseKafkaEventListener {

    private static final String GROUP_ID = "billing-module-wallet-group";

    private final FinalizeInvoicePaymentUseCase finalizeUseCase;
    private final HandleWalletChargeFailedUseCase failedUseCase;

    public WalletChargeResultListener(FinalizeInvoicePaymentUseCase finalizeUseCase,
                                       HandleWalletChargeFailedUseCase failedUseCase,
                                       ObjectMapper objectMapper) {
        super(objectMapper);
        this.finalizeUseCase = finalizeUseCase;
        this.failedUseCase = failedUseCase;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void onWalletChargeResult(String messagePayload) {
        processEventIfMatches(messagePayload, "WalletChargeSuccessfulEvent",
            WalletChargeSuccessfulEvent.class, log, GROUP_ID, event ->
                finalizeUseCase.execute(new FinalizeInvoicePaymentCommand(event.payload().invoiceId()))
        );
        processEventIfMatches(messagePayload, "WalletChargeFailedEvent",
            WalletChargeFailedEvent.class, log, GROUP_ID, event ->
                failedUseCase.execute(new HandleWalletChargeFailedCommand(event.payload().invoiceId(), event.payload().reason()))
        );
    }
}
```

---

## 8. Event Publishing (Outbound)

- All domain events flow through the **Transactional Outbox Pattern** implemented in `atlashub-infrastructure/eventbus`.
- `DomainEventPublisher` writes events to the `outbox_messages` table within the same DB transaction as the aggregate save. A scheduler polls the outbox and publishes to Kafka.
- Publishing happens ONLY via `publishEvents(aggregate, publisher)` in use cases, after `repository.save(aggregate)`.
- NEVER call `publisher.publish(...)` manually inside use cases, listeners, controllers, or mappers.
- NEVER call `EnvelopedDomainEvent.wrap(event)` manually — `publishEvents()` does this internally.

---

## 9. WebSocket Broadcasting

- Handled automatically by `DomainEventWebSocketBroadcaster` in `atlashub-infrastructure/notifications`.
- Any domain event published to a Kafka topic that the broadcaster subscribes to will be forwarded to the relevant frontend client via WebSocket at `/user/{aggregateId}/queue/events`.
- To broadcast a new event type: add its Kafka topic to the `@KafkaListener(topics = {...})` in `DomainEventWebSocketBroadcaster`.
- The `aggregateId` field of the domain event is used as the WebSocket routing key.

---

## 10. Cross-Module Communication

### 10.1 Async (Kafka) — the default
- Modules MUST communicate via events published to Kafka topics (via the outbox).
- Kafka topic names MUST use strictly **lowercase kebab-case** (e.g., `organization-events`, `billing-events`, `account-events`, `pay-events`). DO NOT use uppercase letters or camelCase.
- Module B defines its own copy of any event record it consumes from Module A.

### 10.2 Sync (In-Process, Shared Interface)
- Used sparingly for read-only, low-latency cross-module queries only.
- The consuming module defines the port interface in `application/port/out`.
- The providing module implements it and registers it as a Spring bean.
- NEVER inject a `@Service` class from module A directly into module B's use case.
- NEVER use "dummy" or "mock" adapter implementations in production code.

---

## 11. Sagas and Compensation

### 11.1 Saga Rules
- Sagas coordinate multi-step business processes across modules using async events.
- All sagas are **choreography-based** — no central orchestrator.
- Each step is idempotent (guarded by `eventTrackerApi.isProcessed(...)`).
- Every saga participant reacts to an inbound event by executing a use case and publishing one of: a success event or a failure event.

### 11.2 Compensation Rules
- Every saga step that mutates state MUST have a defined compensation path.
- Compensation is triggered by a `*Failed` event published by the failing step.
- Compensation use cases are named `Handle<FailedEvent>UseCase` (e.g., `HandleWalletChargeFailedUseCase`).
- Compensation must emit a domain event recording the outcome (e.g., `WalletChargeReversedEvent`).
- Never swallow failures silently in a saga.

---

## 12. Error Handling

- Each module defines its own `<Module>ErrorCode` enum implementing `ErrorCode` from `atlashub-shared` (e.g., `BillingErrorCode`, `ChargesErrorCode`).
- NEVER throw `RuntimeException`, `IllegalArgumentException`, or `IllegalStateException` for business errors.
- Use the correct exception type from `atlashub-shared`:
  - `BusinessRuleException` — a business rule was violated.
  - `NotFoundException` — an aggregate was not found.
  - `ConflictException` — a duplicate or idempotency conflict.
  - `ValidationException` — input failed format or constraint validation.
  - `AuthorizationException` — principal lacks permission.
  - `ExternalServiceException` — a third-party API call failed.
- Infrastructure exceptions from adapter layer MUST be caught and re-thrown as `ExternalServiceException`.

---

## 13. Money and Currency

- Always use the `Money` value object from `atlashub-shared`.
- NEVER use raw `BigDecimal` alone across domain/application boundaries.
- `Money.of(BigDecimal amount, CurrencyCode currency)` is the factory.
- Currency is always `CurrencyCode` (enum), never a raw `String`.
- In JPA entities, store as two columns: `amount DECIMAL(19,4)` and `currency VARCHAR(3)`.
- Use the registered `MoneyDeserializer` / `MoneySerializer` for JSON.

---

## 14. ID Generation

- All aggregate IDs are `Long`, generated by `DomainSequenceGenerator` via a `domain_sequences` table.
- `DomainSequenceGenerator.nextIdentity(String sequenceName)` is called ONLY inside repository adapter implementations (`nextIdentity()` method).
- Every new aggregate's sequence MUST be seeded in `DomainSequenceGenerator.init()` via `seedSequence("my_aggregate_seq", startingValue)`.
- NEVER use `@GeneratedValue` or auto-increment on JPA entities.
- NEVER call `DomainSequenceGenerator` from a use case, controller, listener, or domain model.

---

## 15. No Hardcoded Strings

- Any string that represents a finite set of values MUST be an enum in `domain/valueobject`.
- Configuration values (URLs, secrets, API keys, limits) MUST come from `application.yml` via `@Value("${...}")` or `@ConfigurationProperties`. Never hardcode them.
- The frontend base URL is always `${atlashub.frontend.url}`.
- Kafka consumer group IDs in listeners MUST be `private static final String GROUP_ID = "..."` constants.

---

## 16. Code Style

- **NO INLINE IMPORTS**: All imports MUST be at the top of the file. Never use fully-qualified class names inline in code bodies.
- **NO INLINE REQUEST/RESPONSE DTOS**: Never define request or response classes as inner classes inside controllers.
- Use constructor injection always. Never use field injection (`@Autowired` on a field).
- All collections returned from domain methods must be wrapped in `Collections.unmodifiableList(...)` or `List.copyOf(...)`.
- `ZonedDateTime.now()` is used for all timestamps. Never `LocalDateTime`.
- All `@Column` annotations MUST have an explicit `name = "snake_case_column_name"`.
- All non-nullable `@Column` definitions MUST have `nullable = false`.

---

## 17. Logging Rules

- Use `@Slf4j` (Lombok) in use cases and infrastructure adapters.
- Log `INFO` for significant business events. Log `DEBUG` for verbose details. Log `WARN` for recoverable unexpected conditions. Log `ERROR` with the exception object for failures.
- NEVER log sensitive data (passwords, tokens, card numbers, PII).
- NEVER place `@Slf4j` or logging inside domain model classes or controllers.

---

## 18. Documentation

- Any new module, use case, endpoint, aggregate, event, or renamed concept MUST be immediately reflected in:
  - `docs/design.md` — overall architecture and module list.
  - `docs/modules/<module-name>.md` — module-specific design.
  - `docs/domain-glossary.md` — new domain terms.
- Folder structure changes must be documented in `docs/design.md` and `docs/module-design-template.md`.

---

## 19. Git Commits

- Commit in small, logical, atomic chunks. One concern per commit.
- Format: `feat(<module>): <what>`, `fix(<module>): <what>`, `refactor(<module>): <what>`, `chore: <what>`.
- Examples:
  - `feat(billing): add CheckoutSubscriptionUseCase`
  - `feat(charges): introduce PaystackCharge aggregate with lifecycle methods`
  - `fix(ledger): switch WalletChargeRequestedListener to typed event overload`
  - `refactor(billing): replace hardcoded strings with PaymentMethod enum`
- Do NOT lump multiple features, refactors, and fixes into a single commit.

---

## 20. What NEVER to Do — Quick Reference

| Never | Instead |
|---|---|
| Call `DomainSequenceGenerator` from a use case | Call `repository.nextIdentity()` |
| Manually call `publisher.publish(EnvelopedDomainEvent.wrap(...))` | Call `publishEvents(aggregate, publisher)` |
| Create domain events outside domain aggregate methods | Use `registerEvent(...)` inside aggregate business methods |
| Call `aggregate.pullDomainEvents()` in a mapper | Only call via `publishEvents(...)` in use cases |
| Use raw `String` for finite-set values | Define an enum in `domain/valueobject` |
| Use `JsonNode` overload in Kafka listeners | Use the typed `EventClass.class` overload |
| Put `@Setter` at class level on a JPA entity | Put `@Setter` only on mutable fields |
| Inject a `@Service` from module A into module B | Define and inject a port interface |
| Use `@GeneratedValue` or auto-increment | Use `nextIdentity()` via `DomainSequenceGenerator` in repository adapter |
| Put business logic in a controller | Delegate to a use case |
| Define request/response DTOs inline in a controller | Use `adapter/in/web/request` and `adapter/in/web/response` packages |
| Import types from another module's package | Define a local copy or a shared port type |
| Hardcode URLs, secrets, or config values | Use `@Value("${...}")` from `application.yml` |
| Throw `RuntimeException` for business errors | Throw the correct domain exception with an `ErrorCode` |
| Use `@ManyToOne` / `@OneToMany` in JPA entities | Store IDs only; join in query layer |
| Use inline imports (fully-qualified names in code body) | Add all imports at the top of the file |
| Call `super(id)` in an aggregate constructor | `AggregateRoot` has no-arg constructor; do not call super with args |
| Call `super(publisher)` in a use case constructor | `BaseUseCase` has no-arg constructor |
