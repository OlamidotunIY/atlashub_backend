# Admin Module Design (`atlashub-platform:admin`)

## Role & Purpose

The `admin` module is the **internal operations portal** for AtlasHub staff. It exposes tools for the platform's own team to manage businesses on the platform — reviewing KYC applications, managing the product catalog, handling support escalations, monitoring platform health, and taking corrective actions on organizations.

Admin is a **multi-tier module**: different AtlasHub staff roles have different levels of access and capability. A support agent can view an organization's compliance details and leave notes, but only a super admin can approve KYC, ban an organization, or modify platform pricing.

---

## 1. Staff Tiers & Permissions

| Staff Role | Capabilities |
|---|---|
| **SUPER_ADMIN** | Full platform access — approve/reject KYC, ban orgs, manage catalog pricing, manage admin staff, view all data |
| **COMPLIANCE_OFFICER** | Review and action KYC submissions — approve, reject, request more info |
| **SUPPORT_AGENT** | View org details and transaction history, leave internal notes, escalate tickets |
| **FINANCE_ANALYST** | Read-only access to platform financial data, settlement reports, fee revenue reports |
| **CATALOG_MANAGER** | Create and update platform products and pricing plans |

Staff roles are fixed (not configurable by admins themselves — unlike org custom roles).

---

## 2. Features

### KYC Review Workflow
When an organization submits their compliance form, `admin` receives a `ComplianceSubmittedEvent` and creates a `KycReviewTask`. A compliance officer picks it up, reviews all submitted documents (ID, selfie, settlement account), and either:
- **Approves** → `ApproveComplianceUseCase` → `OrganizationComplianceApprovedEvent` → NUBAN issuance begins
- **Rejects** → `RejectComplianceUseCase` with a detailed reason → org is notified and can re-submit
- **Requests More Info** → a status that places the task in a "Waiting on Org" state

### Organization Management
Admins can view any organization's full profile, compliance status, subscription history, and recent transactions. Actions available:
- **Ban Organization** → terminates all subscriptions, freezes virtual accounts, logs reason
- **Unban Organization** → reverses a ban (super admin only)
- **Manually Activate Subscription** → override for special cases (super admin only)

### Platform Catalog Management
Catalog managers can create and update platform products and pricing plans via the admin portal. These actions go through `catalog` module use cases internally.

### Platform Fee & Revenue Analytics
Finance analysts can view:
- Total transaction volume across all organizations for any period
- Platform fee revenue (AtlasHub's cut) per period
- Settlement totals from Paystack/Moniepoint
- Organization-level transaction summaries

### Internal Notes
Support agents can leave internal notes on any organization's record (`AdminNote`). Notes are visible only to admin staff, not to the organization.

### Audit Log Access
Super admins can query the full platform audit log — every state-changing action, who performed it, and when.

---

## 3. Domain Entities & Aggregates

### `AdminStaff` (Aggregate Root)

```
AdminStaff
├── id: Long
├── userId: Long                     ← references accounts:User
├── staffRole: AdminStaffRole        ← SUPER_ADMIN, COMPLIANCE_OFFICER, SUPPORT_AGENT, FINANCE_ANALYST, CATALOG_MANAGER
├── isActive: Boolean
├── onboardedAt: ZonedDateTime
└── deactivatedAt: ZonedDateTime     ← nullable
```

**Business Methods:**
- `deactivate()` — only a SUPER_ADMIN can deactivate another admin
- `changeRole(AdminStaffRole newRole)` — only SUPER_ADMIN

---

### `KycReviewTask` (Aggregate Root)

```
KycReviewTask
├── id: Long
├── organizationId: Long
├── status: ReviewTaskStatus         ← OPEN, IN_REVIEW, WAITING_ON_ORG, APPROVED, REJECTED
├── assignedTo: Long                 ← adminStaffId, nullable
├── assignedAt: ZonedDateTime        ← nullable
├── reviewNotes: String              ← internal notes from reviewer
├── rejectionReason: String          ← nullable
├── submissionCount: Integer         ← how many times org has submitted (re-submissions tracked)
├── createdAt: ZonedDateTime
└── resolvedAt: ZonedDateTime        ← nullable
```

**Business Methods:**
- `assign(Long staffId)` → sets OPEN → IN_REVIEW
- `approve(Long staffId)` → IN_REVIEW → APPROVED → delegates to `compliance:ApproveComplianceUseCase`
- `reject(Long staffId, String reason)` → IN_REVIEW → REJECTED → delegates to `compliance:RejectComplianceUseCase`
- `requestMoreInfo(Long staffId, String notes)` → IN_REVIEW → WAITING_ON_ORG

---

### `AdminNote` (Entity)

```
AdminNote
├── id: Long
├── organizationId: Long
├── createdBy: Long                  ← adminStaffId
├── content: String
├── category: NoteCategory           ← COMPLIANCE, SUPPORT, FINANCIAL, GENERAL
└── createdAt: ZonedDateTime
```

---

## 4. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `KycReviewTaskCreatedEvent` | Compliance submitted | `notifications` (alert compliance officers) |
| `KycApprovedEvent` | Admin approves KYC | `compliance` (approve), `notifications` (email org) |
| `KycRejectedEvent` | Admin rejects KYC | `compliance` (reject), `notifications` (email org with reason) |
| `OrganizationBannedEvent` | Admin bans org | `pay:accounts` (freeze virtual accounts), `billing` (cancel all subscriptions), `notifications` |

---

## 5. Exceptions & Errors

**`AdminErrorCode`**:
- `STAFF_NOT_FOUND`, `STAFF_ALREADY_ACTIVE`, `INSUFFICIENT_ADMIN_PRIVILEGES`
- `REVIEW_TASK_NOT_FOUND`, `REVIEW_TASK_ALREADY_RESOLVED`
- `ORGANIZATION_ALREADY_BANNED`, `ORGANIZATION_NOT_BANNED`

---

## 6. Commands & Use Cases

- `OnboardAdminStaffCommand(userId, staffRole)` → `OnboardAdminStaffUseCase`
- `AssignKycReviewTaskCommand(taskId, staffId)` → `AssignKycReviewTaskUseCase`
- `ApproveKycCommand(taskId, staffId)` → `ApproveKycUseCase`
- `RejectKycCommand(taskId, staffId, reason)` → `RejectKycUseCase`
- `RequestMoreKycInfoCommand(taskId, staffId, notes)` → `RequestMoreKycInfoUseCase`
- `BanOrganizationCommand(orgId, staffId, reason)` → `BanOrganizationUseCase`
- `UnbanOrganizationCommand(orgId, staffId)` → `UnbanOrganizationUseCase`
- `AddAdminNoteCommand(orgId, staffId, content, category)` → `AddAdminNoteUseCase`

---

## 7. Queries

- `ListKycReviewTasksQuery(status, assignedTo)` → `Page<KycReviewTaskResult>`
- `GetOrganizationAdminViewQuery(orgId)` → `OrganizationAdminViewResult` — full org details for admin
- `ListAdminNotesQuery(orgId)` → `List<AdminNoteResult>`
- `GetPlatformRevenueReportQuery(dateFrom, dateTo)` → `PlatformRevenueReport`
- `ListAdminStaffQuery(role, isActive)` → `List<AdminStaffResult>`

---

## 8. Listeners

- **`ComplianceSubmittedListener`**: `ComplianceSubmittedEvent` from `compliance`. Creates a `KycReviewTask` and notifies compliance officers via `notifications`.
