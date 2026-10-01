# Admin Module Design (`atlashub-platform:admin`)

## Role & Purpose

The `admin` module is the **internal operations portal** for AtlasHub staff. It exposes tooling for the platform's own team to manage businesses on the platform — reviewing KYC applications, monitoring organizations, handling support escalations, taking corrective actions, and running platform revenue reports.

Admin is a **multi-tier module**: different AtlasHub staff roles have different levels of access. A support agent can view an organization's compliance details and leave notes, but only a compliance officer can approve KYC, and only a super admin can ban an organization or manage other staff members.

The `admin` module does **not** own compliance state directly. When an admin approves or rejects a KYC submission, the `admin` module publishes `KycApprovedEvent` or `KycRejectedEvent` to Kafka. The `compliance` module listens to those events and transitions its own `ComplianceRecord` state. This keeps compliance business rules encapsulated in the right bounded context and preserves strict async event-driven cross-module boundaries — **no synchronous cross-module write calls**.

---

## 1. Features

### Staff Tiers & Permissions

| Staff Role | Capabilities |
|---|---|
| **SUPER_ADMIN** | Full platform access — manage admin staff, ban/unban orgs, view all data |
| **COMPLIANCE_OFFICER** | Review and action KYC submissions — approve, reject, request more info |
| **SUPPORT_AGENT** | View org details, leave internal notes, escalate |
| **FINANCE_ANALYST** | Read-only access to platform financial data, revenue reports |

Staff roles are fixed — not configurable by admins themselves.

### KYC Review Workflow
When `ComplianceSubmittedEvent` is received, a `KycReviewTask` is created. A compliance officer picks it up, reviews documents, and takes one of three actions: **Approve** (via `ComplianceActionPort.approve()`), **Reject** (via `ComplianceActionPort.reject()`), or **Request More Info** (transitions task to `WAITING_ON_ORG`).

### Organization Management
Admins can view any organization's full profile and take corrective actions:
- **Ban Organization** → terminates subscriptions, freezes virtual accounts, notifies org
- **Unban Organization** → reverses a ban (super admin only)

### Internal Notes
Support agents can attach `AdminNote` records to any organization's record. Notes are internal-only — never visible to the organization.

### Platform Revenue Analytics
Finance analysts query `GetPlatformRevenueReport` for total transaction volume, platform fee revenue, and settlement summaries across a date range.

---

## 2. Domain Layer

### 2.1 Aggregates & Entities

#### `AdminStaff` (Aggregate Root)

```
AdminStaff
├── id: Long
├── userId: Long                     ← references auth:User
├── staffRole: AdminStaffRole        ← SUPER_ADMIN | COMPLIANCE_OFFICER | SUPPORT_AGENT | FINANCE_ANALYST | CATALOG_MANAGER
├── active: Boolean
├── onboardedAt: ZonedDateTime
└── deactivatedAt: ZonedDateTime     ← nullable
```

**Business Methods:**
- `deactivate()` → sets `active = false`, records `deactivatedAt` — guard: staff must currently be active
- `changeRole(AdminStaffRole newRole)` → updates `staffRole`

---

#### `KycReviewTask` (Aggregate Root)

```
KycReviewTask
├── id: Long
├── organizationId: Long
├── status: ReviewTaskStatus         ← OPEN | IN_REVIEW | WAITING_ON_ORG | APPROVED | REJECTED
├── assignedTo: Long                 ← adminStaffId, nullable
├── reviewNotes: String              ← internal notes from reviewer, nullable
├── rejectionReason: String          ← nullable
├── submissionCount: Integer         ← increments on each org re-submission
├── createdAt: ZonedDateTime
└── resolvedAt: ZonedDateTime        ← nullable
```

**Business Methods:**
- `assign(Long staffId)` → transitions `OPEN → IN_REVIEW`, sets `assignedTo`
- `approve(Long staffId)` → transitions `IN_REVIEW → APPROVED`, records `resolvedAt` → registers `KycApprovedEvent`
- `reject(Long staffId, String reason)` → transitions `IN_REVIEW → REJECTED`, records `resolvedAt` and `rejectionReason` → registers `KycRejectedEvent`
- `requestMoreInfo(Long staffId, String notes)` → transitions `IN_REVIEW → WAITING_ON_ORG`, records `reviewNotes`

