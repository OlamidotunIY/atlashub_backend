# AtlasHub Backend — Architecture Rules & Conventions

> **This file is the canonical reference for all AI agents and contributors.**
> Every module MUST follow these rules exactly. When in doubt, look at the
> `authentication` module — it is the gold-standard case study.

---

## 0. Non-Negotiable Directives

- **Do nothing you were not asked to do.** If something is unclear, ask before acting.
- **Never guess at structure.** If a pattern is unclear, read the existing auth module and copy it.
- **Do not duplicate or rename layers.** Package names are exact — do not create variations.
- **The repository `save()` publishes domain events automatically** via `JpaBaseRepository`.
  Handlers MUST NOT call `publishEvents()` or `eventPublisher.publish()` themselves.
- **Do not add Spring annotations to the domain layer** (entities, value objects, domain services,
  domain repositories). That layer is framework-free.

---

## 1. Architecture Overview

AtlasHub uses **Clean Architecture + Domain-Driven Design (DDD) + EIP** principles in a
**modular monolith**. The dependency direction is always inward:

```
Presentation → Application → Domain ← Infrastructure
```

Cross-module communication:
- **Reads**: via port interfaces defined in `atlashub-shared` (e.g. `UserQueryPort`)
- **State changes**: via domain events published to Kafka via the outbox pattern

---

## 2. Module Structure

Every platform module (`authentication`, `accounts`, etc.) MUST have this exact package layout:

```
com.atlashub.<module>/
├── application/
│   ├── command/
│   │   └── <CommandName>/
│   │       ├── <CommandName>Command.java      ← input record
│   │       ├── <CommandName>Handler.java      ← use case class
│   │       └── <CommandName>Result.java       ← output record (if needed)
│   ├── query/
│   │   └── <QueryName>/
│   │       ├── <QueryName>Query.java          ← input record
│   │       ├── <QueryName>Handler.java        ← use case class
│   │       └── <QueryName>Result.java         ← output record
│   └── port/
│       └── <PortName>Port.java                ← outbound port interfaces
├── domain/
│   ├── entities/                              ← Aggregate Roots
│   ├── events/                                ← Domain Events
│   ├── exceptions/                            ← Domain Exceptions
│   ├── ports/                                 ← Domain-level port interfaces (e.g. OtpGenerator)
│   ├── repositories/                          ← Domain Repository interfaces
│   ├── services/                              ← Domain Services (no Spring annotations)
│   └── valueobject/                           ← Value Objects / Enums
├── infrastructure/
│   ├── messaging/
│   │   ├── events/                            ← Kafka event payload records
│   │   └── listeners/                         ← Kafka listener classes
│   ├── persistence/
│   │   ├── adapters/                          ← Repository adapter implementations
│   │   ├── entities/                          ← JPA entities
│   │   ├── mappers/                           ← MapStruct mapper interfaces
│   │   └── repositories/                      ← Spring Data JPA interfaces
│   ├── security/                              ← Security adapters (JWT, session, password, revocation)
│   ├── cache/                                 ← Redis adapters (if any)
│   └── services/                              ← Other infrastructure service adapters
└── presentation/
    ├── dto/                               ← Request/Response DTOs (NEVER inside the controller, NEVER inside rest/)
    └── rest/
        └── <ModuleName>Controller.java    ← REST controller
```

---

## 3. Domain Layer Rules

### 3.1 Aggregate Root

Extends `AggregateRoot<Long>` from shared. Must:
- Have a private/package-private all-args constructor (for MapStruct)
- Have a public static `create(...)` factory method that enforces invariants
- Override `getId()`
- Use `registerEvent(...)` to record domain events (published automatically on `save()`)
- NEVER have Spring annotations (`@Component`, `@Service`, etc.)

**Case study: [`AuthAccount.java`](atlashub-platform/authentication/src/main/java/com/atlashub/authentication/domain/entities/AuthAccount.java)**

```java
@Getter
public class AuthAccount extends AggregateRoot<Long> {
    private final Long id;
    // ... fields ...

    // All-args constructor (used by MapStruct)
    public AuthAccount(Long id, Long userId, ...) { ... }

    // Static factory — enforces invariants
    public static AuthAccount create(Long id, Long userId, String email, String passwordHash) {
        if (id == null || userId == null) throw new AuthInvaraintError("...");
        return new AuthAccount(id, userId, email, passwordHash, false, 0, null, null, null, ZonedDateTime.now());
    }

    // Behaviour methods
    public void verifyEmail() { ... }
    public void recordFailedLogin() { ... }
    public void resetPassword(String newHash) { ... }

    @Override
    public Long getId() { return id; }
}
```

