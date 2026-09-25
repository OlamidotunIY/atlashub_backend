# Platform Identity Module Design (`atlashub-platform:identity`)

## Role & Purpose

The Identity module is the **foundation of the entire platform**. Every entity that interacts with AtlasHub — whether a business owner, a staff member, or an automated service — is first registered and managed here. It answers the fundamental questions: *Who is this person? Which organization do they belong to? What is the relationship between them?*

This module owns the lifecycle of **Organizations** and **Users** and their membership relationship. It is the first module touched during onboarding and the last one referenced when any operation is authorized. It does not handle passwords, tokens, or sessions — those concerns belong to the `auth` module — but it is the identity source of truth that `auth` and every other module references by `userId` and `organizationId`.

Critically, the Identity module also manages the **KYC/Compliance journey** of an Organization. An organization cannot transact money, subscribe to products, or access certain features until it has completed its compliance verification. The `complianceStatus` field on `Organization` acts as a platform-wide gate that all other modules respect.

---

## 1. Features

### Organization Registration & Management
When a business signs up to AtlasHub, this module creates the `Organization` and its founding `User` in a single atomic operation. The business owner becomes the `OWNER` member of the organization automatically. The `User.country` field captured here is critical — it determines the organization's **base currency** (e.g., Nigeria → NGN, Kenya → KES), which is used by the `billing` module for invoice pricing and the `pay` module for wallet denomination.

### Compliance (KYC) Journey
Before an organization can accept payments, issue virtual accounts, or subscribe to paid products, it must complete a **multi-step compliance verification**:
1. **PROFILE** — Business description, industry, annual transaction volume, staff size
2. **CONTACT** — Support and dispute email addresses, WhatsApp contact
3. **OWNER** — BVN, NIN, date of birth, government ID for the account owner
4. **ACCOUNT** — Settlement bank account (bank code + account number + verified name)
5. **SERVICE_AGREEMENT** — Acceptance of platform terms

Each step is tracked by `complianceStep` and `complianceStatus`. Submission triggers a review workflow handled by the `admin` module. On approval, `OrganizationComplianceApproved` is published, which unlocks the `pay:accounts` module to issue a live virtual account for the organization.

### Member Invitations & Roles
Organization owners and admins can invite team members via email. The `Invitation` aggregate tracks the full lifecycle — PENDING → ACCEPTED/DECLINED/EXPIRED. On acceptance, an `OrganizationMember` is created and the invited user gets an account with the assigned `OrganizationRole` (OWNER, ADMIN, MANAGER, VIEWER). The `auth` module reacts to the `InvitationAccepted` event to provision the user's credentials.