**Domain Rules:**
- A task can only be approved or rejected from `IN_REVIEW` status — guard throws `ReviewTaskAlreadyResolvedException` if already `APPROVED` or `REJECTED`
- Only the `assignedTo` staff member can take action on an `IN_REVIEW` task (or a SUPER_ADMIN)

---

#### `AdminNote` (Entity)

```
AdminNote
├── id: Long
├── organizationId: Long
├── createdBy: Long                  ← adminStaffId
├── content: String
├── category: NoteCategory           ← COMPLIANCE | SUPPORT | FINANCIAL | GENERAL
└── createdAt: ZonedDateTime
```

---

### 2.2 Value Objects

| Type | Kind | Values |
|---|---|---|
| `AdminStaffRole` | Enum | `SUPER_ADMIN`, `COMPLIANCE_OFFICER`, `SUPPORT_AGENT`, `FINANCE_ANALYST`, `CATALOG_MANAGER` |
| `ReviewTaskStatus` | Enum | `OPEN`, `IN_REVIEW`, `WAITING_ON_ORG`, `APPROVED`, `REJECTED` |
| `NoteCategory` | Enum | `COMPLIANCE`, `SUPPORT`, `FINANCIAL`, `GENERAL` |

---

### 2.3 Domain Events

**Package**: `com.atlashub.admin.domain.events` | **Kafka topic**: `admin-events`

| Event | Published When | Consumed By |
|---|---|---|
| `KycReviewTaskCreatedEvent` | `ComplianceSubmittedEvent` received → task created | `notifications` (alert compliance officers) |
| `KycApprovedEvent` | Admin approves KYC task | `compliance` (listens → approves ComplianceRecord), `notifications` (email org) |
| `KycRejectedEvent` | Admin rejects KYC task | `compliance` (listens → rejects ComplianceRecord), `notifications` (email org with reason) |
| `KycMoreInfoRequestedEvent` | Admin requests more info | `notifications` (email org with notes) |
| `OrganizationBannedEvent` | Admin bans org | `pay:accounts` (freeze virtual accounts), `pay:ledger` (freeze all ledger accounts), `billing` (cancel subscriptions), `auth` (revoke all sessions), `notifications` |
| `OrganizationUnbannedEvent` | Super admin unbans org | `pay:accounts` (unfreeze), `pay:ledger` (unfreeze), `billing` (restore subscriptions), `notifications` |

**Event payload shapes:**

```
KycApprovedEvent.payload
├── taskId           : Long
├── organizationId   : Long
├── approvedByStaffId: Long
└── approvedAt       : ZonedDateTime

KycRejectedEvent.payload
├── taskId           : Long
├── organizationId   : Long
├── rejectedByStaffId: Long
├── reason           : String
└── rejectedAt       : ZonedDateTime

KycMoreInfoRequestedEvent.payload
├── taskId         : Long
├── organizationId : Long
├── requestedByStaffId : Long
├── notes          : String
└── requestedAt    : ZonedDateTime

OrganizationBannedEvent.payload
├── organizationId : Long
├── bannedByStaffId: Long
├── reason         : String
└── bannedAt       : ZonedDateTime

OrganizationUnbannedEvent.payload
├── organizationId   : Long
├── unbannedByStaffId: Long
└── unbannedAt       : ZonedDateTime
```

---

### 2.5 Domain Exceptions

