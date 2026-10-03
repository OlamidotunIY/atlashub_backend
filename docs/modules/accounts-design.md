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

`accounts` records the organization's legal registration classification and product-facing industry. It does **not** decide whether the organization is eligible for banking or verified by Anchor; those decisions belong to `compliance`.

For the initial Nigerian banking programme, AtlasHub accepts only `SOLE_PROPRIETORSHIP` and `PRIVATE_LIMITED_COMPANY`. Unsupported registration types are rejected during registration so an organization is not allowed to complete an onboarding journey that can never receive a supported banking product.

### Atomic Registration
`RegisterOrganizationHandler` creates both `User` and `Organization` in a single database transaction. It publishes `UserCreated` and `OrganizationRegistered` domain events, which downstream modules (`auth`, `iam`, `billing`, `compliance`) react to asynchronously.

### Profile Management
Users can update their profile. Organizations can update their business details and logo.

### Active Organization Context
Authentication owns organization switching because it must atomically rotate the session and access token with the target organization's permissions. Accounts consumes `ActiveOrganizationSwitchedEvent` and updates `User.activeOrganizationId` as the durable profile preference.

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
├── activeOrganizationId: Long   ← nullable, set when user joins their first org
├── emailVerified: Boolean       ← set to true after OTP verification; synced from auth via AuthEmailVerifiedEvent
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `create(id, firstName, lastName, email, country, isInvited, passwordHash)` — static factory; validates all required fields and password strength; raises `UserCreated` event
- `updateProfile(firstName, lastName, phone)` → registers `UserProfileUpdated` event
- `updateImageUrl(imageUrl)`
- `switchActiveOrganization(organizationId)` → registers `UserActiveOrganizationChanged` event
- `markEmailVerified()` — called by `AuthEventListener` when `AuthEmailVerifiedEvent` arrives; sets `emailVerified = true`

**Domain Rules:**
- `email` must be unique across all users (enforced by DB unique index + pre-check in handler)
- `country` is immutable after registration — it determines base currency for all financial activity
- Password strength validated in `User.create()`: min 8 chars, requires uppercase, lowercase, digit, special character

---

### `Organization` (Aggregate Root)

Represents a registered business entity. Has no separate `status` field — the organization's operational capability is gated by `compliance:ComplianceStatus` and `billing:SubscriptionStatus`, both of which are queried via their own ports.

```
Organization
├── id: Long
├── businessName: String
├── registrationType: AtlasHubRegistrationType
├── registrationDate: LocalDate          ← generated on AtlasHub registration
├── industry: SupportedIndustry
├── description: String          ← nullable
├── logoUrl: String              ← nullable
├── websiteUrl: String           ← nullable
├── country: Country             ← immutable after creation
├── baseCurrency: CurrencyCode   ← derived from country on creation (NG→NGN, KE→KES)
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `create(id, businessName, registrationType, description, currency, logoUrl, country, industry, websiteUrl, ownerUserId)` — generates `registrationDate` from the current AtlasHub date and raises `OrganizationRegistered`
- `updateOrganization(businessName, description, logoUrl, industry, websiteUrl)` → registers `OrganizationUpdated` event

**Domain Rules:**
- `country` and `baseCurrency` are immutable — changing them would invalidate all historical financial records
- `businessName` must be non-empty
- `registrationType` must be enabled by the AtlasHub onboarding policy
- Initial supported types are `SOLE_PROPRIETORSHIP` and `PRIVATE_LIMITED_COMPANY`
- `industry` must be selected from AtlasHub's supported-industry allowlist; arbitrary strings are not accepted
- Legal registration identity is immutable in accounts after registration. Corrections require a dedicated compliance amendment workflow rather than a generic organization-profile update.

---

## 3. Aggregate Root: `Outlet`

An `Outlet` represents a physical branch, store, or POS location belonging to an `Organization`. This is the foundational entity that physical commerce (`commerce-storefront`, `commerce-inventory`), till operations, and ledger TILL accounts all depend on.

```
Outlet
├── id: Long
├── organizationId: Long         ← owner org
├── name: String                 ← e.g. "Ikeja Branch", "Lagos Island"
├── address: String
├── city: String
├── state: String
├── country: Country
├── currency: Currency
├── managerId: Long              ← userId of the branch manager; nullable
├── status: OutletStatus         ← ACTIVE | SUSPENDED | CLOSED
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateDetails(name, address, city, state)` → registers `OutletUpdatedEvent`
- `assignManager(Long userId)` → updates `managerId`
- `suspend()` → status `ACTIVE → SUSPENDED`
- `close()` → status `ACTIVE|SUSPENDED → CLOSED`

**Domain Rules:**
- An org may have multiple outlets but each outlet belongs to exactly one org
- `country` and `currency` are immutable after creation

**`OutletStatus` Enum**: `ACTIVE`, `SUSPENDED`, `CLOSED`

---

## 4. Value Objects

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
    private static final Set<String> SUPPORTED = Set.of("NG");
    public Country {
        if (!SUPPORTED.contains(code))
            throw new ValidationException(AccountsErrorCode.UNSUPPORTED_COUNTRY, "Unsupported country: " + code);
    }
    public Currency deriveCurrency() {
        return switch (code) {
            case "NG" -> Currency.NGN;
            default -> throw new BusinessRuleException(AccountsErrorCode.UNSUPPORTED_COUNTRY, code);
        };
    }
}
```