### 3.2 Domain Repository Interface

Extends `Repository<TDomain>` from shared. Lives in `domain/repositories/`. No Spring annotations.

```java
public interface AuthAccountRepository extends Repository<AuthAccount> {
    Optional<AuthAccount> findByEmail(String email);
    Optional<AuthAccount> findByUserId(Long userId);
}
```

### 3.3 Domain Events

Use the project's `DomainEvent<TPayload>` base. Registered via `aggregateRoot.registerEvent(...)`.

### 3.4 Domain Services

Live in `domain/services/`. No Spring annotations — they are declared as `@Bean` in
`ApplicationConfig` in the main module.

```java
public class OtpVerificationIssuer {
    // Constructor injected manually via @Bean in ApplicationConfig
    public OtpVerificationIssuer(OtpGenerator generator, PasswordEncoderPort encoder) { ... }
}
```

---

## 4. Application Layer Rules

### 4.1 Commands

**Command input record** — a plain Java record. No annotations. No business logic.

```java
public record LoginCommand(String email, String password, String deviceFingerprint,
                           String ipAddress, String userAgent) {}
```

**Command handler** — extends `Command<I, O>`. Annotated `@Component`. Contains business orchestration.

```java
@Component
public class LoginHandler extends Command<LoginCommand, LoginResponse> {

    // Constructor injection — all fields final
    private final AuthAccountRepository accountRepository;
    // ...

    @Override
    public LoginResponse execute(LoginCommand input) {
        // business orchestration
    }
}
```

### 4.2 Queries

**Query input record** — plain record.

```java
public record GetActiveSessionsQuery(Long userId) {}
```

**Query handler** — extends `Query<I, O>`. Annotated `@Component`.

```java
@Component
public class GetActiveSessionsHandler extends Query<GetActiveSessionsQuery, List<SessionResult>> {
    @Override
    public List<SessionResult> execute(GetActiveSessionsQuery query) { ... }
}
```

### 4.3 Result Records

Plain records in the same package as the handler. Example:

```java
public record SessionResult(String refreshTokenHash, ZonedDateTime accessTokenExpiresAt,
                            ZonedDateTime refreshTokenExpiresAt, Long deviceId, Long orgId) {}
```

### 4.4 Application Ports

Outbound port interfaces live in `application/port/`. No Spring annotations.

```java
public interface SessionPort {
    void save(Session session);
    Optional<Session> findByTokenHash(String tokenHash);
    void delete(String tokenHash);
    Set<Session> findAllByAuthAccountId(Long authAccountId);
    void deleteAllForUser(Long authAccountId);
}
```

---

## 5. Infrastructure Layer Rules

### 5.1 JPA Entity