```java
public class AdminStaffNotFoundException extends NotFoundException {
    public AdminStaffNotFoundException(Long staffId) {
        super("Admin staff not found: " + staffId);
    }
}

public class InsufficientAdminPrivilegesException extends AuthorizationException {
    public InsufficientAdminPrivilegesException(String message) { super(message); }
}

public class ReviewTaskNotFoundException extends NotFoundException {
    public ReviewTaskNotFoundException(Long taskId) {
        super("KYC review task not found: " + taskId);
    }
}

public class ReviewTaskAlreadyResolvedException extends BusinessRuleException {
    public ReviewTaskAlreadyResolvedException(Long taskId) {
        super("KYC review task " + taskId + " is already resolved");
    }
}

public class OrganizationAlreadyBannedException extends ConflictException {
    public OrganizationAlreadyBannedException(Long orgId) {
        super("Organization " + orgId + " is already banned");
    }
}
```

---

## 3. Application Layer

### 3.1 Commands

#### `OnboardAdminStaffCommand`

```java
public record OnboardAdminStaffCommand(
    Long userId,
    AdminStaffRole staffRole
) {}
```

**Handler:** `OnboardAdminStaffHandler extends Command<OnboardAdminStaffCommand, OnboardAdminStaffResponse>`
- Creates `AdminStaff` aggregate
- Returns `OnboardAdminStaffResponse(Long staffId)`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:staff:manage')")`

---

#### `DeactivateAdminStaffCommand`

```java
public record DeactivateAdminStaffCommand(Long staffId) {}
```

**Handler:** `DeactivateAdminStaffHandler extends Command<DeactivateAdminStaffCommand, Void>`
- Loads `AdminStaff` → throws `AdminStaffNotFoundException`
- Calls `staff.deactivate()`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:staff:manage')")`

---

#### `ChangeAdminStaffRoleCommand`

```java
public record ChangeAdminStaffRoleCommand(
    Long staffId,
    AdminStaffRole newRole
) {}
```

**Handler:** `ChangeAdminStaffRoleHandler extends Command<ChangeAdminStaffRoleCommand, Void>`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:staff:manage')")`

---

#### `AssignKycReviewTaskCommand`

```java
public record AssignKycReviewTaskCommand(
    Long taskId,
    Long staffId
) {}
```

**Handler:** `AssignKycReviewTaskHandler extends Command<AssignKycReviewTaskCommand, Void>`
- Loads `KycReviewTask` → throws `ReviewTaskNotFoundException`
- Calls `task.assign(staffId)`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:kyc:approve')")`

---

#### `ApproveKycCommand`

```java
public record ApproveKycCommand(
    Long taskId,
    Long staffId
) {}
```

**Handler:** `ApproveKycHandler extends Command<ApproveKycCommand, Void>`
- Loads task → throws `ReviewTaskNotFoundException`, `ReviewTaskAlreadyResolvedException`
- Calls `task.approve(staffId)` → registers `KycApprovedEvent`
- Saves task → `KycApprovedEvent` published via outbox to Kafka
- `compliance` module listens to `KycApprovedEvent` asynchronously and approves its own `ComplianceRecord` — no sync cross-module call
- **RBAC:** `@PreAuthorize("hasAuthority('admin:kyc:approve')")`


---

#### `RejectKycCommand`

```java
public record RejectKycCommand(
    Long taskId,
    Long staffId,
    String reason
) {}
```

**Handler:** `RejectKycHandler extends Command<RejectKycCommand, Void>`
- Loads task → validates state
- Calls `task.reject(staffId, reason)` → registers `KycRejectedEvent`
- Saves task → `KycRejectedEvent` published via outbox to Kafka
- `compliance` module listens to `KycRejectedEvent` asynchronously and rejects its own `ComplianceRecord` — no sync cross-module call
- **RBAC:** `@PreAuthorize("hasAuthority('admin:kyc:reject')")`


---

#### `RequestMoreKycInfoCommand`

```java
public record RequestMoreKycInfoCommand(
    Long taskId,
    Long staffId,
    String notes
) {}
```

**Handler:** `RequestMoreKycInfoHandler extends Command<RequestMoreKycInfoCommand, Void>`
- Calls `task.requestMoreInfo(staffId, notes)`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:kyc:approve')")`

---

#### `BanOrganizationCommand`

```java
public record BanOrganizationCommand(
    Long organizationId,
    Long staffId,
    String reason
) {}
```