### `AtlasHubRegistrationType` (Enum)

The public AtlasHub values are `SOLE_PROPRIETORSHIP` and `PRIVATE_LIMITED_COMPANY`. Provider values are not exposed by accounts. The Anchor compliance adapter maps these values to its provider contract when submitting compliance.

### `SupportedIndustry` (Enum)

The initial allowlist is deliberately limited to AtlasHub's commerce, hospitality, and logistics use cases:

- `PHYSICAL_GOODS`
- `DIGITAL_SERVICES`
- `PHYSICAL_SERVICES`
- `PROFESSIONAL_SERVICES`
- `HOTELS`
- `COURIER_SERVICES`
- `FREIGHT_SERVICES`
- `RETAIL`
- `WHOLESALE`
- `RESTAURANTS`

Each value has an explicit Anchor code. Financial services, gaming, government, political organizations, public companies, and other enhanced-risk categories are not accepted in the initial programme.

---

## 5. Domain Events

All events are written to the outbox within the same DB transaction that mutates the aggregate.

> **Topics**: `User`/`UserProfileUpdated`/`UserActiveOrganizationChanged` → **`user-events`**. `OrganizationRegistered`/`OrganizationUpdated` → **`accounts-events`**. Outlet events → **`accounts-events`**.

| Event class | Published When | Consumed By |
|---|---|---|
| `UserCreated` | New user registers | `auth` (create AuthAccount + issue email OTP), `notifications` (welcome email) |
| `UserProfileUpdated` | User updates name/phone | `hr` (sync employee name if linked) |
| `UserActiveOrganizationChanged` | User switches active org | Internal — no downstream consumers |
| `OrganizationRegistered` | New org registers | `iam` (create OWNER membership), `billing` (auto-initialise subscription), `compliance` (init KYC record) |
| `OrganizationUpdated` | Org updates business details | Internal only |
| `OutletCreatedEvent` | New outlet/branch registered | `commerce-inventory` (init inventory context), `pay:ledger` (create TILL ledger account for outlet) *(planned)* |
| `OutletUpdatedEvent` | Outlet details changed | Internal only *(planned)* |
| `OutletSuspendedEvent` | Outlet suspended | `notifications` (alert manager) *(planned)* |
| `OutletClosedEvent` | Outlet permanently closed | `pay:ledger` (close TILL account), `notifications` *(planned)* |

**Event payload shapes (from code):**

```
UserCreated
├── aggregateId  : Long      ← the userId
└── payload
    ├── email        : String
    ├── isInvited    : Boolean   ← true = came via org invitation; auth skips email verification
    └── credentialReference : String  ← one-time Redis reference; raw password and hash never enter the event

OrganizationRegistered
├── aggregateId    : Long    ← the organizationId
└── payload
    ├── businessName   : String
├── registrationType : AtlasHubRegistrationType
├── registrationDate : LocalDate
├── industry       : SupportedIndustry
├── country        : String
    ├── currency       : CurrencyCode
    └── ownerUserId    : Long

UserProfileUpdated.payload
├── firstName  : String
├── lastName   : String
└── phone      : String      ← nullable

UserActiveOrganizationChanged.payload
├── userId           : Long
└── organizationId   : Long
```

