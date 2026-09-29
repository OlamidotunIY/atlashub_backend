# HR Module Design (`atlashub-hr`) — Index

## Overview

The `atlashub-hr` module is the **people operations engine** of AtlasHub. It is a single Gradle module (`atlashub-hr`) with five internal subpackages, each documented separately.

All subpackages share the root package `com.atlashub.hr`. There is no separate Gradle module per subpackage — they all compile into the single `atlashub-hr` artifact.

---

## Subpackage Documentation

| Subpackage | Package | Design Doc |
|---|---|---|
| **Staff & Salary Grades** | `com.atlashub.hr.staff` | [hr-staff-design.md](hr-staff-design.md) |
| **Leave Management** | `com.atlashub.hr.leave` | [hr-leave-design.md](hr-leave-design.md) |
| **Attendance Tracking** | `com.atlashub.hr.attendance` | [hr-attendance-design.md](hr-attendance-design.md) |
| **Employee Loans** | `com.atlashub.hr.loan` | [hr-loan-design.md](hr-loan-design.md) |
| **Payroll & Disbursement** | `com.atlashub.hr.payroll` | [hr-payroll-design.md](hr-payroll-design.md) |

---

## Key Cross-Cutting Concerns

### Maker-Checker (Payroll)
Payroll disbursement enforces the four-eyes principle at the domain level. See [hr-payroll-design.md](hr-payroll-design.md) and [maker-checker.md](../architecture/maker-checker.md).

### Payroll Disbursement Saga
The payroll → pay → accounting → notifications cross-module saga is choreography-based. See [sagas-design.md](../architecture/sagas-design.md).

### Module Dependencies
- **Reads from shared:** `MembershipQueryPort`, `EntitlementQueryPort`
- **Publishes events to:** `pay-events` topic consumed by `pay` and `accounting`
- **Consumes events from:** `iam-events` (`UserInvitationAcceptedEvent`), `pay-events` (`BulkPayoutCompletedEvent`, `BulkPayoutFailedEvent`)

---

## Gradle

```groovy
// settings.gradle
include 'atlashub-hr'

// atlashub-hr/build.gradle — depends on atlashub-shared
```
