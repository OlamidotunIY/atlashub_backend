# Platform Identity Module Design (`atlashub-platform:identity`)

## 1. Domain Entities & Aggregates

**`Organization` (Aggregate Root)**
- **Fields**: 
  - `id`: Long
  - `name`: String
  - `businessType`: `BusinessType`
  - `businessSize`: `BusinessSize`
  - `complianceStatus`: `ComplianceStatus`
  - `status`: `OrganizationStatus` (ACTIVE, SUSPENDED, BANNED)
- **Methods**: `updateDetails(...)`, `suspend()`, `approveCompliance()`

**`User` (Aggregate Root)**
- **Fields**: 
  - `id`: Long
  - `email`: String
  - `firstName`: String, `lastName`: String
  - `activeOrganizationId`: Long
  - `status`: `UserStatus`
- **Methods**: `changeActiveOrganization(Long orgId)`

**`OrganizationMember` (Entity)**
- **Fields**: 
  - `id`: Long
  - `organizationId`: Long
  - `userId`: Long
  - `role`: `OrganizationRole` (OWNER, ADMIN, STAFF)
  - `status`: `MemberStatus`

**`Invitation` (Aggregate Root)**
- **Fields**: 
  - `id`: Long
  - `organizationId`: Long
  - `email`: String
  - `role`: `OrganizationRole`
  - `status`: `InvitationStatus` (PENDING, ACCEPTED, DECLINED, EXPIRED)
  - `expiresAt`: LocalDateTime
- **Methods**: `accept(Long userId)`, `decline()`, `expire()`

## 2. Domain Events
- `OrganizationRegistered(Long organizationId)`
- `OrganizationComplianceApproved(Long organizationId)`
- `UserCreated(Long userId, String email)`
- `OrganizationMemberAdded(Long organizationId, Long userId, OrganizationRole role)`
- `InvitationCreated(Long invitationId, String email)`
- `InvitationAccepted(Long invitationId, Long userId)`

## 3. Exceptions & Errors
**`IdentityErrorCode`**:
- `ORGANIZATION_NOT_FOUND`, `USER_NOT_FOUND`
- `EMAIL_ALREADY_REGISTERED`
- `INVITATION_NOT_FOUND`, `INVITATION_EXPIRED`
- `UNAUTHORIZED_ACCESS`

## 4. Commands & Use Cases
- `RegisterOrganizationCommand(...)` -> `RegisterOrganizationUseCase` (Creates User, Organization, and maps them as OWNER).
- `InviteMemberCommand(orgId, email, role)` -> `InviteMemberUseCase` (Creates Invitation, triggers email).
- `AcceptInvitationCommand(invitationId, userId)` -> `AcceptInvitationUseCase` (Creates OrganizationMember).
- `ChangeActiveOrganizationCommand(userId, orgId)` -> `ChangeActiveOrganizationUseCase`

## 5. Queries
- `GetOrganizationDetailsQuery(orgId)`
- `ListOrganizationMembersQuery(orgId)`
- `GetUserProfileQuery(userId)`

## 6. Listeners
- `SubscriptionSuspendedListener`: Listens to `atlashub-platform:billing`. If an org's billing fails, identity can suspend organization access or specific feature flags.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Organization` and `Invitation`. Preventing double-acceptance of invitations.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `UserCreated` and `InvitationCreated` to trigger Auth credentials setup and Notification emails respectively.
- **Inbox**: Consumes `SubscriptionSuspendedEvent` idempotently.
