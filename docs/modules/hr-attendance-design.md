# HR Attendance Design (`atlashub-hr` / `com.atlashub.hr.attendance`)

## Role & Purpose

The `attendance` subpackage tracks employee clock-in/clock-out events, calculates hours worked and overtime per shift, and provides monthly attendance summaries. Attendance data feeds into payroll when the organization uses attendance-based pay.

Gradle module: `atlashub-hr`  
Package: `com.atlashub.hr.attendance`

---

## Domain Layer

### `AttendanceRecord` (Aggregate Root)

**Package:** `com.atlashub.hr.attendance.domain.entities`

```
AttendanceRecord
├── id             : Long
├── employeeId     : Long
├── organizationId : Long
├── date           : LocalDate
├── clockIn        : ZonedDateTime    ← nullable; set on clock-in
├── clockOut       : ZonedDateTime    ← nullable; set on clock-out
├── hoursWorked    : BigDecimal       ← calculated on clock-out
├── overtimeHours  : BigDecimal       ← hours beyond configured shift length
└── status         : AttendanceStatus ← PRESENT | ABSENT | LATE | HALF_DAY | ON_LEAVE
```

**Invariants:**
- Only one `AttendanceRecord` per `(employeeId, date)` pair
- `clockOut` may not be set before `clockIn`
- `status` is set to `LATE` if `clockIn` is after the configured shift start time

**Business methods (on entity — operate entirely on own fields):**

| Method | Inputs | What it does | Events |
|---|---|---|---|
| `clockIn(ZonedDateTime time, ZonedDateTime shiftStart)` | time, shiftStart | Sets `clockIn`, marks PRESENT; marks LATE if `time > shiftStart` | — |
| `clockOut(ZonedDateTime time, int shiftHours)` | time, shiftHours | Sets `clockOut`; `hoursWorked = duration(clockIn, time)` in hours; `overtimeHours = max(0, hoursWorked - shiftHours)` | — |

**Why on entity:** Both methods operate solely on the record's own fields — no other aggregate involved.

---

### Value Objects

None specific — uses `AttendanceStatus` enum in `domain/valueobject/`.

---

### Domain Events