**Handler:** `BanOrganizationHandler extends Command<BanOrganizationCommand, Void>`
- Validates org is not already banned → throws `OrganizationAlreadyBannedException`
- Records ban with reason, publishes `OrganizationBannedEvent`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:organizations:ban')")`

---

#### `UnbanOrganizationCommand`

```java
public record UnbanOrganizationCommand(
    Long organizationId,
    Long staffId
) {}
```

**Handler:** `UnbanOrganizationHandler extends Command<UnbanOrganizationCommand, Void>`
- Validates org is currently banned
- Publishes `OrganizationUnbannedEvent`
- **RBAC:** `@PreAuthorize("hasAuthority('admin:organizations:ban')")`

---

#### `AddAdminNoteCommand`

```java
public record AddAdminNoteCommand(
    Long organizationId,
    Long staffId,
    String content,
    NoteCategory category
) {}
```

**Handler:** `AddAdminNoteHandler extends Command<AddAdminNoteCommand, AddAdminNoteResponse>`
- Creates `AdminNote`, persists
- Returns `AddAdminNoteResponse(Long noteId)`
- **RBAC:** any authenticated admin staff member

---

### 3.2 Queries

#### `ListKycReviewTasksQuery`

```java
public record ListKycReviewTasksQuery(
    ReviewTaskStatus status,     // nullable — filter
    Long assignedTo,             // nullable — filter by staff
    int page,
    int size
) {}
```

**Handler:** `ListKycReviewTasksHandler extends Query<ListKycReviewTasksQuery, PageResult<KycReviewTaskResult>>`
**Returns:** `PageResult<KycReviewTaskResult>` — compliance officers work through potentially many tasks from multiple organizations; this is a user-facing pageable queue view.

---

#### `GetOrganizationAdminViewQuery`

```java
public record GetOrganizationAdminViewQuery(Long organizationId) {}
```

**Handler:** `GetOrganizationAdminViewHandler extends Query<GetOrganizationAdminViewQuery, OrganizationAdminViewResult>`
**Returns:** single composite result — pulls org profile, compliance status, ban status, and recent KYC task history.

---

#### `ListAdminNotesQuery`

```java
public record ListAdminNotesQuery(Long organizationId) {}
```

**Handler:** `ListAdminNotesHandler extends Query<ListAdminNotesQuery, List<AdminNoteResult>>`
**Returns:** `List<AdminNoteResult>` — notes on a given org are a small bounded set; never paginated in admin tooling.

---

#### `GetPlatformRevenueReportQuery`

```java
public record GetPlatformRevenueReportQuery(
    LocalDate dateFrom,
    LocalDate dateTo
) {}
```

**Handler:** `GetPlatformRevenueReportHandler extends Query<GetPlatformRevenueReportQuery, PlatformRevenueReportResult>`
**Returns:** single report result.
**RBAC:** `@PreAuthorize("hasAuthority('admin:kyc:approve') or hasAuthority('admin:staff:manage')")`

---

#### `ListAdminStaffQuery`

```java
public record ListAdminStaffQuery(
    AdminStaffRole role,    // nullable
    Boolean isActive        // nullable
) {}
```

**Handler:** `ListAdminStaffHandler extends Query<ListAdminStaffQuery, List<AdminStaffResult>>`
**Returns:** `List<AdminStaffResult>` — the platform's internal staff list is a small bounded set.

---

## 4. Infrastructure Layer

### 4.1 Persistence

#### JPA Entities

| JPA Entity | Domain Type | Table |
|---|---|---|
| `AdminStaffJpaEntity` | `AdminStaff` | `admin_staff` |
| `KycReviewTaskJpaEntity` | `KycReviewTask` | `kyc_review_tasks` |
| `AdminNoteJpaEntity` | `AdminNote` | `admin_notes` |

`KycReviewTaskJpaEntity` uses `@Version` for optimistic locking (concurrent assignment prevention).

#### Spring Data Repositories

