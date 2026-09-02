# Platform Identity Module Design (`atlashub-platform:identity`)

## 1. Domain Entities & Aggregates

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
  - `updateComplianceProfile(String description, StaffSize staffSize, String industry, String category, BigDecimal annualVolume, String currency)`
  - `updateComplianceContact(EmailAddress supportEmail, EmailAddress disputeEmail, PhoneNumber whatsapp, ...)`
  - `updateComplianceOwner(String bvn, String nin, LocalDate dob, String address, GovernmentIdType idType, String idNumber, String rcNumber)`
  - `updateComplianceAccount(String bankCode, String accountNumber, String accountName)`
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
- **Note**: There is no `status` or `UserStatus` field on `User`. User activation state is managed by `AuthAccount` in the `auth` module.
- **Methods**:
  - `updateProfile(String firstName, String lastName, PhoneNumber phone)` — raises `UserProfileUpdated`
  - `updateImageUrl(String imageUrl)`
  - `switchActiveOrganization(Long organizationId)` — raises `UserActiveOrganizationChanged`

**`OrganizationMember` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `organizationId`: Long
  - `userId`: Long
  - `role`: `OrganizationRole` (OWNER, ADMIN, MANAGER, VIEWER) *(not STAFF)*
  - `status`: `MemberStatus` (ACTIVE, INACTIVE)
  - `joinedAt`: ZonedDateTime
- **Note (RBAC Future)**: Once custom roles (as described in `rbac-design.md`) are implemented, `role` will be replaced by a `roleId` reference to a `CustomRole` entity.

**`Invitation` (Aggregate Root)**
- **Fields**:
  - `id`: **UUID** *(not Long)*
  - `organizationId`: Long
  - `invitedEmail`: String
  - `invitedByUserId`: Long
  - `role`: `OrganizationRole`
  - `token`: String (hashed secure token for email link)
  - `status`: `InvitationStatus` (PENDING, ACCEPTED, DECLINED, EXPIRED)
  - `expiresAt`: **ZonedDateTime** *(not LocalDateTime)*
  - `createdAt`: ZonedDateTime
- **Methods**:
  - `accept(Long acceptingUserId)` — raises `InvitationAccepted`
  - `decline()` — raises `InvitationDeclined`
  - `expire()` — raises `InvitationExpired`

**`ApiKey` (Aggregate Root)**
- **Fields**: `id`, `organizationId`, `keyHash`, `prefix`, `environment` (`ApiEnvironment`: LIVE, TEST), `type` (`KeyType`: PUBLIC, SECRET), `isRevoked`, `createdAt`

**`SplitRecipient` (Entity)**
- Used by `atlashub-pay` split groups, scoped to identity for ownership tracking.

## 2. Domain Events (Wrapped in `EnvelopedDomainEvent`)
- `OrganizationRegistered(String organizationId, String businessName, BusinessType type, BusinessSize size)`
- `OrganizationUpdated(String organizationId, String businessName, String description, String logoUrl)`
- `OrganizationComplianceStepCompleted(String organizationId, ComplianceStep step)`
- `OrganizationComplianceSubmitted(String organizationId)`
- `OrganizationComplianceApproved(String organizationId, String businessName)`
- `OrganizationComplianceRejected(String organizationId, String reason)`
- `OrganizationBanned(String organizationId, String reason)`
- `UserCreated(String userId, String email, String firstName, String lastName, String country, Boolean isInvited)`
- `UserProfileUpdated(String userId, String firstName, String lastName, String phone)`
- `UserActiveOrganizationChanged(String userId, Long organizationId)`
- `OrganizationMemberAdded(String organizationId, Long userId, String role)`
- `InvitationCreated(UUID invitationId, Long organizationId, String email, OrganizationRole role, String token)`
- `InvitationAccepted(UUID invitationId, Long organizationId, String email, Long acceptingUserId)`
- `InvitationDeclined(UUID invitationId, Long organizationId, String email)`
- `InvitationExpired(UUID invitationId, Long organizationId, String email)`

## 3. Exceptions & Errors
**`IdentityErrorCode`**:
- `ORGANIZATION_NOT_FOUND`, `USER_NOT_FOUND`
- `EMAIL_ALREADY_REGISTERED`
- `INVITATION_NOT_FOUND`, `INVITATION_EXPIRED`
- `COMPLIANCE_STEP_OUT_OF_ORDER`, `COMPLIANCE_NOT_ALL_STEPS_COMPLETE`, `COMPLIANCE_NOT_SUBMITTED`
- `UNAUTHORIZED_ACCESS`

## 4. Commands & Use Cases
- `RegisterOrganizationCommand(firstName, lastName, email, phone, country, businessName, businessType, businessSize)` → `RegisterOrganizationUseCase` (Creates `User`, `Organization`, and `OrganizationMember` as OWNER. Publishes `UserCreated` and `OrganizationRegistered`.)
- `InviteMemberCommand(orgId, invitedByUserId, email, role)` → `InviteMemberUseCase` (Creates `Invitation`, publishes `InvitationCreated` to trigger notification email.)
- `AcceptInvitationCommand(UUID token, Long acceptingUserId)` → `AcceptInvitationUseCase` (Calls `invitation.accept()`, creates `OrganizationMember`.)
- `SwitchActiveOrganizationCommand(Long userId, Long orgId)` → `SwitchActiveOrganizationUseCase`
- `UpdateOrganizationCommand(Long orgId, String name, String description, String logoUrl)` → `UpdateOrganizationUseCase`
- `SubmitComplianceCommand(Long orgId)` → `SubmitComplianceUseCase`

## 5. Queries
- `GetOrganizationDetailsQuery(Long orgId)` → `OrganizationDetailsResult`
- `ListOrganizationMembersQuery(Long orgId)` → `List<MemberResult>`
- `GetUserProfileQuery(Long userId)` → `UserProfileResult`
- `GetOrganizationComplianceQuery(Long orgId)` → `ComplianceDetailsResult`

## 6. Listeners
- `SubscriptionSuspendedListener`: Listens to `SubscriptionSuspendedEvent` (from `billing`). Logs the event and can optionally mark member statuses as restricted. Full access revocation is enforced at the RBAC layer by the billing module.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Organization` and `Invitation`. Prevents double-acceptance of the same invitation token under concurrent requests.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `UserCreated` (triggers `AuthAccount` creation in auth module) and `InvitationCreated` (triggers notification email).
- **Inbox**: Consumes `SubscriptionSuspendedEvent` idempotently via `EventDeliveryTracker`.