No domain events published by attendance — attendance is consumed by leave (`LeaveApprovedEvent` tells attendance to mark an employee's upcoming days as `ON_LEAVE`) but attendance itself does not publish events in MVP.

---

### Domain Exceptions — `com.atlashub.hr.attendance.domain.exceptions`

```java
public class AttendanceRecordNotFoundException extends NotFoundException {
    public AttendanceRecordNotFoundException(Long id) { super("Attendance record not found: " + id); }
}
public class AlreadyClockedInException extends ConflictException {
    public AlreadyClockedInException() { super("Employee has already clocked in today"); }
}
public class NotClockedInException extends BusinessRuleException {
    public NotClockedInException() { super("No open clock-in record found for today"); }
}
```

---

## Application Layer

### Commands — `com.atlashub.hr.attendance.application.commands`

#### `ClockInCommand`
```java
record ClockInCommand(Long employeeId, Long organizationId)
```
**Handler:** `ClockInHandler` | **Response:** `void`  
**Flow:**
1. Check no existing open `AttendanceRecord` for `(employeeId, today)` → `AlreadyClockedInException`
2. Load configured shift start time for the employee's outlet/org
3. Create `AttendanceRecord` with `date = today`
4. `record.clockIn(now(), shiftStart)` — sets status to PRESENT or LATE
5. `repository.save(record)`

---

#### `ClockOutCommand`
```java
record ClockOutCommand(Long employeeId, Long organizationId)
```
**Handler:** `ClockOutHandler` | **Response:** `void`  
**Flow:**
1. Load today's open record for employee → `NotClockedInException` if none
2. Load configured shift hours for org
3. `record.clockOut(now(), configuredShiftHours)`
4. `repository.save(record)`

---

### Queries — `com.atlashub.hr.attendance.application.queries`

#### `GetAttendanceSummaryQuery`
```java
record GetAttendanceSummaryQuery(Long organizationId, Long employeeId, int month, int year)
```
**Handler:** `GetAttendanceSummaryHandler` | **Result:** `AttendanceSummaryResult`  
**Justification:** Single result object — not paginated; bounded to a calendar month.

`AttendanceSummaryResult`:
```java
record AttendanceSummaryResult(
    int totalDays,
    int presentDays,
    int absentDays,
    int lateDays,
    BigDecimal totalHoursWorked,
    BigDecimal totalOvertimeHours,
    List<AttendanceDayResult> records
)
```

`AttendanceDayResult`:
```java
record AttendanceDayResult(
    LocalDate date,
    ZonedDateTime clockIn,
    ZonedDateTime clockOut,
    BigDecimal hoursWorked,
    BigDecimal overtimeHours,
    AttendanceStatus status
)
```

---

## Infrastructure Layer

### Persistence

**JPA Entity:**

| Entity | Table | Locking |
|---|---|---|
| `AttendanceRecordJpaEntity` | `hr_attendance_records` | — |

**Spring Data Repository:**

```
AttendanceRecordJpaRepository
  + findByEmployeeIdAndDate(Long employeeId, LocalDate date): Optional<AttendanceRecordJpaEntity>
  + findByOrganizationIdAndEmployeeIdAndDateBetween(
      Long orgId, Long employeeId, LocalDate from, LocalDate to
    ): List<AttendanceRecordJpaEntity>
```

**Mapper:**
- `AttendanceRecordMapper` — `AttendanceRecord ↔ AttendanceRecordJpaEntity`

**Repository Adapter:**
- `AttendanceRecordRepositoryAdapter` → sequence: `hr_attendance_seq`

---

## Presentation Layer

### Controller: `HrAttendanceController`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/attendance/clock-in` | — | `ClockInRequest` | `void` |
| `POST` | `/attendance/clock-out` | — | `ClockOutRequest` | `void` |
| `GET` | `/attendance/summary` | — | `?orgId&employeeId&month&year` | `AttendanceSummaryResult` |

### DTOs

- `ClockInRequest`: `employeeId`, `organizationId`
- `ClockOutRequest`: `employeeId`, `organizationId`
- `AttendanceSummaryResult`: (see query result above — used as both application result and response DTO)
- `AttendanceDayResult`: (see query result above)

---

## Distributed Architecture

### Locking
- No locking required for attendance — each record is unique per `(employeeId, date)`, managed by the uniqueness constraint on the table.

---

## Complete File List

```
atlashub-hr/src/main/java/com/atlashub/hr/attendance/
├── domain/
│   ├── entities/
│   │   └── AttendanceRecord.java
│   ├── exceptions/
│   │   ├── AttendanceRecordNotFoundException.java
│   │   ├── AlreadyClockedInException.java
│   │   └── NotClockedInException.java
│   ├── repositories/
│   │   └── AttendanceRecordRepository.java
│   └── valueobject/
│       └── AttendanceStatus.java
├── application/
│   ├── commands/
│   │   ├── ClockIn/
│   │   │   ├── ClockInCommand.java
│   │   │   └── ClockInHandler.java
│   │   └── ClockOut/
│   │       ├── ClockOutCommand.java
│   │       └── ClockOutHandler.java
│   └── queries/
│       └── GetAttendanceSummary/
│           ├── GetAttendanceSummaryQuery.java
│           ├── GetAttendanceSummaryHandler.java
│           ├── AttendanceSummaryResult.java
│           └── AttendanceDayResult.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   └── AttendanceRecordRepositoryAdapter.java
│   │   ├── entities/
│   │   │   └── AttendanceRecordJpaEntity.java
│   │   ├── mappers/
│   │   │   └── AttendanceRecordMapper.java
│   │   └── repositories/
│   │       └── AttendanceRecordJpaRepository.java
└── presentation/
    ├── dto/
    │   ├── ClockInRequest.java
    │   ├── ClockOutRequest.java
    │   ├── AttendanceSummaryResult.java
    │   └── AttendanceDayResult.java
    └── rest/
        └── HrAttendanceController.java
```