```java
// infrastructure/persistence/repositories/
AdminStaffJpaRepository extends JpaRepository<AdminStaffJpaEntity, Long> {
    Optional<AdminStaffJpaEntity> findByUserId(Long userId);
    List<AdminStaffJpaEntity> findByStaffRoleAndActive(AdminStaffRole, Boolean);
}
KycReviewTaskJpaRepository extends JpaRepository<KycReviewTaskJpaEntity, Long> {
    Page<KycReviewTaskJpaEntity> findByStatusAndAssignedTo(ReviewTaskStatus, Long, Pageable);
    Optional<KycReviewTaskJpaEntity> findByOrganizationIdAndStatusNotIn(Long, List<ReviewTaskStatus>);
}
AdminNoteJpaRepository extends JpaRepository<AdminNoteJpaEntity, Long> {
    List<AdminNoteJpaEntity> findByOrganizationIdOrderByCreatedAtDesc(Long organizationId);
}
```

#### Persistence Adapters

```java
// infrastructure/persistence/adapters/
AdminStaffPersistenceAdapter        // implements AdminStaffRepository (domain)
KycReviewTaskPersistenceAdapter     // implements KycReviewTaskRepository (domain)
AdminNotePersistenceAdapter         // implements AdminNoteRepository (domain)
```

#### Mappers

```java
// infrastructure/persistence/mappers/
AdminStaffMapper
KycReviewTaskMapper
AdminNoteMapper
```

---

### 4.2 Kafka Listeners

#### `ComplianceSubmittedListener`

| Property | Value |
|---|---|
| Topic | `compliance-events` |
| Group ID | `admin-compliance-group` |
| Event handled | `ComplianceSubmittedEvent` |
| Command invoked | Creates `KycReviewTask` directly (no command — task is a new aggregate) |

```java
@Component
public class ComplianceSubmittedListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "admin-compliance-group";

    @PostConstruct
    public void init() { registerSubscription("ComplianceSubmittedEvent", GROUP_ID); }

    @KafkaListener(topics = "compliance-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ComplianceSubmittedEvent",
            ComplianceSubmittedEvent.class, log, GROUP_ID,
            e -> e instanceof TimeoutException,
            event -> {
                // Creates KycReviewTask for the organization
                kycReviewTaskRepository.save(KycReviewTask.open(event.payload().organizationId()));
            });
    }
}
```

---

### 4.3 Infrastructure Services

```java
// infrastructure/services/
ComplianceActionAdapter    // implements ComplianceActionPort (domain)
                           // calls compliance module's ApproveComplianceHandler and RejectComplianceHandler
```

---

## 5. Presentation Layer

### 5.1 Controllers

#### `AdminStaffController` — `/api/v1/admin/staff`

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `POST` | `/` | Bearer | `admin:staff:manage` | Onboard a new admin staff member |
| `DELETE` | `/{staffId}` | Bearer | `admin:staff:manage` | Deactivate admin staff |
| `PUT` | `/{staffId}/role` | Bearer | `admin:staff:manage` | Change admin staff role |
| `GET` | `/` | Bearer | `admin:staff:manage` | List admin staff (filterable by role, isActive) |

---

#### `KycReviewController` — `/api/v1/admin/kyc`

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `GET` | `/tasks` | Bearer | `admin:kyc:approve` | List review tasks (paged, filterable) |
| `POST` | `/tasks/{taskId}/assign` | Bearer | `admin:kyc:approve` | Assign task to a staff member |
| `POST` | `/tasks/{taskId}/approve` | Bearer | `admin:kyc:approve` | Approve KYC submission |
| `POST` | `/tasks/{taskId}/reject` | Bearer | `admin:kyc:reject` | Reject KYC with reason |
| `POST` | `/tasks/{taskId}/request-info` | Bearer | `admin:kyc:approve` | Set task to WAITING_ON_ORG |

---

#### `AdminOrganizationController` — `/api/v1/admin/organizations`

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `GET` | `/{orgId}` | Bearer | any admin staff | Get org admin view (compliance + bans + notes) |
| `POST` | `/{orgId}/ban` | Bearer | `admin:organizations:ban` | Ban an organization |
| `POST` | `/{orgId}/unban` | Bearer | `admin:organizations:ban` | Unban an organization |
| `GET` | `/{orgId}/notes` | Bearer | any admin staff | List internal notes for org |
| `POST` | `/{orgId}/notes` | Bearer | any admin staff | Add internal note to org |

