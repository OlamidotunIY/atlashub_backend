# IAM Module Design (`atlashub-platform:iam`)

## Role & Purpose

The `iam` (Identity and Access Management) module owns **authorization** on the AtlasHub platform. It answers the question: *what is this user or API key allowed to do?*

IAM owns:
- **Organization memberships** — which users belong to which org, and with what role
- **Custom roles** — org admins define roles by selecting specific permissions (like AWS IAM policies)
- **Permissions** — the set of named, resource-scoped actions available on the platform
- **API keys** — public/secret key pairs issued to organizations for machine-to-machine access
- **Invitations** — the invitation workflow for adding new members to an organization

IAM does **not** own:
- Who the user is (`accounts`)
- How they log in (`auth`)
- Whether the org is KYC-approved (`compliance`)
- What products they are subscribed to (`billing`)

---

## 1. Features

### Custom Role Builder
Organizations do not get fixed roles like "ADMIN" or "MANAGER". Instead, org admins define custom roles by selecting individual permissions. Each permission maps to a specific resource and action:

```
Format:  {module}:{resource}:{action}

Examples:
  pay:charges:create          ← can initiate payments
  pay:transfers:approve       ← can approve payouts (maker-checker)
  commerce:orders:read        ← can view orders
  commerce:inventory:update   ← can adjust stock
  hr:payroll:initiate         ← can start a payroll run
  hr:payroll:approve          ← can approve a payroll run
  accounting:journal:post     ← can post journal entries
  accounting:journal:approve  ← can approve journal entries (maker-checker)
  logistics:shipments:dispatch← can dispatch deliveries
  iam:members:invite          ← can invite new team members
```

### Permission Claim Embedding
When a user logs in, `iam` provides all permissions for their active organization. These permissions are embedded as claims in the JWT access token. Downstream services validate permissions locally (no database hit per request).

### Membership Management
- An `OrganizationMember` record links a `User` to an `Organization` with a `customRoleId`
- One user can be a member of multiple organizations (with different roles in each)
- The `OWNER` role is a built-in, immutable role with all permissions — it cannot be deleted or modified

### Invitation Workflow
Org owners/admins invite new members by email. The `Invitation` aggregate tracks the full lifecycle. On acceptance, `OrganizationMember` is created and `InvitationAcceptedEvent` is published.

### API Key Management
Organizations are issued API key pairs (public + secret) per environment (LIVE/TEST). The secret key is returned once; AtlasHub stores only AES-256-GCM ciphertext so it can verify HMAC signatures. Keys can be revoked or atomically rotated.

---

## 2. Domain Entities & Aggregates

### `Permission` (Aggregate Root — Platform-Managed)

Permissions are **platform-defined** — AtlasHub engineers define the full set. Organizations cannot create custom permissions; they can only assign existing permissions to their roles.

```
Permission
├── id: Long
├── code: String                  ← unique, e.g., "pay:charges:create"
├── module: String                ← e.g., "pay"
├── resource: String              ← e.g., "charges"
├── action: PermissionAction      ← CREATE, READ, UPDATE, DELETE, APPROVE, INITIATE, DISPATCH, MANAGE, SUSPEND, REACTIVATE, CLOSE, REFUND, OPEN, POST
├── displayName: String           ← human-readable label
├── description: String
└── isActive: Boolean
```

---

### `CustomRole` (Aggregate Root — Org-Managed)

```
CustomRole
├── id: Long
├── organizationId: Long
├── name: String                  ← e.g., "Cashier", "Finance Manager", "Warehouse Supervisor"
├── description: String
├── permissions: Set<Long>        ← set of Permission IDs
├── isBuiltIn: Boolean            ← true for OWNER role only
├── createdBy: Long               ← userId of the creator
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `addPermission(Long permissionId)` → adds permission to this role
- `removePermission(Long permissionId)` → cannot remove from built-in OWNER role
- `rename(String newName)`
- `delete()` → guard: cannot delete if any active member uses this role

**Domain Rules:**
- The built-in `OWNER` role always has all permissions and cannot be modified
- A `CustomRole` cannot be deleted while it is assigned to an active member
- Minimum 1 permission per custom role

---

### `OrganizationMember` (Aggregate Root)

```
OrganizationMember
├── id: Long
├── organizationId: Long
├── userId: Long
├── customRoleId: Long            ← references CustomRole
├── status: MemberStatus          ← ACTIVE, INACTIVE, SUSPENDED
├── joinedAt: ZonedDateTime
├── invitedBy: Long               ← userId who sent the invitation, nullable
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `assignRole(Long newRoleId)` → changes the member's role
- `deactivate()` → sets status INACTIVE (soft removal)
- `suspend(String reason)` → temporary suspension
- `reactivate()`

