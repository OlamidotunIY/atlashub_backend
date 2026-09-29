# Maker-Checker (Four-Eyes Principle) Architecture

## What Is Maker-Checker?

Maker-Checker is a financial control pattern where **any high-risk financial mutation requires two separate authorized users** to complete. One user initiates (the "maker"); a different user reviews and approves (the "checker"). Neither can fulfill both roles for the same transaction.

This pattern is mandated by CBN (Central Bank of Nigeria) guidelines for financial institutions and is considered best practice in any system handling significant amounts of money. It prevents:
- Insider fraud (a malicious employee cannot unilaterally move large amounts)
- Human error (a second reviewer catches mistakes before money moves)
- Collusion (requires two people to be compromised, not one)

---

## Operations Subject to Maker-Checker

| Module | Operation | Threshold |
|---|---|---|
| `pay:transfers` | Outbound bank transfer (Payout) | Any amount above ₦100,000 (configurable per org) |
| `hr:payroll` | Payroll run approval | All payroll runs, regardless of amount |
| `accounting:gl` | Manual journal entry | Entries above ₦500,000 (configurable per org) |
| `billing` (internal) | Large invoice approval | Platform-level — AtlasHub internal |

---

## The Pattern: Domain-Level Enforcement

Maker-Checker is enforced **inside the domain aggregate**, not just at the API or application layer. Even if someone bypasses authentication or permission checks, the domain method itself rejects self-approval.

There is **no shared maker-checker infrastructure class**. It is a domain convention — each aggregate that requires it implements the `approve()` method with the self-approval guard.

```java
// Example: PayrollRun aggregate
public void approve(Long approverId) {
    if (this.status != PayrollStatus.PENDING_APPROVAL) {
        throw new InvalidPayrollStateException(
            "Payroll is not pending approval — current status: " + this.status);
    }
    if (approverId.equals(this.initiatedBy)) {
        throw new SelfApprovalNotAllowedException(
            "The approver cannot be the same person who initiated the payroll run");
    }
    this.approvedBy = approverId;
    this.approvedAt = ZonedDateTime.now();
    this.status = PayrollStatus.APPROVED;
    registerEvent(new PayrollApprovedEvent(
        UUID.randomUUID().toString(),
        String.valueOf(this.id),
        ZonedDateTime.now(),
        new PayrollApprovedEvent.Payload(this.id, this.organizationId, approverId)
    ));
}
```

The same pattern is applied in `Payout.approve()` and `JournalEntry.approve()`.

> [!IMPORTANT]
> `registerEvent()` adds the domain event to the aggregate's internal event list. The event is **not published directly** — it is published automatically when `repository.save(aggregate)` is called. `JpaBaseRepository` reads the registered events and writes them to the outbox table within the same transaction. Do NOT call `publishEvents(aggregate, publisher)` directly.

The correct approval handler pattern:
```java
@Component
public class ApprovePayrollRunHandler extends Command<ApprovePayrollRunCommand, Void> {

    private final PayrollRunRepository payrollRunRepository;

    @Override
    @PreAuthorize("hasAuthority('hr:payroll:approve')")
    public Void execute(ApprovePayrollRunCommand command) {
        PayrollRun run = payrollRunRepository.findById(command.payrollRunId())
            .orElseThrow(() -> new PayrollRunNotFoundException(command.payrollRunId()));

        run.approve(command.approverId());  // domain method — may throw SelfApprovalNotAllowedException

        payrollRunRepository.save(run);     // ← this publishes PayrollApprovedEvent via outbox
        return null;
    }
}
```

---

## Domain Exceptions for Maker-Checker

Each module implements its own named exception classes. No shared error code enum.

### HR Module
```java
public class SelfApprovalNotAllowedException extends BusinessRuleException {
    public SelfApprovalNotAllowedException() {
        super("The approver cannot be the same person who initiated this operation");
    }
    public SelfApprovalNotAllowedException(String message) { super(message); }
}

public class InvalidPayrollStateException extends BusinessRuleException {
    public InvalidPayrollStateException(String message) { super(message); }
}

public class InsufficientApproversException extends BusinessRuleException {
    public InsufficientApproversException() {
        super("At least 2 members with the approval permission are required");
    }
}
```

### Pay Module
```java
public class PayoutSelfApprovalException extends BusinessRuleException {
    public PayoutSelfApprovalException() {
        super("The approver cannot be the initiator of this payout");
    }
}
```

### Accounting Module
```java
public class JournalEntrySelfApprovalException extends BusinessRuleException {
    public JournalEntrySelfApprovalException() {
        super("The approver cannot be the same person who created this journal entry");
    }
}
```

---

## Approval Notification via WebSocket

When a maker submits for approval, the operation transitions to a `PENDING_APPROVAL` state and all eligible approvers (users with the appropriate permission) are notified immediately via:
1. **WebSocket** — real-time in-app notification (appears as a badge/alert in the UI)
2. **Email** — notification with summary and a deep-link to the approval page

The `SelectiveWebSocketBroadcaster` in the notifications module infrastructure handles the WebSocket push. The module simply publishes the domain event; the broadcaster decides to push via WS.

WebSocket push payload example (for payroll):
```json
{
  "type": "APPROVAL_REQUIRED",
  "message": "Payroll run for September 2026 needs your approval",
  "data": {
    "entityType": "PAYROLL_RUN",
    "entityId": 42
  },
  "timestamp": "2026-09-27T08:00:00Z"
}
```

**Channel**: `/user/{approverId}/queue/notifications` — delivered only to eligible approvers.

---

## Approval Expiry