---

#### `AdminReportController` — `/api/v1/admin/reports`

| Method | Path | Auth | RBAC | Description |
|---|---|---|---|---|
| `GET` | `/revenue` | Bearer | `admin:staff:manage` or `admin:kyc:approve` | Platform revenue report |

---

### 5.2 Request/Response DTOs

#### `OnboardAdminStaffRequest`
```java
public record OnboardAdminStaffRequest(
    @NotNull Long userId,
    @NotNull AdminStaffRole staffRole
) {}
```

#### `OnboardAdminStaffResponse`
```java
public record OnboardAdminStaffResponse(Long staffId) {}
```

#### `ChangeAdminRoleRequest`
```java
public record ChangeAdminRoleRequest(@NotNull AdminStaffRole newRole) {}
```

#### `AdminStaffResult`
```java
public record AdminStaffResult(
    Long id,
    Long userId,
    AdminStaffRole staffRole,
    Boolean active,
    ZonedDateTime onboardedAt,
    ZonedDateTime deactivatedAt
) {}
```

#### `AssignTaskRequest`
```java
public record AssignTaskRequest(@NotNull Long staffId) {}
```

#### `RejectKycRequest`
```java
public record RejectKycRequest(@NotBlank String reason) {}
```

#### `RequestMoreInfoRequest`
```java
public record RequestMoreInfoRequest(@NotBlank String notes) {}
```

#### `KycReviewTaskResult`
```java
public record KycReviewTaskResult(
    Long id,
    Long organizationId,
    ReviewTaskStatus status,
    Long assignedTo,
    String reviewNotes,
    String rejectionReason,
    Integer submissionCount,
    ZonedDateTime createdAt,
    ZonedDateTime resolvedAt
) {}
```

#### `BanOrganizationRequest`
```java
public record BanOrganizationRequest(@NotBlank String reason) {}
```

#### `AddAdminNoteRequest`
```java
public record AddAdminNoteRequest(
    @NotBlank String content,
    @NotNull NoteCategory category
) {}
```

#### `AdminNoteResult`
```java
public record AdminNoteResult(
    Long id,
    Long organizationId,
    Long createdBy,
    String content,
    NoteCategory category,
    ZonedDateTime createdAt
) {}
```

#### `OrganizationAdminViewResult`
```java
public record OrganizationAdminViewResult(
    Long organizationId,
    String organizationName,
    String complianceStatus,
    Boolean isBanned,
    String banReason,
    KycReviewTaskResult latestReviewTask
) {}
```

#### `PlatformRevenueReportResult`
```java
public record PlatformRevenueReportResult(
    LocalDate dateFrom,
    LocalDate dateTo,
    BigDecimal totalTransactionVolume,
    BigDecimal totalPlatformFeeRevenue,
    String currency
) {}
```

---

## 6. RBAC Table

| Operation | Permission Required | Notes |
|---|---|---|
| Onboard admin staff | `admin:staff:manage` | SUPER_ADMIN only in practice |
| Deactivate admin staff | `admin:staff:manage` | |
| Change admin staff role | `admin:staff:manage` | |
| List admin staff | `admin:staff:manage` | |
| List/assign KYC review tasks | `admin:kyc:approve` | COMPLIANCE_OFFICER |
| Approve KYC | `admin:kyc:approve` | |
| Reject KYC | `admin:kyc:reject` | Separate permission from approve |
| Request more KYC info | `admin:kyc:approve` | |
| View organization admin view | any authenticated admin | |
| Add internal note | any authenticated admin | |
| Ban organization | `admin:organizations:ban` | |
| Unban organization | `admin:organizations:ban` | |
| View revenue report | `admin:staff:manage` or `admin:kyc:approve` | |

---

## 7. Maker-Checker