**Domain Rules:**
- An organization must always have at least one OWNER — `deactivate()` on the last OWNER is rejected
- A suspended member cannot log in to the org context (enforced at login by checking membership status)

---

### `Invitation` (Aggregate Root)

```
Invitation
├── id: Long
├── organizationId: Long
├── invitedEmail: EmailAddress
├── invitedByUserId: Long
├── customRoleId: Long            ← which role they will get on acceptance
├── token: String                 ← SHA-256 hashed secure token (sent in email as plaintext)
├── status: InvitationStatus      ← PENDING, ACCEPTED, DECLINED, EXPIRED, REVOKED
├── expiresAt: ZonedDateTime      ← 7 days from creation
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `accept(Long acceptingUserId)` → validates not expired, sets ACCEPTED → registers `InvitationAcceptedEvent`
- `decline()` → sets DECLINED → registers `InvitationDeclinedEvent`
- `expire()` → called by a scheduler, sets EXPIRED
- `revoke(Long revokedByUserId)` → admin cancels a pending invitation

---

### `ApiKey` (Aggregate Root)

Organizations are issued separate API key pairs for `TEST` and `LIVE` machine-to-machine access. TEST keys are available without provider production approval. LIVE key issuance requires approved AtlasHub compliance. Keys are environment-bound, can be named, revoked, and rotated, and can never switch environment after creation.

```
ApiKey
├── id: Long
├── organizationId: Long
├── publicKey: String             ← "atlas_pk_..." — stored plaintext
├── secretKeyCiphertext: String   ← AES-256-GCM ciphertext; plaintext is never stored
├── name: String                  ← human-readable label (e.g., "Server Key", "Webhook Processor")
├── environment: ApiEnvironment  ← TEST or LIVE; immutable
├── boundRoleId: Long             ← nullable; null means all active platform permissions
├── isRevoked: Boolean
├── lastUsedAt: ZonedDateTime     ← nullable
├── revokedAt: ZonedDateTime      ← nullable
├── revokedBy: Long               ← userId, nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `revoke(Long revokedByUserId)` → sets `isRevoked = true` → registers `ApiKeyRevokedEvent`
- `recordUsage()` → updates `lastUsedAt` (done asynchronously to avoid blocking request)

**Key Generation** (in `IssueApiKeyUseCase`):
```
publicKey  = "atlas_pk_" + environment + "_" + secureRandom
secretKey        = "atlas_sk_" + environment + "_" + secureRandom
secretCiphertext = AES-256-GCM(secretKey)        ← stored in DB; plaintext shown once
```

**Permissions via API Key:**
An API key authenticates as the organization. An unbound key receives the current active platform permission catalog. A key with `boundRoleId` receives only that role's current active permissions. Authentication never writes key usage synchronously; it publishes `ApiKeyAuthenticatedEvent`, and IAM records `lastUsedAt` asynchronously.

```
ApiKey (extended)
└── boundRoleId: Long             ← nullable; if set, limits key to that role's permissions only
```



## 3. Built-In Permissions (Platform-Defined)

### `atlashub-pay`
| Code | Description |
|---|---|
| `pay:accounts:create` | Issue customer/vendor reserved accounts |
| `pay:accounts:read` | View the organization's banking profile and reserved accounts |
| `pay:accounts:suspend` | Suspend a reserved account |
| `pay:accounts:reactivate` | Reactivate a suspended reserved account |
| `pay:accounts:close` | Permanently close a reserved account |
| `pay:charges:create` | Initiate payment collection |
| `pay:transfers:create` | Initiate a bank transfer |
| `pay:transfers:approve` | Approve a bank transfer (maker-checker) |
| `pay:ledger:read` | View ledger transactions |
| `pay:splits:manage` | Create and manage split rules |
| `pay:settlements:read` | View settlement history |

Organization deposit-account and FBO-subaccount provisioning is system-only and has no organization IAM permission. Ledger freeze/unfreeze/close operations use platform-staff payment-operations authorization and are not represented as organization role permissions.

### `atlashub-commerce`
| Code | Description |
|---|---|
| `commerce:products:manage` | Create/update/delete products |
| `commerce:orders:create` | Process sales orders |
| `commerce:orders:read` | View orders |
| `commerce:orders:refund` | Process refunds |
| `commerce:inventory:read` | View stock levels |
| `commerce:inventory:update` | Adjust stock |
| `commerce:suppliers:manage` | Manage suppliers and purchase orders |
| `commerce:pos:manage` | Enable or disable the POS feature for the organization |
| `commerce:tills:open` | Open a POS till |
| `commerce:tills:close` | Close a POS till |
| `commerce:vendors:manage` | Manage marketplace vendors |