---

## 6. Outbound Ports (Open Host Service)

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

## 7. Exceptions & Errors

**`AccountsErrorCode`** (implements `ErrorCode`):
- `USER_NOT_FOUND`, `ORGANIZATION_NOT_FOUND`, `OUTLET_NOT_FOUND`
- `EMAIL_ALREADY_REGISTERED`
- `INVALID_EMAIL_FORMAT`, `INVALID_PHONE_FORMAT`
- `UNSUPPORTED_COUNTRY`

---

## 8. Commands

### Registration
- `RegisterOrganizationCommand(firstName, lastName, email, password, country, businessName, registrationType, industry, ...)` → `RegisterOrganizationHandler`
  - Creates `User`, `Organization` in one transaction
  - Generates the organization registration date in code
  - Stores the password temporarily through `OneTimeSecretStore` and publishes only its one-time reference
  - Publishes `UserCreated` + `OrganizationRegistered`
  - Idempotency: pre-checks email uniqueness before creation
  - Rejects registration types and industries disabled by the current AtlasHub onboarding policy

### Profile Updates
- `UpdateUserProfileCommand(userId, firstName, lastName, phone, timezone)` → `UpdateUserProfileHandler`
- `UpdateOrganizationDetailsCommand(orgId, businessName, description, websiteUrl)` → `UpdateOrganizationDetailsHandler`
Organization switching is implemented by authentication and synchronized back through `ActiveOrganizationSwitchedEvent`. Payment/POS capability enablement belongs to `pay:accounts`, not this module.

### Outlet Management
- `CreateOutletCommand(organizationId, name, address, city, state, country, managerId)` → `CreateOutletHandler`
  - Publishes `OutletCreatedEvent` → triggers TILL ledger account creation in `pay:ledger`
- `UpdateOutletCommand(outletId, name, address, city, state, managerId)` → `UpdateOutletHandler`
- `SuspendOutletCommand(outletId, requestedByUserId)` → `SuspendOutletHandler`
- `CloseOutletCommand(outletId, requestedByUserId)` → `CloseOutletHandler`

---

## 9. Queries

- `GetUserProfileQuery(Long userId)` → `UserProfileResult`
- `GetOrganizationDetailsQuery(Long orgId)` → `OrganizationDetailsResult`
- `ListUserOrganizationsQuery(Long userId)` → `List<OrganizationSummaryResult>` — all orgs the user is a member of
- `GetOutletQuery(Long outletId)` → `OutletResult`
- `ListOutletsQuery(Long organizationId)` → `List<OutletResult>`

---

## 10. Listeners

- **`AuthEventListener`** — topic: `auth-events`, groupId: `accounts-auth-group`
  - Listens for `AuthEmailVerifiedEvent`
  - Payload: `{ userId: String, email: String }`
  - Calls `user.markEmailVerified()` then `userRepository.save(user)`
  - Effect: sets `User.emailVerified = true` so the accounts module stays in sync with auth's verification state

---

## 11. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `UserJpaEntity` and `OrganizationJpaEntity`. Prevents concurrent profile update conflicts.

### Outbox & Inbox
- **Outbox**: `UserCreated`, `OrganizationRegistered`, `UserProfileUpdated`, `UserActiveOrganizationChanged`, `OrganizationUpdated` are written to the outbox within the same transaction. Guarantees at-least-once delivery even if downstream modules are temporarily down.
- **Inbox (consumed)**: `AuthEmailVerifiedEvent` from `auth-events` — processed idempotently (find user → if already verified, no-op).

### Idempotency
- `RegisterOrganizationHandler` checks for email uniqueness before creating the user. If a duplicate registration is attempted concurrently, the DB unique constraint on `email` serves as the final guard.
