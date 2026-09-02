# Platform Admin Module Design (`atlashub-platform:admin`)

## Role & Purpose

The Admin module is the **back-office control panel** for AtlasHub staff. While all other modules serve the organizations using the platform, the Admin module serves AtlasHub's own internal team — the people who operate, monitor, and govern the platform itself.

Its responsibilities are broad: reviewing and approving KYC submissions, managing the platform's product catalog, monitoring flagged organizations, handling compliance decisions, and managing the access levels of AtlasHub staff accounts. It is explicitly **not** accessible to organization users — all endpoints in this module are secured behind admin-only authentication using a separate `Admin` principal (not the standard `User`).

Think of this module as the operations dashboard of a fintech company: compliance officers approve businesses, product managers configure pricing, and engineers can override system states when necessary.

---

## 1. Features

### Admin Account Management
AtlasHub staff have dedicated `Admin` accounts that are entirely separate from the `User` accounts that organizations use. Admins are assigned roles and permissions using a role-based system (`AdminRole` with `AdminPermission` sets). This isolation ensures that even if an organization's credentials are compromised, no admin access is granted.

### KYC / Compliance Review
When an organization submits their compliance data (via `identity` module's `SubmitComplianceUseCase`), an `OrganizationComplianceSubmitted` event is published. The Admin module listens to this event and creates a **review task**. Compliance officers can:
- View the full compliance submission (business details, owner BVN/NIN, bank account)
- Approve the submission → publishes `OrganizationComplianceApproved` → unlocks live payment accounts
- Reject with a reason → publishes `OrganizationComplianceRejected` → organization is notified and can resubmit
- Ban the organization entirely → publishes `OrganizationBanned` → all services are terminated

### Organization Oversight
Admins can view and monitor all organizations on the platform, including their compliance status, subscription state, and transaction volumes (via integration with the `pay` module's query side).

### Platform Catalog Management
Admins manage the `HubProduct` catalog (the platform's own product offerings like Atlas Pay, Atlas Commerce, etc.) and their pricing (`ProductPricing`). These changes flow into the `catalog` module and subsequently affect what organizations see when subscribing. Only admins with the `CATALOG_MANAGE` permission can perform these operations.

### Flags & Manual Overrides
Admins can manually suspend, unsuspend, or ban organizations in exceptional circumstances — e.g., fraud detection, regulatory request, or payment failure beyond recovery.

---

## 2. Domain Entities & Aggregates

**`Admin` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `firstName`: String
  - `lastName`: String
  - `email`: String
  - `passwordHash`: String
  - `role`: `AdminRole` (SUPER_ADMIN, COMPLIANCE_OFFICER, SUPPORT_AGENT, CATALOG_MANAGER)
  - `permissions`: `Set<AdminPermission>`
  - `status`: `AdminStatus` (ACTIVE, INACTIVE, LOCKED)
  - `createdAt`: ZonedDateTime
  - `lastLoginAt`: ZonedDateTime (nullable)
- **Methods**:
  - `updateRole(AdminRole newRole, Set<AdminPermission> permissions)`
  - `lock()`
  - `unlock()`
  - `recordLogin(ZonedDateTime time)`

**`AdminPermission` (Value Object)**
Granular permission flags checked on each admin use case. Examples:
- `CATALOG_MANAGE` — Can create/edit hub products and pricing
- `COMPLIANCE_REVIEW` — Can approve/reject KYC submissions
- `ORGANIZATION_BAN` — Can ban an organization
- `ADMIN_MANAGE` — Can create and modify other admin accounts

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `AdminCreatedEvent` | New admin account created | `notifications` (send welcome/setup email) |
| `AdminLockedEvent` | Admin account locked | `audit` |

> The Admin module primarily **consumes** events from other modules rather than producing them. Key decisions like compliance approval are communicated back via commands that mutate the `Organization` aggregate in the `identity` module.

---

## 4. Exceptions & Errors

**`AdminErrorCode`** (implements `ErrorCode`):
- `ADMIN_NOT_FOUND`
- `ADMIN_EMAIL_ALREADY_EXISTS`
- `INSUFFICIENT_PERMISSIONS`
- `INVALID_ADMIN_STATE`
- `COMPLIANCE_REVIEW_NOT_FOUND`

---

## 5. Commands & Use Cases

### Admin Account Management
- `CreateAdminCommand(firstName, lastName, email, role)` → `CreateAdminUseCase`
  Creates admin account, issues setup verification code, sends setup email.
- `UpdateAdminRoleCommand(adminId, newRole, permissions)` → `UpdateAdminRoleUseCase`
- `DeactivateAdminCommand(adminId)` → `DeactivateAdminUseCase`

### Compliance Review
- `ApproveComplianceCommand(orgId, reviewedByAdminId)` → `ApproveComplianceUseCase`
  Calls `organization.approveCompliance()` in the identity module, publishes `OrganizationComplianceApproved`.
- `RejectComplianceCommand(orgId, reason, reviewedByAdminId)` → `RejectComplianceUseCase`
  Calls `organization.rejectCompliance(reason)`, publishes `OrganizationComplianceRejected`.
- `BanOrganizationCommand(orgId, reason, adminId)` → `BanOrganizationUseCase`
  Calls `organization.ban(reason)`, publishes `OrganizationBanned`.

### Catalog Management
*(Delegates to the `catalog` module via use cases)*
- `CreateHubProductCommand(adminId, key, name, description)` → `CreateHubProductUseCase` (in `catalog`)
- `UpdateHubProductCommand(adminId, productId, name, description)` → `UpdateHubProductUseCase`
- `SetProductPricingCommand(adminId, productId, cycle, amount)` → `SetProductPricingUseCase`

---

## 6. Queries

- `ListPendingComplianceReviewsQuery()` → `List<ComplianceReviewResult>`
- `GetOrganizationOverviewQuery(orgId)` → `OrganizationOverviewResult` (compliance + subscription + account status)
- `ListAllOrganizationsQuery(filters)` → `List<OrganizationSummaryResult>`
- `GetAdminDetailsQuery(adminId)` → `AdminResult`
- `ListAdminsQuery()` → `List<AdminResult>`

---

## 7. Listeners

- **`OrganizationComplianceSubmittedListener`**: Listens to `OrganizationComplianceSubmitted` (from `identity`). Flags the organization for compliance officer review and sends an internal notification to the compliance team.

---

## 8. Security

All admin endpoints are secured separately from organization endpoints. Admin authentication uses a dedicated authentication flow (separate `AdminAuthenticateUseCase`). The `@adminAuth.hasPermission(authentication, 'PERMISSION_NAME')` SpEL expression is evaluated on each admin use case handler, not at the controller level alone.

Admin JWTs carry a different `principal_type = "ADMIN"` claim so that the API gateway can route them to admin-only endpoints exclusively.

---

## 9. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `Admin` to prevent concurrent role updates.

### Inbox & Outbox Patterns
- **Inbox**: Idempotently processes `OrganizationComplianceSubmitted` to create exactly one review task per submission.
- **Outbox**: Any decisions (approve/reject/ban) that mutate `Organization` state are written to the Outbox to guarantee the state change event reaches downstream consumers (e.g., `pay:accounts` waiting on `OrganizationComplianceApproved` to issue a live virtual account).