Pending approvals that are not actioned within 72 hours (configurable) are automatically expired. On expiry:
- The operation transitions back to DRAFT (for payroll) or is VOIDED (for journal entries)
- The maker is notified via email
- The audit log records the expiry

```java
// Scheduled job — runs every hour
// In: HR module infrastructure/schedulers/PayrollApprovalExpiryScheduler.java
@Component
public class PayrollApprovalExpiryScheduler {

    private final PayrollRunRepository payrollRunRepository;
    private final ExpirePayrollApprovalHandler expireHandler;

    @Scheduled(cron = "0 0 * * * *")
    public void expireStalePendingApprovals() {
        ZonedDateTime threshold = ZonedDateTime.now().minusHours(72);
        List<PayrollRun> stale = payrollRunRepository
            .findByStatusAndSubmittedForApprovalBefore(PayrollStatus.PENDING_APPROVAL, threshold);

        stale.forEach(run -> {
            run.expire();
            payrollRunRepository.save(run);  // ← publishes PayrollApprovalExpiredEvent via outbox
        });
    }
}
```

---

## Audit Trail for Maker-Checker Events

Every maker-checker action is recorded in the platform audit log:

| Action | Audit Fields Recorded |
|---|---|
| Maker initiates | `userId`, `operation`, `entityType`, `entityId`, `amount`, `timestamp` |
| Checker approves | `approverId`, `approverRole`, `entityId`, `approvedAt`, `timestamp` |
| Checker rejects | `rejectorId`, `rejectionReason`, `entityId`, `timestamp` |
| Auto-expiry | `entityId`, `expiredAt`, `reason: "72-hour timeout"` |

The `audit` module subscribes to:
- `PayrollApprovedEvent`, `PayrollRejectedEvent`, `PayrollApprovalExpiredEvent`
- `PayoutApprovedEvent`, `PayoutRejectedEvent`
- `JournalEntryApprovedEvent`, `JournalEntryRejectedEvent`

---

## Per-Org Threshold Configuration

Organizations can configure their own approval thresholds:

```
OrgMakerCheckerConfig
├── organizationId: Long
├── payoutApprovalThreshold: Money     ← default ₦100,000
├── journalApprovalThreshold: Money    ← default ₦500,000
└── updatedAt: ZonedDateTime
```

If no config exists for an org, the platform defaults apply. Admins (OWNER or users with `pay:transfers:approve` permission) can update this via the settings API.

---

## Minimum Approver Pool Validation

An organization must have at least **one other active member** with the appropriate approval permission before they can initiate a maker-checker operation. If only one person exists in the org with `hr:payroll:approve`, they cannot initiate a payroll run because no one else can approve it.

`InitiatePayrollRunHandler` pre-validates:
```java
int approverCount = membershipQueryPort.countMembersWithPermission(orgId, "hr:payroll:approve");
if (approverCount < 2) {
    throw new InsufficientApproversException();
}
```

`MembershipQueryPort` is defined in `hr` application/port and implemented by `MembershipQueryAdapter` in `atlashub-platform:iam` infrastructure services.

---

## How to Implement Maker-Checker in a New Module

Follow these steps exactly for any new operation that requires four-eyes approval:

### Step 1 — Add status enum values to the aggregate
```java
public enum NewEntityStatus {
    DRAFT,
    PENDING_APPROVAL,  // ← maker has submitted
    APPROVED,
    REJECTED,
    EXPIRED
}
```

### Step 2 — Add `initiatedBy` field to the aggregate
```java
private Long initiatedBy;  // userId of the maker
```

### Step 3 — Add `approve()` and `reject()` domain methods
```java
public void approve(Long approverId) {
    if (this.status != NewEntityStatus.PENDING_APPROVAL) {
        throw new InvalidEntityStateException("Entity is not pending approval");
    }
    if (approverId.equals(this.initiatedBy)) {
        throw new SelfApprovalNotAllowedException();
    }
    this.status = NewEntityStatus.APPROVED;
    this.approvedBy = approverId;
    this.approvedAt = ZonedDateTime.now();
    registerEvent(new EntityApprovedEvent(...));
}

public void reject(Long rejectorId, String reason) {
    if (this.status != NewEntityStatus.PENDING_APPROVAL) {
        throw new InvalidEntityStateException("Entity is not pending approval");
    }
    this.status = NewEntityStatus.REJECTED;
    this.rejectionReason = reason;
    registerEvent(new EntityRejectedEvent(...));
}
```

### Step 4 — Implement named exception classes
```java
public class SelfApprovalNotAllowedException extends BusinessRuleException { ... }
public class InvalidEntityStateException extends BusinessRuleException { ... }
```

### Step 5 — Implement the handlers
```java
// ApproveEntityHandler — calls aggregate.approve(), then repository.save()
// RejectEntityHandler — calls aggregate.reject(), then repository.save()
```

### Step 6 — Add `@PreAuthorize` to approval handlers
```java
@PreAuthorize("hasAuthority('module:resource:approve')")
public Void execute(ApproveEntityCommand command) { ... }
```

### Step 7 — Validate approver pool in the initiate handler
```java
if (membershipQueryPort.countMembersWithPermission(orgId, "module:resource:approve") < 2) {
    throw new InsufficientApproversException();
}
```

### Step 8 — Publish event that triggers WS notification
The initiate handler's aggregate.submitForApproval() registers an `EntityPendingApprovalEvent`. `repository.save()` publishes it. The `SelectiveWebSocketBroadcaster` in notifications picks it up and pushes to approvers' sessions.

### Step 9 — Add expiry scheduler (if needed)
Add a `@Scheduled` component that queries for `PENDING_APPROVAL` records older than the threshold, calls `aggregate.expire()`, and `repository.save()`.