Lives in `infrastructure/persistence/entities/`. Rules:
- Implements `BaseJpaEntity` from shared
- Lombok: `@Getter`, `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, `@AllArgsConstructor`
- `@Entity` + `@Table` with explicit name and indexes
- `@Id` on the `Long id` field (no `@GeneratedValue` — IDs come from the sequence generator)
- `@Version private Long version` at the bottom for optimistic locking
- Field names use camelCase; column names use `snake_case` via `@Column(name = "...")`
- Only use `nullable = false` in `@Column` when the field is genuinely non-null in the DB
- **No relationships (`@OneToMany`, `@ManyToOne`, etc.)** — cross-entity joins done via IDs

**Case study: [`AuthAccountJpa.java`](atlashub-platform/authentication/src/main/java/com/atlashub/authentication/infrastructure/persistence/entities/AuthAccountJpa.java)**

```java
@Entity
@Table(
    name = "auth_accounts",
    indexes = {
        @Index(name = "Idx_auth_user_id", columnList = "user_id", unique = true),
        @Index(name = "Idx_auth_email", columnList = "email", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AuthAccountJpa implements BaseJpaEntity {

    @Id
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column
    private String email;

    @Column
    private String passwordHash;

    @Column
    private Boolean emailVerified;

    @Column
    private int failedLoginAttempts;

    @Column(nullable = false)
    private ZonedDateTime lockedUntil;

    // ... remaining fields ...

    @Version
    private Long version;
}
```

### 5.2 MapStruct Mapper

Lives in `infrastructure/persistence/mappers/`. Rules:
- Extends `DomainMapper<TDomain, TJpa>` from shared
- **Always include `uses = {ValueObjectMapper.class}`** to handle value object mapping
- Do NOT write manual mapping methods unless MapStruct cannot handle it automatically

**Case study: [`AuthAccountMapper.java`](atlashub-platform/authentication/src/main/java/com/atlashub/authentication/infrastructure/persistence/mappers/AuthAccountMapper.java)**

```java
@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {ValueObjectMapper.class}
)
public interface AuthAccountMapper extends DomainMapper<AuthAccount, AuthAccountJpa> {
}
```

### 5.3 Spring Data Repository

Lives in `infrastructure/persistence/repositories/`. Extends `JpaRepository<TJpa, Long>`.
Declare only the query methods you need.

```java
public interface SpringDataAuthAccountRepository extends JpaRepository<AuthAccountJpa, Long> {
    Optional<AuthAccountJpa> findByEmail(String email);
    Optional<AuthAccountJpa> findByUserId(Long userId);
}
```

### 5.4 Repository Adapter

Lives in `infrastructure/persistence/adapters/`. Rules:
- Extends `JpaBaseRepository<TDomain, TJpa>` from shared
- Implements the domain repository interface
- Annotated `@Component`
- Constructor passes all four args (`springDataRepo`, `mapper`, `sequenceGenerator`, `eventPublisher`) to `super(...)`
- Holds `private final SpringDataXxxRepository springDataRepo` for custom queries
- Override `getSequenceName()` with the DB sequence name
- Implement each custom query method by delegating to `springDataRepo` and mapping via `mapper::toDomain`
- **The inherited `save()` automatically publishes domain events — never do this manually**

**Case study: [`AuthAccountRepositoryAdapter.java`](atlashub-platform/authentication/src/main/java/com/atlashub/authentication/infrastructure/persistence/adapters/AuthAccountRepositoryAdapter.java)**

```java
@Component
public class AuthAccountRepositoryAdapter
        extends JpaBaseRepository<AuthAccount, AuthAccountJpa>
        implements AuthAccountRepository {

    private final SpringDataAuthAccountRepository springDataRepo;

    public AuthAccountRepositoryAdapter(SpringDataAuthAccountRepository springDataRepo,
                                        AuthAccountMapper mapper,
                                        DomainSequenceGenerator sequenceGenerator,
                                        DomainEventPublisher eventPublisher) {
        super(springDataRepo, mapper, sequenceGenerator, eventPublisher);
        this.springDataRepo = springDataRepo;
    }

    @Override
    protected String getSequenceName() {
        return "auth_account_seq";
    }

    @Override
    public Optional<AuthAccount> findByEmail(String email) {
        return springDataRepo.findByEmail(email).map(mapper::toDomain);
    }

    @Override
    public Optional<AuthAccount> findByUserId(Long userId) {
        return springDataRepo.findByUserId(userId).map(mapper::toDomain);
    }
}
```

### 5.5 Cross-Module Query Port Adapters

Live in `infrastructure/services/`. Implement port interfaces defined in `atlashub-shared`.
Query directly from the Spring Data repository without going through the domain mapper —
they return DTOs, not domain objects.

**Case study: [`UserQueryPortAdapter.java`](atlashub-platform/accounts/src/main/java/com/atlashub/accounts/infrastructure/services/UserQueryPortAdapter.java)**

```java
@Component
public class UserQueryPortAdapter implements UserQueryPort {

    private final SpringDataUserRepository springDataRepo;

    public UserQueryPortAdapter(SpringDataUserRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public Optional<UserDto> findById(Long userId) {
        return springDataRepo.findById(userId).map(this::toDto);
    }

    // ... other methods ...

    private UserDto toDto(UserJPA jpa) {
        return new UserDto(jpa.getId(), jpa.getFirstName(), ...);
    }
}
```

### 5.6 Other Infrastructure Adapters

Security adapters (`SessionAdapter`, `JwtTokenAdapter`, `TokenRevocationAdapter`,
`PasswordEncoderAdapter`, `OtpTransmissionAdapter`) and service adapters (`OtpGeneratorAdapter`)
live in their respective sub-packages. All must be annotated `@Component`.

### 5.7 Kafka Listeners

Live in `infrastructure/messaging/listeners/`. Must be annotated `@Component`.
Extend `BaseKafkaEventListener`. Register subscriptions in `@PostConstruct init()`.

---

## 6. Presentation Layer Rules

### 6.1 URL Conventions

- Base path is always `/api/v1` — no module name in the URL.
- Use **plural nouns** for resource collections: `/organizations`, `/devices`, `/sessions`.
- If the **last segment of the URL would be an ID**, use a **query parameter** instead of a path variable.
- If the ID is **not** the last segment (i.e. there is a sub-resource after it), keep it as a path variable.

```
✅  GET  /api/v1/organizations?id=123          ← ID is last → query param
✅  PUT  /api/v1/organizations?id=123
✅  DELETE /api/v1/devices?id=456

✅  GET  /api/v1/organizations/{id}/members    ← sub-resource follows → path variable
✅  POST /api/v1/login                         ← no ID at all → just the action
```

### 6.2 Controller

Lives in `presentation/rest/`. Rules:
- Annotated `@RestController`, `@RequestMapping("/api/v1")`, and Swagger `@Tag`
- All dependencies injected via constructor (no field injection)
- Each endpoint annotated with the HTTP method, `@Operation` (Swagger), and
  `@SecurityRequirement(name = "bearerAuth")` for authenticated endpoints
- Public endpoints also annotated with `@PublicEndpoint`
- `@AuthenticationPrincipal Long userId` extracts the authenticated user ID from the JWT
- The controller maps DTOs → commands/queries, calls the handler, wraps result in `ApiResponse`
- **NO logic in the controller beyond mapping and delegation**
- **NO inner classes or records** — all DTOs must live in the `dto` package

### 6.3 DTOs

Live in `presentation/dto/`. Rules:
- Plain Java records
- Request DTOs use `jakarta.validation` annotations (`@NotBlank`, `@NotNull`, `@Email`, `@Size`, etc.)
- One file per DTO
- Naming convention: `<Action>Request.java`, `<Action>Response.java`

```
presentation/
├── dto/
│   ├── LoginRequest.java
│   ├── RefreshTokenRequest.java
│   ├── VerifyEmailRequest.java
│   ├── EmailRequest.java
│   ├── ResetPasswordRequest.java
│   ├── LogoutRequest.java
│   └── ChangePasswordRequest.java
└── rest/
    └── AuthController.java
```

### 6.4 ApiResponse Wrapper

All controller methods return `ResponseEntity<ApiResponse<T>>`. Use the shared
`ApiResponse` record: `new ApiResponse<>(true, "message", data, null)`.

---

## 7. Shared Module Rules

### 7.1 Cross-Module Ports (`atlashub-shared/application/port/`)

- `UserQueryPort` — implemented by `accounts` module
- `OrganizationQueryPort` — implemented by `accounts` module
- `MembershipQueryPort` — implemented by `accounts` module
- `ApiKeyQueryPort` — implemented by relevant module

Ports are plain Java interfaces with inner `record` DTOs for return types.

### 7.2 No Cross-Module Repository Access

A module MUST NOT inject another module's repository, JPA entity, or domain class.
Cross-module reads MUST go through the shared query port interfaces.

---

## 8. ApplicationConfig Rules (Main Module)

- `ObjectMapper`, `PasswordEncoder`, `RestTemplate` — always present
- **Domain services** (e.g. `OtpVerificationIssuer`) — declared as `@Bean` here because
  they live in the domain layer and must not have Spring annotations
- **Swagger/OpenAPI** `OpenAPI` bean — declared here with `bearerAuth` security scheme
- **Handlers are NOT declared here** — they are `@Component` on the class itself

---

## 9. SecurityConfig Rules

- `SessionCreationPolicy.STATELESS` always
- Public URL paths listed explicitly in `requestMatchers(...).permitAll()`
- `JwtAuthenticationFilter` added before `UsernamePasswordAuthenticationFilter`
- Swagger paths (`/swagger-ui/**`, `/v3/api-docs/**`) always public
- `/actuator/health` always public

---

## 10. Key Anti-Patterns to Avoid

| ❌ Wrong | ✅ Correct |
|----------|-----------|
| `@Service` on a handler | `@Component` on a handler |
| Handler calls `eventPublisher.publish(...)` | `repository.save()` publishes events automatically |
| `BaseUseCase` on any handler | `Command<I,O>` or `Query<I,O>` |
| DTOs as inner records in the controller | DTOs in `presentation/rest/dto/` package |
| Cross-module repository injection | Use shared query port interface |
| Manual MapStruct mapping without `uses = {ValueObjectMapper.class}` | Always include `uses = {ValueObjectMapper.class}` |
| `@GeneratedValue` on JPA `@Id` | No `@GeneratedValue` — use sequence generator |
| JPA relationships (`@OneToMany`, etc.) | Store IDs only, join at query time |
| Domain service annotated `@Component` | Domain service declared as `@Bean` in `ApplicationConfig` |
| `publishEvents(...)` in handler | Repository `save()` handles it |
| `new ObjectMapper()` without JavaTimeModule | Always register `JavaTimeModule` |