### API Key Management
Organizations are issued `ApiKey` records (PUBLIC and SECRET types) per environment (LIVE/TEST). These keys are how external applications authenticate with AtlasHub APIs (e.g., a merchant's e-commerce store calling the Pay API to initiate a payment). Key revocation is managed here.

---

## 2. Domain Entities & Aggregates

**`Organization` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `businessName`: String *(not `name`)*
  - `businessType`: `BusinessType`
  - `businessSize`: `BusinessSize`
  - `description`: String (nullable)
  - `logoUrl`: String (nullable)
  - `complianceStatus`: `ComplianceStatus` (NOT_STARTED, IN_PROGRESS, SUBMITTED, UNDER_REVIEW, APPROVED, REJECTED)
  - `complianceStep`: `ComplianceStep` (PROFILE, CONTACT, OWNER, ACCOUNT, SERVICE_AGREEMENT)
  - `compliance`: `OrganizationCompliance` (embedded value object holding all KYC fields)
  - `createdAt`: ZonedDateTime
  - `updatedAt`: ZonedDateTime
- **Note**: There is no separate `OrganizationStatus` field. Organization lifecycle state is managed through `complianceStatus`. Suspension/banning is represented by `ComplianceStatus.REJECTED` and enforced via billing (`SubscriptionSuspendedEvent`).
- **Methods**:
  - `updateOrganization(String businessName, String description, String logoUrl)`
  - `updateComplianceProfile(...)`
  - `updateComplianceContact(...)`
  - `updateComplianceOwner(...)`
  - `updateComplianceAccount(...)`
  - `acceptServiceAgreement()`
  - `submitCompliance()` — guards: all steps must be complete
  - `approveCompliance()` — raises `OrganizationComplianceApproved`
  - `rejectCompliance(String reason)` — raises `OrganizationComplianceRejected`
  - `ban(String reason)` — raises `OrganizationBanned`

**`User` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `firstName`: String
  - `lastName`: String
  - `email`: `EmailAddress` *(value object, not raw String)*
  - `imageUrl`: String (nullable)
  - `phone`: `PhoneNumber` (nullable)
  - `country`: `Country` *(value object — determines base currency for billing)*
  - `activeOrganizationId`: Long (nullable)
  - `createdAt`: ZonedDateTime
  - `updatedAt`: ZonedDateTime
- **Note**: There is no `status` or `UserStatus` field on `User`. User activation state is managed by `AuthAccountJpa` in the `auth` module.
- **Methods**:
  - `updateProfile(String firstName, String lastName, PhoneNumber phone)` — raises `UserProfileUpdated`
  - `updateImageUrl(String imageUrl)`
  - `switchActiveOrganization(Long organizationId)` — raises `UserActiveOrganizationChanged`

**`OrganizationMember` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `organizationId`: Long
  - `userId`: Long
  - `role`: `OrganizationRole` (OWNER, ADMIN, MANAGER, VIEWER)
  - `status`: `MemberStatus` (ACTIVE, INACTIVE)
  - `joinedAt`: ZonedDateTime
- **Note (RBAC Future)**: Once custom roles (as described in `rbac-design.md`) are implemented, `role` will be replaced by a `roleId` reference to a `CustomRole` entity.

**`Invitation` (Aggregate Root)**
- **Fields**:
  - `id`: UUID
  - `organizationId`: Long
  - `invitedEmail`: String
  - `invitedByUserId`: Long
  - `role`: `OrganizationRole`
  - `token`: String (hashed secure token for email link)
  - `status`: `InvitationStatus` (PENDING, ACCEPTED, DECLINED, EXPIRED)
  - `expiresAt`: ZonedDateTime
  - `createdAt`: ZonedDateTime
- **Methods**:
  - `accept(Long acceptingUserId)` — raises `InvitationAccepted`
  - `decline()` — raises `InvitationDeclined`
  - `expire()` — raises `InvitationExpired`

**`ApiKey` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `keyHash`, `prefix`, `environment` (`ApiEnvironment`: LIVE, TEST), `type` (`KeyType`: PUBLIC, SECRET), `isRevoked`, `createdAt`

---

## 3. Domain Events

> All events are wrapped in `EnvelopedDomainEvent` before publishing via Outbox.

| Event | Published When | Consumed By |
|---|---|---|
| `OrganizationRegistered` | New org signs up | `billing` (bootstrap subscriptions), `pay:accounts` (prepare for account issuance) |
| `OrganizationComplianceSubmitted` | Org submits KYC | `admin` (creates review task) |
| `OrganizationComplianceApproved` | Admin approves KYC | `pay:accounts` (trigger LIVE virtual account issuance), `notifications` (email org) |
| `OrganizationComplianceRejected` | Admin rejects KYC | `notifications` (email org with reason) |
| `OrganizationBanned` | Admin bans org | `pay:accounts` (close all accounts), `billing` (cancel subscriptions) |
| `UserCreated` | New user registered | `auth` (create AuthAccount), `notifications` (send welcome email) |
| `UserProfileUpdated` | User updates their name/phone | `hr` (sync employee name if linked) |
| `InvitationCreated` | Member invited | `notifications` (send invite email) |
| `InvitationAccepted` | Invited user accepts | `auth` (activate AuthAccount), `hr` (draft Employee record if org has HR module) |

---

## 4. Exceptions & Errors

**`IdentityErrorCode`**:
- `ORGANIZATION_NOT_FOUND`, `USER_NOT_FOUND`
- `EMAIL_ALREADY_REGISTERED`
- `INVITATION_NOT_FOUND`, `INVITATION_EXPIRED`
- `COMPLIANCE_STEP_OUT_OF_ORDER`, `COMPLIANCE_NOT_ALL_STEPS_COMPLETE`, `COMPLIANCE_NOT_SUBMITTED`
- `UNAUTHORIZED_ACCESS`

---

## 5. Commands & Use Cases

- `RegisterOrganizationCommand(firstName, lastName, email, phone, country, businessName, businessType, businessSize)` → `RegisterOrganizationUseCase`
  Creates `User`, `Organization`, and `OrganizationMember` as OWNER in one transaction. Publishes `UserCreated` and `OrganizationRegistered`.
- `InviteMemberCommand(orgId, invitedByUserId, email, role)` → `InviteMemberUseCase`
  Creates `Invitation`, publishes `InvitationCreated` to trigger notification email.
- `AcceptInvitationCommand(UUID token, Long acceptingUserId)` → `AcceptInvitationUseCase`
  Calls `invitation.accept()`, creates `OrganizationMember`.
- `SwitchActiveOrganizationCommand(Long userId, Long orgId)` → `SwitchActiveOrganizationUseCase`
- `UpdateOrganizationCommand(Long orgId, String name, String description, String logoUrl)` → `UpdateOrganizationUseCase`
- `SubmitComplianceCommand(Long orgId)` → `SubmitComplianceUseCase`
- `UpdateComplianceProfileCommand(...)` → `UpdateComplianceProfileUseCase`
- `UpdateComplianceContactCommand(...)` → `UpdateComplianceContactUseCase`
- `UpdateComplianceOwnerCommand(...)` → `UpdateComplianceOwnerUseCase`
- `UpdateComplianceAccountCommand(...)` → `UpdateComplianceAccountUseCase`
- `IssueApiKeyCommand(orgId, environment, type)` → `IssueApiKeyUseCase`
- `RevokeApiKeyCommand(keyId, orgId)` → `RevokeApiKeyUseCase`

---

## 6. Queries

- `GetOrganizationDetailsQuery(Long orgId)` → `OrganizationDetailsResult`
- `ListOrganizationMembersQuery(Long orgId)` → `List<MemberResult>`
- `GetUserProfileQuery(Long userId)` → `UserProfileResult`
- `GetOrganizationComplianceQuery(Long orgId)` → `ComplianceDetailsResult`
- `ListApiKeysQuery(Long orgId, ApiEnvironment)` → `List<ApiKeyResult>`

---

## 7. Listeners

- `SubscriptionSuspendedListener`: Listens to `SubscriptionSuspendedEvent` (from `billing`). Logs the event; full access revocation is enforced at the RBAC layer by the billing module.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Organization` and `Invitation`. Prevents double-acceptance of the same invitation token under concurrent requests.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `UserCreated` (triggers `AuthAccountJpa` creation in auth) and `InvitationCreated` (triggers notification email). All events go through the Outbox to guarantee delivery even if the downstream consumer is temporarily unavailable.
- **Inbox**: Consumes `SubscriptionSuspendedEvent` idempotently via `EventDeliveryTracker`.
