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

```java
// Example: PayrollRun aggregate
public void approve(Long approverId) {
    if (this.status != PayrollStatus.PENDING_APPROVAL) {
        throw new BusinessRuleException(HrErrorCode.INVALID_PAYROLL_STATE,
            "Payroll is not pending approval");
    }
    if (approverId.equals(this.initiatedBy)) {
        throw new BusinessRuleException(HrErrorCode.SELF_APPROVAL_NOT_ALLOWED,
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

---

## Approval Notification via WebSocket

When a maker submits for approval, the operation transitions to a `PENDING_APPROVAL` state and all eligible approvers (users with the `hr:payroll:approve` permission) are notified immediately via:
1. **WebSocket** — real-time in-app notification (appears as a badge/alert in the UI)
2. **Email** — notification with summary and a deep-link to the approval page

The WebSocket broadcast:
```
Topic: /user/{approverId}/queue/notifications
Payload: {
  "type": "APPROVAL_REQUIRED",
  "entityType": "PAYROLL_RUN",
  "entityId": 42,
  "initiatedBy": "John Doe",
  "summary": "September 2026 Payroll — ₦8,450,000",
  "actionUrl": "/hr/payroll/42/review"
}
```

---

## Approval Expiry

Pending approvals that are not actioned within 72 hours (configurable) are automatically expired. On expiry:
- The operation transitions back to DRAFT (for payroll) or is VOIDED (for journal entries)
- The maker is notified via email
- The audit log records the expiry

```java
// Scheduled job: runs every hour
@Scheduled(cron = "0 0 * * * *")
public void expireStalePendingApprovals() {
    ZonedDateTime threshold = ZonedDateTime.now().minusHours(72);
    List<PayrollRun> stale = payrollRunRepository.findByStatusAndCreatedAtBefore(
        PayrollStatus.PENDING_APPROVAL, threshold);
    stale.forEach(run -> {
        run.expire();
        payrollRunRepository.save(run);
        publishEvents(run, publisher);
    });
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

## Minimum Approver Pool

An organization must have at least **one other active member** with the appropriate approval permission before they can enable a maker-checker operation. If only one person exists in the org with `hr:payroll:approve`, they cannot initiate a payroll run because no one else can approve it.

`InitiatePayrollUseCase` pre-validates:
```java
int approverCount = membershipQueryPort.countMembersWithPermission(orgId, "hr:payroll:approve");
if (approverCount < 2) {
    throw new BusinessRuleException(HrErrorCode.INSUFFICIENT_APPROVERS,
        "At least 2 members with hr:payroll:approve permission are required to use payroll");
}
```