### `atlashub-logistics`
| Code | Description |
|---|---|
| `logistics:shipments:create` | Create shipments |
| `logistics:shipments:dispatch` | Assign riders and dispatch |
| `logistics:shipments:read` | View shipment status |
| `logistics:fleet:manage` | Register and manage vehicles |
| `logistics:riders:manage` | Register and manage riders |
| `logistics:transfers:approve` | Approve stock transfers |

### `atlashub-hr`
| Code | Description |
|---|---|
| `hr:employees:manage` | Onboard, update, terminate employees |
| `hr:employees:read` | View employee records |
| `hr:payroll:initiate` | Start a payroll run |
| `hr:payroll:approve` | Approve a payroll run (maker-checker) |
| `hr:payroll:read` | View payroll runs and payslips |
| `hr:leave:approve` | Approve leave applications |
| `hr:loans:approve` | Approve employee loans |

### `atlashub-accounting`
| Code | Description |
|---|---|
| `accounting:journal:post` | Record journal entries |
| `accounting:journal:approve` | Approve journal entries (maker-checker) |
| `accounting:reports:read` | View financial reports |
| `accounting:accounts:manage` | Manage chart of accounts |
| `accounting:expenses:approve` | Approve expenses |
| `accounting:budget:manage` | Create and approve budgets |

### `atlashub-iam`
| Code | Description |
|---|---|
| `iam:members:invite` | Invite new members |
| `iam:members:manage` | Deactivate/suspend members |
| `iam:roles:manage` | Create/edit custom roles |
| `iam:apikeys:manage` | Issue/revoke API keys |

### `atlashub-compliance`
| Code | Description |
|---|---|
| `compliance:read` | View compliance status and outstanding requirements |
| `compliance:manage` | Update business details, contacts, officers, agreements, and requested documents |
| `compliance:submit` | Submit the organization's completed KYB application |

---

## 4. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `MemberJoinedEvent` | Invitation accepted / member directly added | `notifications` (welcome to org), `hr` (optional: draft employee record) |
| `MemberDeactivatedEvent` | Member deactivated or suspended | `notifications`, `authentication` (revoke user sessions) |
| `InvitationCreatedEvent` | New invitation created | `notifications` (send invite email with token link) |
| `InvitationAcceptedEvent` | Invited user accepts | `auth` (ensure account is active), `hr` (if org has HR, draft employee) |
| `InvitationExpiredEvent` | Invitation TTL reached | Internal only |
| `ApiKeyRevokedEvent` | API key revoked | Audit/notification consumers; authentication reads current key state and therefore needs no key cache invalidation |
| `ApiKeyAuthenticatedEvent` | HMAC signature accepted | `iam` (asynchronously update `lastUsedAt`) |
| `CustomRolePermissionsChangedEvent` | Role permissions updated | `authentication` (revoke organization sessions) |

---

## 5. Outbound Ports (Open Host Service)

```java
// In atlashub-shared
public interface MembershipQueryPort {
    boolean isMemberOf(Long userId, Long orgId);
    boolean isActiveOwner(Long userId, Long orgId);
    Set<String> getPermissions(Long userId, Long orgId);  // returns permission codes for JWT
    MemberStatus getMemberStatus(Long userId, Long orgId);
}

public interface ApiKeyQueryPort {
    Optional<AuthenticatedApiKey> authenticate(
        String publicKey, String canonicalMessage, String signature);
}
```

---

## 6. Exceptions & Errors

**`IamErrorCode`**:
- `MEMBER_NOT_FOUND`, `INVITATION_NOT_FOUND`
- `INVITATION_EXPIRED`, `INVITATION_ALREADY_ACCEPTED`, `INVITATION_REVOKED`
- `ROLE_NOT_FOUND`, `CANNOT_DELETE_BUILT_IN_ROLE`, `ROLE_IN_USE`
- `LAST_OWNER_CANNOT_BE_DEACTIVATED`
- `PERMISSION_NOT_FOUND`, `DUPLICATE_PERMISSION_CODE`
- `API_KEY_NOT_FOUND`, `API_KEY_ALREADY_REVOKED`
- `UNAUTHORIZED` — operation not permitted for this member's role

---

## 7. Commands & Use Cases

### Memberships & Invitations
- `InitializeOrganizationIamCommand(orgId, foundingUserId)` → `InitializeOrganizationIamUseCase`
  - Triggered internally by `OrganizationRegisteredListener`
  - Bootstraps the built-in `OWNER` role for the organization
  - Creates the initial `OrganizationMember` entity linking the founding user as the owner
- `InviteMemberCommand(orgId, invitedByUserId, email, customRoleId)` → `InviteMemberUseCase`
- `AcceptInvitationCommand(token, acceptingUserId)` → `AcceptInvitationUseCase`
- `DeclineInvitationCommand(token)` → `DeclineInvitationUseCase`
- `RevokeInvitationCommand(invitationId, revokedByUserId)` → `RevokeInvitationUseCase`
- `DeactivateMemberCommand(memberId, requestedByUserId)` → `DeactivateMemberUseCase`
- `AssignRoleCommand(memberId, newRoleId, requestedByUserId)` → `AssignRoleUseCase`

