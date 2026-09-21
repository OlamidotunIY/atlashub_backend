# Accounts Module Design (`atlashub-platform:accounts`)

## Role & Purpose

The `accounts` module is the **identity foundation** of AtlasHub. It owns exactly two things: **who a person is** (`User`) and **what organization they represent** (`Organization`). Nothing else.

This module does **not** handle:
- How you log in (that is `auth`)
- Whether your business is verified (that is `compliance`)
- What you are allowed to do (that is `iam`)
- What products you have subscribed to (that is `billing`)

This strict boundary prevents the "god module" anti-pattern. The `accounts` module is intentionally narrow. It answers one question: *who exists on this platform?*

Other modules reference `userId` and `organizationId` as foreign keys. They never import `accounts` internal classes — they query via the `OrganizationQueryPort` or `UserQueryPort` interfaces defined in `atlashub-shared`.

---

## 1. Features

### User Registration
A `User` represents a human being on the AtlasHub platform. A user can belong to multiple organizations (as a member). The `activeOrganizationId` field tracks which org context they are currently operating in.

### Organization Registration
An `Organization` represents a business entity on AtlasHub. On creation, the founding `User` becomes the first member via the `iam` module. An organization carries its `country` and `baseCurrency`, which determine the currency used in billing, pay, and accounting.

### Atomic Registration
`RegisterOrganizationUseCase` creates both `User` and `Organization` in a single database transaction. It publishes `UserCreatedEvent` and `OrganizationCreatedEvent`, which downstream modules (`auth`, `iam`, `billing`) react to.

### Profile Management
Users can update their profile. Organizations can update their business details and logo.

### Active Organization Switching
A user who belongs to multiple organizations can switch their active context (`SwitchActiveOrganizationUseCase`). The `activeOrganizationId` field drives which org's data is loaded on login.

---

## 2. Domain Entities & Aggregates

### `User` (Aggregate Root)

Represents a human on the platform. Has no status field — activation state is owned by `auth:AuthAccount`.

```
User
├── id: Long
├── firstName: String
├── lastName: String
├── email: EmailAddress          ← value object, validates format
├── phone: PhoneNumber           ← value object, nullable
├── imageUrl: String             ← nullable
├── country: Country             ← value object (ISO 3166-1 alpha-2, e.g. "NG", "KE")
├── locale: String               ← e.g. "en-NG"
├── timezone: String             ← e.g. "Africa/Lagos"
├── activeOrganizationId: Long   ← nullable, set when user joins their first org
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateProfile(String firstName, String lastName, PhoneNumber phone, String timezone)` → registers `UserProfileUpdatedEvent`
- `updateImageUrl(String imageUrl)`
- `switchActiveOrganization(Long organizationId)` → registers `UserActiveOrganizationChangedEvent`

**Domain Rules:**
- `email` must be unique across all users (enforced by DB unique index + pre-check in use case)
- `country` is immutable after registration — it determines base currency for all financial activity

---

### `Organization` (Aggregate Root)

Represents a registered business entity. Has no separate `status` field — the organization's operational capability is gated by `compliance:ComplianceStatus` and `billing:SubscriptionStatus`, both of which are queried via their own ports.

```
Organization
├── id: Long
├── businessName: String
├── businessType: BusinessType   ← SOLE_PROPRIETOR, LIMITED_LIABILITY, PARTNERSHIP, NGO, ENTERPRISE
├── businessSize: BusinessSize   ← MICRO (1-9), SMALL (10-49), MEDIUM (50-249), LARGE (250+)
├── industry: String
├── description: String          ← nullable
├── logoUrl: String              ← nullable
├── websiteUrl: String           ← nullable
├── country: Country             ← immutable after creation
├── baseCurrency: Currency       ← derived from country on creation (NG→NGN, KE→KES)
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateDetails(String businessName, String description, String websiteUrl)` → registers `OrganizationUpdatedEvent`
- `updateLogo(String logoUrl)`

**Domain Rules:**
- `country` and `baseCurrency` are immutable — changing them would invalidate all historical financial records
- `businessName` must be non-empty and ≤ 200 characters

---

## 3. Value Objects

### `EmailAddress`
```java
public record EmailAddress(String value) {
    public EmailAddress {
        Objects.requireNonNull(value);
        if (!value.matches("^[\\w+\\-.]+@[a-z\\d\\-.]+\\.[a-z]+$"))
            throw new ValidationException(AccountsErrorCode.INVALID_EMAIL_FORMAT, "Invalid email: " + value);
    }
}
```

### `PhoneNumber`
```java
public record PhoneNumber(String value) {
    public PhoneNumber {
        // E.164 format: +234XXXXXXXXXX
        if (!value.matches("^\\+[1-9]\\d{6,14}$"))
            throw new ValidationException(AccountsErrorCode.INVALID_PHONE_FORMAT, "Invalid phone: " + value);
    }
}
```