The admin module does not enforce Maker-Checker directly. However, by design:
- KYC approval requires a **different staff member** to approve than whoever submitted the compliance data (enforced by `compliance` module when `complianceActionPort.approve()` is called)
- The KYC task `assignedTo` field tracks who last acted on the task for auditability

---

## 8. Socket Events

The `admin` module does not push WebSocket events directly. However, the following domain events published by `admin` may result in WebSocket pushes handled by `SelectiveWebSocketBroadcaster` in the `notifications` module:

| Domain Event | Possible WS Push | Notes |
|---|---|---|
| `KycReviewTaskCreatedEvent` | None — email notification only | No human is watching the admin dashboard live for this |
| `OrganizationBannedEvent` | None — processed async | Downstream modules handle via Kafka |

---

## 9. Domain Events Table

| Event | Published When | Consumed By |
|---|---|---|
| `KycReviewTaskCreatedEvent` | `ComplianceSubmittedEvent` received | `notifications` (alert compliance officers) |
| `KycApprovedEvent` | Admin approves KYC task | `compliance` (via port), `notifications` |
| `KycRejectedEvent` | Admin rejects KYC task | `compliance` (via port), `notifications` |
| `OrganizationBannedEvent` | Admin bans org | `pay:accounts`, `billing`, `notifications` |
| `OrganizationUnbannedEvent` | Super admin unbans org | `billing`, `notifications` |

---

## 10. Distributed Architecture

### Locking
- **Optimistic Locking (`@Version`)**: `KycReviewTaskJpaEntity` — prevents two compliance officers from simultaneously assigning the same open task

### Outbox & Inbox
- **Outbox**: `OrganizationBannedEvent` and `KycApprovedEvent` — must be delivered reliably as they trigger subscription cancellations and NUBAN issuance in downstream modules
- **Inbox**: `ComplianceSubmittedEvent` — processed idempotently. If replayed, the listener checks whether a task already exists for the `organizationId` before creating another

### Cross-Module Call
The `ComplianceActionAdapter` calls compliance module handlers synchronously within the same JVM (intra-service call, no HTTP). This is acceptable because `admin` and `compliance` are in the same deployment unit (`atlashub-platform`). The domain port boundary ensures the `admin` domain model remains decoupled from compliance internals.

---

## 11. Complete File List