### Custom Roles
- `CreateCustomRoleCommand(orgId, name, description, permissionIds)` → `CreateCustomRoleUseCase`
- `UpdateCustomRoleCommand(roleId, name, description, permissionIds)` → `UpdateCustomRoleUseCase`
- `DeleteCustomRoleCommand(roleId, requestedByUserId)` → `DeleteCustomRoleUseCase`

### API Keys
- `IssueApiKeyCommand(orgId, name, requestedByUserId, boundRoleId?)` → `IssueApiKeyUseCase`
  - Returns `IssuedApiKeyResult` containing plaintext `secretKey` — the ONLY time it is returned
  - `boundRoleId` is optional; if provided, limits the key's permissions to that role
- `RevokeApiKeyCommand(keyId, orgId, requestedByUserId)` → `RevokeApiKeyUseCase`
- `RotateApiKeyCommand(keyId, orgId, requestedByUserId, name?)` → atomically revokes the old key and returns the replacement secret once
- `RecordApiKeyUsageCommand(orgId, publicKey)` → event-driven audit update for `lastUsedAt`

---

## 8. Queries

- `ListMembersQuery(orgId, status)` → `List<MemberResult>`
- `GetMemberDetailsQuery(memberId)` → `MemberDetailsResult`
- `ListCustomRolesQuery(orgId)` → `List<CustomRoleResult>`
- `GetCustomRolePermissionsQuery(roleId)` → `CustomRolePermissionsResult`
- `ListPermissionsQuery(module)` → `List<PermissionResult>` ← for role builder UI
- `ListApiKeysQuery(orgId)` → `List<ApiKeyResult>` ← never returns key values
- `ListInvitationsQuery(orgId, status)` → `List<InvitationResult>`

---

## 9. Listeners

- **`OrganizationRegisteredListener`**: topic=`accounts-events`, event=`OrganizationRegistered`. Creates the built-in OWNER role for the org and an `OrganizationMember` linking the founding user to the org with the OWNER role.
- IAM publishes `MemberDeactivatedEvent` from member deactivation/suspension; the `authentication` module consumes it and revokes the user's sessions.
- **`SubscriptionSuspendedListener`**: topic=`billing-events`. Suspends all non-OWNER members for the organization (access cutoff without deleting data).
- **`EmployeeSuspendedListener`**: topic=`hr-events`. Event=`EmployeeSuspendedEvent`. Payload: `employeeId`, `organizationId`, `userId`, `suspendedAt`. Calls `DeactivateMemberHandler` for the suspended employee's corresponding `OrganizationMember` record. Prevents suspended employees from accessing the dashboard or API while HR suspension is active.
- **`OrganizationBannedListener`**: topic=`admin-events`. Event=`OrganizationBannedEvent`. Payload: `organizationId`, `reason`, `bannedAt`. Deactivates ALL members of the organization (including OWNER). Publishes `MemberDeactivatedEvent` for each, causing `auth` to revoke all tokens for the org.


---

## 10. Permission Enforcement

### At API Layer (Spring Security)
```java
@PreAuthorize("hasAuthority('pay:transfers:create')")
@PostMapping("/transfers")
public ResponseEntity<...> initiateTransfer(...) { ... }
```

Permissions are read from the JWT claims (`permissions` array). No DB hit per request.

### At Application Layer (Double-Check for Critical Operations)
For Maker-Checker and other sensitive operations, the use case re-validates the permission in the application layer — defense-in-depth against JWT tampering.

### Token Invalidation on Role Change
When a `CustomRole`'s permissions change, IAM publishes `CustomRolePermissionsChangedEvent`. Authentication consumes it and revokes every session for that organization, conservatively guaranteeing that no token retains stale role permissions. The next authentication issues a token with the current permission set.

---

## 11. Distributed Architecture

### Locking
- **Optimistic Locking**: `CustomRole`, `Invitation`
- **Pessimistic Locking**: `OrganizationMember` during role assignment — prevents concurrent role changes leaving inconsistent state

### Outbox & Inbox
- **Outbox**: invitation/member events, `ApiKeyRevokedEvent`, `ApiKeyAuthenticatedEvent`, and `CustomRolePermissionsChangedEvent`, routed to `iam-events` where applicable
- **Inbox**: `OrganizationRegistered`, `SubscriptionSuspendedEvent`, `EmployeeSuspendedEvent`, `OrganizationBannedEvent`, and IAM's own asynchronous API-key usage event
- **Invitation expiry**: `InvitationExpirationScheduler` under `infrastructure/messaging/schedulers` invokes only `ExpireInvitationsHandler`