### `Country`
```java
public record Country(String code) {
    // ISO 3166-1 alpha-2
    private static final Set<String> SUPPORTED = Set.of("NG", "KE", "GH", "ZA", "US");
    public Country {
        if (!SUPPORTED.contains(code))
            throw new ValidationException(AccountsErrorCode.UNSUPPORTED_COUNTRY, "Unsupported country: " + code);
    }
    public Currency deriveCurrency() {
        return switch (code) {
            case "NG" -> Currency.NGN;
            case "KE" -> Currency.KES;
            case "GH" -> Currency.GHS;
            case "ZA" -> Currency.ZAR;
            case "US" -> Currency.USD;
            default -> throw new BusinessRuleException(AccountsErrorCode.UNSUPPORTED_COUNTRY, code);
        };
    }
}
```

### `BusinessType` (Enum)
`SOLE_PROPRIETOR`, `LIMITED_LIABILITY`, `PARTNERSHIP`, `NGO`, `ENTERPRISE`

### `BusinessSize` (Enum)
`MICRO` (1–9 employees), `SMALL` (10–49), `MEDIUM` (50–249), `LARGE` (250+)

---

## 4. Domain Events

All events are written to the outbox within the same DB transaction that mutates the aggregate.

| Event | Published When | Consumed By |
|---|---|---|
| `UserCreatedEvent` | New user registers | `auth` (create AuthAccount), `notifications` (welcome email) |
| `UserProfileUpdatedEvent` | User updates name/phone | `hr` (sync employee name if linked) |
| `UserActiveOrganizationChangedEvent` | User switches active org | Internal — no downstream consumers |
| `OrganizationCreatedEvent` | New org registers | `iam` (create OWNER membership), `billing` (init subscription state), `compliance` (init KYC record) |
| `OrganizationUpdatedEvent` | Org updates business details | `notifications` (if name changed, alert members) |

---

## 5. Outbound Ports (Open Host Service)

These interfaces are defined in `atlashub-shared` and implemented in this module. Other modules inject them for sync reads.

```java
// atlashub-shared / application/port/in
public interface UserQueryPort {
    Optional<UserDto> findById(Long userId);
    Optional<UserDto> findByEmail(String email);
    boolean existsById(Long userId);
}

public interface OrganizationQueryPort {
    Optional<OrganizationDto> findById(Long orgId);
    boolean existsById(Long orgId);
    String getBaseCurrency(Long orgId);    // returns "NGN", "KES", etc.
    String getCountry(Long orgId);         // returns "NG", "KE", etc.
}
```

---

## 6. Exceptions & Errors

**`AccountsErrorCode`** (implements `ErrorCode`):
- `USER_NOT_FOUND`, `ORGANIZATION_NOT_FOUND`
- `EMAIL_ALREADY_REGISTERED`
- `INVALID_EMAIL_FORMAT`, `INVALID_PHONE_FORMAT`
- `UNSUPPORTED_COUNTRY`
- `ORGANIZATION_NOT_FOUND`

---

## 7. Commands & Use Cases

### Registration
- `RegisterOrganizationCommand(firstName, lastName, email, phone, country, businessName, businessType, businessSize, industry)` → `RegisterOrganizationUseCase`
  - Creates `User`, `Organization` in one transaction
  - Publishes `UserCreatedEvent` + `OrganizationCreatedEvent`
  - Idempotency: pre-checks email uniqueness before creation

### Profile Updates
- `UpdateUserProfileCommand(userId, firstName, lastName, phone, timezone)` → `UpdateUserProfileUseCase`
- `UpdateOrganizationDetailsCommand(orgId, businessName, description, websiteUrl)` → `UpdateOrganizationDetailsUseCase`
- `UpdateOrganizationLogoCommand(orgId, logoUrl)` → `UpdateOrganizationLogoUseCase`
- `SwitchActiveOrganizationCommand(userId, orgId)` → `SwitchActiveOrganizationUseCase`
  - Guards: user must be a member of the target org (checked via `iam:MembershipQueryPort`)

---

## 8. Queries

- `GetUserProfileQuery(Long userId)` → `UserProfileResult`
- `GetOrganizationDetailsQuery(Long orgId)` → `OrganizationDetailsResult`
- `ListUserOrganizationsQuery(Long userId)` → `List<OrganizationSummaryResult>` — all orgs the user is a member of

---

## 9. Listeners

- **`SubscriptionSuspendedListener`**: Listens to `SubscriptionSuspendedEvent` from `billing`. Logs the suspension. Access enforcement is handled by `iam` — this listener exists only for audit.

---

## 10. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to both `UserJpaEntity` and `OrganizationJpaEntity`. Prevents concurrent profile update conflicts.

### Outbox & Inbox
- **Outbox**: `UserCreatedEvent` and `OrganizationCreatedEvent` are written to the outbox within the same transaction. This guarantees that even if `auth` or `iam` are temporarily down, they will eventually receive and process these events.
- **No Inbox needed**: This module does not consume any cross-module events that require idempotent processing.

### Idempotency
- `RegisterOrganizationUseCase` checks for email uniqueness before creating the user. If a duplicate registration is attempted concurrently, the DB unique constraint on `email` serves as the final guard.