```
atlashub-platform/admin/src/main/java/com/atlashub/admin/
│
├── domain/
│   ├── entities/
│   │   ├── AdminNote.java
│   │   ├── AdminStaff.java
│   │   └── KycReviewTask.java
│   ├── events/
│   │   ├── KycApprovedEvent.java
│   │   ├── KycRejectedEvent.java
│   │   ├── KycReviewTaskCreatedEvent.java
│   │   ├── OrganizationBannedEvent.java
│   │   └── OrganizationUnbannedEvent.java
│   ├── exceptions/
│   │   ├── AdminStaffNotFoundException.java
│   │   ├── InsufficientAdminPrivilegesException.java
│   │   ├── OrganizationAlreadyBannedException.java
│   │   ├── ReviewTaskAlreadyResolvedException.java
│   │   └── ReviewTaskNotFoundException.java
│   ├── ports/
│   │   └── ComplianceActionPort.java
│   ├── repositories/
│   │   ├── AdminNoteRepository.java
│   │   ├── AdminStaffRepository.java
│   │   └── KycReviewTaskRepository.java
│   └── valueobject/
│       ├── AdminStaffRole.java
│       ├── NoteCategory.java
│       └── ReviewTaskStatus.java
│
├── application/
│   ├── commands/
│   │   ├── AddAdminNote/
│   │   │   ├── AddAdminNoteCommand.java
│   │   │   ├── AddAdminNoteHandler.java
│   │   │   └── AddAdminNoteResponse.java
│   │   ├── ApproveKyc/
│   │   │   ├── ApproveKycCommand.java
│   │   │   └── ApproveKycHandler.java
│   │   ├── AssignKycReviewTask/
│   │   │   ├── AssignKycReviewTaskCommand.java
│   │   │   └── AssignKycReviewTaskHandler.java
│   │   ├── BanOrganization/
│   │   │   ├── BanOrganizationCommand.java
│   │   │   └── BanOrganizationHandler.java
│   │   ├── ChangeAdminStaffRole/
│   │   │   ├── ChangeAdminStaffRoleCommand.java
│   │   │   └── ChangeAdminStaffRoleHandler.java
│   │   ├── DeactivateAdminStaff/
│   │   │   ├── DeactivateAdminStaffCommand.java
│   │   │   └── DeactivateAdminStaffHandler.java
│   │   ├── OnboardAdminStaff/
│   │   │   ├── OnboardAdminStaffCommand.java
│   │   │   ├── OnboardAdminStaffHandler.java
│   │   │   └── OnboardAdminStaffResponse.java
│   │   ├── RejectKyc/
│   │   │   ├── RejectKycCommand.java
│   │   │   └── RejectKycHandler.java
│   │   ├── RequestMoreKycInfo/
│   │   │   ├── RequestMoreKycInfoCommand.java
│   │   │   └── RequestMoreKycInfoHandler.java
│   │   └── UnbanOrganization/
│   │       ├── UnbanOrganizationCommand.java
│   │       └── UnbanOrganizationHandler.java
│   └── queries/
│       ├── GetOrganizationAdminView/
│       │   ├── GetOrganizationAdminViewQuery.java
│       │   ├── GetOrganizationAdminViewHandler.java
│       │   └── OrganizationAdminViewResult.java
│       ├── GetPlatformRevenueReport/
│       │   ├── GetPlatformRevenueReportQuery.java
│       │   ├── GetPlatformRevenueReportHandler.java
│       │   └── PlatformRevenueReportResult.java
│       ├── ListAdminNotes/
│       │   ├── ListAdminNotesQuery.java
│       │   ├── ListAdminNotesHandler.java
│       │   └── AdminNoteResult.java
│       ├── ListAdminStaff/
│       │   ├── ListAdminStaffQuery.java
│       │   ├── ListAdminStaffHandler.java
│       │   └── AdminStaffResult.java
│       └── ListKycReviewTasks/
│           ├── ListKycReviewTasksQuery.java
│           ├── ListKycReviewTasksHandler.java
│           └── KycReviewTaskResult.java
│
├── infrastructure/
│   ├── messaging/
│   │   └── listeners/
│   │       └── ComplianceSubmittedListener.java
│   ├── persistence/
│   │   ├── adapters/
│   │   │   ├── AdminNotePersistenceAdapter.java
│   │   │   ├── AdminStaffPersistenceAdapter.java
│   │   │   └── KycReviewTaskPersistenceAdapter.java
│   │   ├── entities/
│   │   │   ├── AdminNoteJpaEntity.java
│   │   │   ├── AdminStaffJpaEntity.java
│   │   │   └── KycReviewTaskJpaEntity.java
│   │   ├── mappers/
│   │   │   ├── AdminNoteMapper.java
│   │   │   ├── AdminStaffMapper.java
│   │   │   └── KycReviewTaskMapper.java
│   │   └── repositories/
│   │       ├── AdminNoteJpaRepository.java
│   │       ├── AdminStaffJpaRepository.java
│   │       └── KycReviewTaskJpaRepository.java
│   └── services/
│       └── ComplianceActionAdapter.java
│
└── presentation/
    ├── dto/
    │   ├── AddAdminNoteRequest.java
    │   ├── AdminNoteResult.java
    │   ├── AdminStaffResult.java
    │   ├── AssignTaskRequest.java
    │   ├── BanOrganizationRequest.java
    │   ├── ChangeAdminRoleRequest.java
    │   ├── KycReviewTaskResult.java
    │   ├── OnboardAdminStaffRequest.java
    │   ├── OrganizationAdminViewResult.java
    │   ├── PlatformRevenueReportResult.java
    │   ├── RejectKycRequest.java
    │   └── RequestMoreInfoRequest.java
    └── rest/
        ├── AdminOrganizationController.java
        ├── AdminReportController.java
        ├── AdminStaffController.java
        └── KycReviewController.java
```
