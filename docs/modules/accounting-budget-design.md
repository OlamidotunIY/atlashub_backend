# Accounting Budget Design (`atlashub-accounting` / `com.atlashub.accounting.budget`)

## Role & Purpose

The `budget` subpackage lets organizations define financial budgets per GL account per period (e.g., ₦2M for Marketing in Q3 2026). Budget variance is computed on-demand by querying journal line totals for the account and period against the budgeted amount — no running total is maintained. Budgets require approval (Maker-Checker).

Gradle module: `atlashub-accounting`  
Package: `com.atlashub.accounting.budget`

---

## Domain Layer

### `Budget` (Aggregate Root)

**Package:** `com.atlashub.accounting.budget.domain.entities`

```
Budget
├── id              : Long
├── organizationId  : Long
├── accountId       : Long          ← which GL account this budget applies to
├── name            : String
├── period          : String        ← e.g., "2026-Q3", "2026-09"
├── budgetedAmount  : Money
├── status          : BudgetStatus  ← DRAFT | APPROVED | CLOSED
├── initiatedBy     : Long
├── approvedBy      : Long          ← nullable
└── approvedAt      : ZonedDateTime ← nullable
```

**Business methods (on entity):**
- `approve(approverId)` → DRAFT → APPROVED; validates `approverId ≠ initiatedBy`; registers `BudgetApprovedEvent`; throws `SelfApprovalNotAllowedException`, `InvalidBudgetStateException`
- `close()` → APPROVED → CLOSED

**Maker-Checker:** Same pattern as HR Payroll and GL Journal — `approve()` enforces four-eyes at the domain level.

---

### Domain Events — `com.atlashub.accounting.budget.domain.events`

| Event | Published When | Consumed By |
|---|---|---|
| `BudgetApprovedEvent` | Budget activated | `analytics` (load budget baseline for variance tracking) |

---

### Domain Exceptions — `com.atlashub.accounting.budget.domain.exceptions`

```java
public class BudgetNotFoundException extends NotFoundException {
    public BudgetNotFoundException(Long id) { super("Budget not found: " + id); }
}
public class BudgetAlreadyApprovedException extends ConflictException {
    public BudgetAlreadyApprovedException() { super("This budget has already been approved"); }
}
public class InvalidBudgetStateException extends BusinessRuleException {
    public InvalidBudgetStateException(String message) { super(message); }
}
public class SelfApprovalNotAllowedException extends AuthorizationException {
    public SelfApprovalNotAllowedException() {
        super("The approver cannot be the same person who created the budget");
    }
}
```

---

## Application Layer

### Commands — `com.atlashub.accounting.budget.application.commands`

#### `CreateBudgetCommand`
```java
record CreateBudgetCommand(Long organizationId, Long accountId, String name,
                           String period, Money budgetedAmount, Long initiatedBy)
```
**Handler:** `CreateBudgetHandler` | **Response:** `Long budgetId`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:budgets:manage')")`  
**Flow:** Create `Budget` (status = DRAFT) → `repository.save()`

---

#### `ApproveBudgetCommand`
```java
record ApproveBudgetCommand(Long budgetId, Long approverId)
```
**Handler:** `ApproveBudgetHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:budgets:approve')")`  
**Maker-Checker:** `budget.approve(approverId)` validates `approverId ≠ initiatedBy`  
**Flow:** Load `Budget` → `budget.approve(approverId)` → `repository.save()` → `BudgetApprovedEvent` published

---

#### `CloseBudgetCommand`
```java
record CloseBudgetCommand(Long budgetId)
```
**Handler:** `CloseBudgetHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:budgets:manage')")`

---

### Queries — `com.atlashub.accounting.budget.application.queries`

#### `ListBudgetsQuery`
```java
record ListBudgetsQuery(Long organizationId, BudgetStatus status, String period)
```
**Handler:** `ListBudgetsHandler` | **Result:** `List<BudgetResult>` (bounded per org)

---

#### `GetBudgetVarianceQuery`
```java
record GetBudgetVarianceQuery(Long budgetId)
```
**Handler:** `GetBudgetVarianceHandler` | **Result:** `BudgetVarianceResult`

`BudgetVarianceResult`: `budgetId`, `accountId`, `period`, `budgetedAmount`, `actualAmount`, `variance`, `variancePercentage`

**Flow:** Load `Budget` → query `JournalLine` totals for `accountId` + `period` date range → compute variance = `budgetedAmount - actualAmount`

**Justification for List not Page:** Budgets are bounded — an org defines one budget per account per period. List is appropriate.

---

## Infrastructure Layer

### Persistence

| JPA Entity | Table | Locking |
|---|---|---|
| `BudgetJpaEntity` | `accounting_budgets` | `@Version` optimistic |

**Spring Data:**
```
BudgetJpaRepository
  + findByOrganizationIdAndStatus(Long orgId, BudgetStatus status): List<BudgetJpaEntity>
  + findByOrganizationIdAndAccountIdAndPeriod(Long orgId, Long accountId, String period): Optional<BudgetJpaEntity>
```

**Repository Adapter:** `BudgetRepositoryAdapter` → `accounting_budget_seq`

---

## Presentation Layer

### Controller: `AccountingBudgetController` — `/api/v1/accounting/budgets`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `POST` | `/budgets` | `accounting:budgets:manage` | `CreateBudgetRequest` | `Long` |
| `POST` | `/budgets/{id}/approve` | `accounting:budgets:approve` | — | `void` |
| `POST` | `/budgets/{id}/close` | `accounting:budgets:manage` | — | `void` |
| `GET` | `/budgets` | — | `?orgId&status&period` | `List<BudgetResult>` |
| `GET` | `/budgets/{id}/variance` | — | — | `BudgetVarianceResult` |

---

## Maker-Checker

| Operation | Maker Permission | Checker Permission | Domain Enforcement |
|---|---|---|---|
| Budget Approval | `accounting:budgets:manage` | `accounting:budgets:approve` | `Budget.approve()` throws `SelfApprovalNotAllowedException` |

---

## Complete File List

```
atlashub-accounting/src/main/java/com/atlashub/accounting/budget/
├── domain/
│   ├── entities/
│   │   └── Budget.java
│   ├── events/
│   │   └── BudgetApprovedEvent.java
│   ├── exceptions/
│   │   ├── BudgetNotFoundException.java
│   │   ├── BudgetAlreadyApprovedException.java
│   │   ├── InvalidBudgetStateException.java
│   │   └── SelfApprovalNotAllowedException.java
│   ├── repositories/
│   │   └── BudgetRepository.java
│   └── valueobject/
│       └── BudgetStatus.java
├── application/
│   ├── commands/
│   │   ├── CreateBudget/ [CreateBudgetCommand, CreateBudgetHandler]
│   │   ├── ApproveBudget/ [ApproveBudgetCommand, ApproveBudgetHandler]
│   │   └── CloseBudget/ [CloseBudgetCommand, CloseBudgetHandler]
│   └── queries/
│       ├── ListBudgets/ [ListBudgetsQuery, ListBudgetsHandler, BudgetResult]
│       └── GetBudgetVariance/ [GetBudgetVarianceQuery, GetBudgetVarianceHandler, BudgetVarianceResult]
├── infrastructure/
│   └── persistence/ [adapters, entities, mappers, repositories]
└── presentation/
    ├── dto/ [CreateBudgetRequest, BudgetResult, BudgetVarianceResult]
    └── rest/
        └── AccountingBudgetController.java
```
