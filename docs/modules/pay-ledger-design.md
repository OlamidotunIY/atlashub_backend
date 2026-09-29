# Pay Ledger Design (`atlashub-pay:ledger`)

## Role & Purpose

The `ledger` submodule is the **double-entry accounting engine** of AtlasHub Pay. Every movement of money within the platform — whether a card sale, a salary disbursement, a marketplace split, or an inter-outlet float transfer — produces a `LedgerTransaction` with balanced DEBIT and CREDIT entries. No other module moves money without posting a corresponding ledger entry.

The ledger is an **append-only, non-reversible record**. There are no updates or deletes. Corrections are made by posting reversal transactions.

This submodule also provides the real-time balance query API used by dashboards, the payroll module (to verify sufficient funds before approving a run), and the billing module.

> **The Shadow Ledger Principle**: AtlasHub's internal ledger is a shadow of real money held at Anchor/Paystack. Every real-money event (Anchor confirms a deposit, Paystack confirms a charge, a payout settles) produces a corresponding `LedgerTransaction`. This gives AtlasHub a real-time, independently auditable balance record per organization without relying solely on external provider APIs.

---

## Domain Layer

### Aggregate Root: `LedgerAccount`

**Package**: `com.atlashub.pay.ledger.domain.entities`

```
LedgerAccount
├── id: Long
├── organizationId: Long
├── accountType: LedgerAccountType     ← OPERATING, PAYROLL_RESERVE, TAX_HOLDING,
│                                         ESCROW, SUSPENSE, TILL, SPLIT_HOLDING
├── outletId: Long                     ← nullable; only for TILL accounts
├── currency: Currency
├── status: LedgerAccountStatus        ← ACTIVE, FROZEN, CLOSED
└── createdAt: ZonedDateTime
```

**State Machine:**
```
ACTIVE ──freeze()──► FROZEN ──unfreeze()──► ACTIVE
  │                                           │
  └────────────close()──────────────────────►CLOSED
```

**Business Methods:**

| Method | Guard | Effect | Event Registered |
|---|---|---|---|
| `freeze()` | status must be `ACTIVE` | sets `status = FROZEN` | `LedgerAccountFrozenEvent` |
| `unfreeze()` | status must be `FROZEN` | sets `status = ACTIVE` | `LedgerAccountUnfrozenEvent` |
| `close()` | status must be `ACTIVE` or `FROZEN`; balance must be zero | sets `status = CLOSED` | `LedgerAccountClosedEvent` |

**Exceptions:**

| Exception | When |
|---|---|
| `LedgerAccountNotFoundException` | Account lookup fails |
| `LedgerAccountFrozenException` | Posting attempted on a `FROZEN` account |
| `LedgerAccountClosedException` | Any mutation attempted on a `CLOSED` account |
| `LedgerAccountNotEmptyException` | `close()` attempted when balance is non-zero |

---

### Aggregate Root: `LedgerTransaction`

**Package**: `com.atlashub.pay.ledger.domain.entities`

```
LedgerTransaction
├── id: Long
├── organizationId: Long
├── entries: List<LedgerEntry>         ← at least 2: one DEBIT, one CREDIT
├── sourceSystem: SourceSystem
├── sourceReferenceId: String          ← ID of the originating business entity
├── description: String
├── currency: Currency
├── postedAt: ZonedDateTime
└── reference: String                  ← unique idempotency reference
```

**Construction Rule — `validateBalance()`:**
At construction, before any persistence occurs, `validateBalance()` is invoked. It sums all `DEBIT` entry amounts and all `CREDIT` entry amounts. If they are not equal, `UnbalancedLedgerTransactionException` is thrown and the transaction is rejected. The ledger will **never** contain an unbalanced transaction.

```java
private void validateBalance() {
    BigDecimal totalDebits = entries.stream()
        .filter(e -> e.type() == EntryType.DEBIT)
        .map(e -> e.amount().value())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal totalCredits = entries.stream()
        .filter(e -> e.type() == EntryType.CREDIT)
        .map(e -> e.amount().value())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    if (totalDebits.compareTo(totalCredits) != 0) {
        throw new UnbalancedLedgerTransactionException(
            "Ledger transaction debits (" + totalDebits + ") do not equal credits (" + totalCredits + ")");
    }
}
```

**Events Registered on Construction**: `LedgerTransactionPostedEvent`

---

### Entity: `LedgerEntry` (immutable, owned by `LedgerTransaction`)

```
LedgerEntry
├── id: Long
├── transactionId: Long
├── accountId: Long
├── type: EntryType              ← DEBIT | CREDIT
├── amount: Money
└── runningBalance: Money        ← balance of this account AFTER this entry is applied
```

`LedgerEntry` is immutable after creation. There is no `update()` method.

---

### Entity: `BalanceSnapshot`

Periodically computed balance per `LedgerAccount`. Balance queries use `latest snapshot + entries since snapshot` to avoid full table scans.

```
BalanceSnapshot
├── id: Long
├── accountId: Long
├── balance: Money
└── snapshotAt: ZonedDateTime
```

---

### Value Objects

**Package**: `com.atlashub.pay.ledger.domain.valueobject`

| Class | Values | Description |
|---|---|---|
| `LedgerAccountType` | `OPERATING`, `PAYROLL_RESERVE`, `TAX_HOLDING`, `ESCROW`, `SUSPENSE`, `TILL`, `SPLIT_HOLDING` | The functional purpose of each account |
| `LedgerAccountStatus` | `ACTIVE`, `FROZEN`, `CLOSED` | Lifecycle state |
| `EntryType` | `DEBIT`, `CREDIT` | Direction of ledger entry |
| `SourceSystem` | See table below | The originating business operation |
| `Money` | `value: BigDecimal`, `currency: String` | Immutable monetary amount |

**`SourceSystem` values:**

| Value | Originating Module | Description |
|---|---|---|
| `COMMERCE_CHECKOUT` | `commerce` | POS sale completed |
| `COMMERCE_REFUND` | `commerce` | Customer refund |
| `PLATFORM_BILLING` | `billing` | Organization pays AtlasHub invoice |
| `PAYROLL` | `hr` | Payroll disbursement |
| `LOAN_DISBURSEMENT` | `hr` | Employee loan approved |
| `INTER_OUTLET_TRANSFER` | `commerce` | Stock/cash movement between outlets |
| `CASH_BANKING` | `commerce` | Till cash deposited to bank |
| `EXTERNAL_COLLECTION` | `pay:accounts` | Bank transfer to NUBAN (Anchor) |
| `CARD_CHARGE` | `pay:charges` | Card/USSD/POS payment (Paystack/Moniepoint) |
| `PAYOUT` | `pay:transfers` | Outbound bank transfer |
| `SETTLEMENT` | `pay:settlement` | Paystack/Moniepoint settles to org bank |
| `SPLIT` | `pay:splits` | Revenue split distribution |
| `ESCROW_RELEASE` | `commerce` | Escrow released on delivery confirmation |
| `ESCROW_REFUND` | `commerce` | Escrow refunded on delivery failure |
| `MANUAL` | `accounting` | Admin-initiated manual entry |
| `SYSTEM` | Internal | Auto-generated (fee calculation, etc.) |

---

### Domain Events

**Package**: `com.atlashub.pay.ledger.domain.events`

All events published to Kafka topic **`pay-events`**.

| Event | Published When | Consumed By |
|---|---|---|
| `LedgerTransactionPostedEvent` | Any ledger transaction posted | `accounting` (bridge listener posts GL journal entry), `pay:tx-query` |
| `LedgerAccountFrozenEvent` | Account frozen | `notifications` |
| `LedgerAccountUnfrozenEvent` | Account unfrozen | `notifications` |
| `LedgerAccountClosedEvent` | Account closed | `notifications` |

**`LedgerTransactionPostedEvent` payload:**
```java
public record LedgerTransactionPostedEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) {
    public record Payload(
        Long transactionId,
        Long organizationId,
        String reference,
        String sourceSystem,
        String sourceReferenceId,
        String description,
        String currency,
        ZonedDateTime postedAt,
        List<EntryPayload> entries
    ) {}

    public record EntryPayload(
        Long accountId,
        String entryType,
        BigDecimal amount,
        BigDecimal runningBalance
    ) {}
}
```

---

### Domain Exceptions

**Package**: `com.atlashub.pay.ledger.domain.exceptions`

```java
public class LedgerAccountNotFoundException extends NotFoundException {
    public LedgerAccountNotFoundException() { super("Ledger account not found"); }
    public LedgerAccountNotFoundException(String message) { super(message); }
}

public class LedgerAccountFrozenException extends BusinessRuleException {
    public LedgerAccountFrozenException() { super("Ledger account is frozen and cannot accept transactions"); }
}

public class LedgerAccountClosedException extends BusinessRuleException {
    public LedgerAccountClosedException() { super("Ledger account is closed"); }
}

public class LedgerAccountNotEmptyException extends BusinessRuleException {
    public LedgerAccountNotEmptyException() { super("Ledger account cannot be closed while it has a non-zero balance"); }
}

public class UnbalancedLedgerTransactionException extends DomainException {
    public UnbalancedLedgerTransactionException(String message) { super(message); }
}

public class DuplicateLedgerReferenceException extends ConflictException {
    public DuplicateLedgerReferenceException(String reference) {
        super("A ledger transaction with reference '" + reference + "' already exists");
    }
}

public class CurrencyMismatchException extends DomainException {
    public CurrencyMismatchException() { super("All entries in a ledger transaction must use the same currency"); }
}

public class InsufficientFundsException extends BusinessRuleException {
    public InsufficientFundsException() { super("Insufficient funds in ledger account"); }
    public InsufficientFundsException(String message) { super(message); }
}
```

---

### Domain Repositories

**Package**: `com.atlashub.pay.ledger.domain.repositories`

```java
public interface LedgerAccountRepository {
    LedgerAccount save(LedgerAccount account);
    Optional<LedgerAccount> findById(Long id);
    // Pessimistic lock — used during PostLedgerTransaction
    Optional<LedgerAccount> findByIdWithLock(Long id);
    List<LedgerAccount> findAllByOrganizationId(Long organizationId);
    Optional<LedgerAccount> findByOrganizationIdAndAccountType(Long organizationId, LedgerAccountType type);
    Optional<LedgerAccount> findByOrganizationIdAndOutletId(Long organizationId, Long outletId);
    List<LedgerAccount> findAllByIdInWithLock(List<Long> ids);  // ascending ID order for deadlock prevention
}

public interface LedgerTransactionRepository {
    LedgerTransaction save(LedgerTransaction transaction);
    Optional<LedgerTransaction> findById(Long id);
    Optional<LedgerTransaction> findByReference(String reference);
    List<LedgerTransaction> findByOrganizationId(Long organizationId, Pageable pageable);
}

public interface BalanceSnapshotRepository {
    BalanceSnapshot save(BalanceSnapshot snapshot);
    Optional<BalanceSnapshot> findLatestByAccountId(Long accountId);
}
```

---

## Application Layer

### Commands

#### `PostLedgerTransactionCommand`

**Package**: `com.atlashub.pay.ledger.application.commands.PostLedgerTransaction`

```java
public record PostLedgerTransactionCommand(
    Long organizationId,
    String reference,                         // idempotency key — caller-supplied
    String sourceSystem,
    String sourceReferenceId,
    String description,
    String currency,
    List<LedgerEntryRequest> entries
) {
    public record LedgerEntryRequest(
        Long accountId,
        String entryType,                     // "DEBIT" or "CREDIT"
        BigDecimal amount
    ) {}
}
```

**Handler**: `PostLedgerTransactionHandler extends Command<PostLedgerTransactionCommand, PostLedgerTransactionResponse>`

**RBAC**: Internal system command — called by other handlers within the platform, not directly from the API.

**Processing steps (CRITICAL — locking order must be preserved):**
1. Check idempotency: if a `LedgerTransaction` with `reference` already exists, return its result without re-posting.
2. Validate the caller-provided `entries` list: at least one DEBIT and one CREDIT must be present.
3. **Collect all unique `accountId`s from `entries`. Sort them in ascending order. Acquire `PESSIMISTIC_WRITE` locks on each `LedgerAccount` in that sorted order.** This prevents deadlocks when concurrent transactions affect overlapping account sets.
4. Verify each locked account belongs to `organizationId` and is `ACTIVE` (not FROZEN or CLOSED). Throw `LedgerAccountFrozenException` or `LedgerAccountClosedException` if violated.
5. Compute `runningBalance` for each entry using the account's current balance.
6. Construct `LedgerTransaction` — `validateBalance()` is called in the constructor. If unbalanced, `UnbalancedLedgerTransactionException` is thrown and no persistence occurs.
7. Save `LedgerTransaction` and all `LedgerEntry` records.
8. Update each account's balance (stored on the `LedgerAccount` entity for fast queries).
9. Publish `LedgerTransactionPostedEvent` via outbox.

**Response**: `PostLedgerTransactionResponse`
```java
public record PostLedgerTransactionResponse(
    Long transactionId,
    String reference,
    ZonedDateTime postedAt
) {}
```

---

#### `CreateLedgerAccountCommand`

**Package**: `com.atlashub.pay.ledger.application.commands.CreateLedgerAccount`

```java
public record CreateLedgerAccountCommand(
    Long organizationId,
    String accountType,
    Long outletId,       // nullable
    String currency
) {}
```

**Handler**: `CreateLedgerAccountHandler extends Command<CreateLedgerAccountCommand, CreateLedgerAccountResponse>`

**RBAC**: Internal system command (triggered on org compliance approval and on outlet creation). Not exposed directly as an API endpoint.

**Processing steps:**
1. Verify no account of this `accountType` (and `outletId` for TILL) already exists for the org.
2. Construct `LedgerAccount` with `status = ACTIVE`.
3. Save and return response.

**Response**: `CreateLedgerAccountResponse`
```java
public record CreateLedgerAccountResponse(Long ledgerAccountId) {}
```

---

#### `FreezeAccountCommand`

**Package**: `com.atlashub.pay.ledger.application.commands.FreezeAccount`

```java
public record FreezeAccountCommand(
    Long ledgerAccountId,
    Long requestedByUserId
) {}
```

**Handler**: `FreezeAccountHandler extends Command<FreezeAccountCommand, Void>`

**RBAC**: `pay:ledger:freeze` (platform admin only)

**Processing steps:**
1. Load `LedgerAccount` by ID. Throw `LedgerAccountNotFoundException` if absent.
2. Call `account.freeze()`.
3. Save and publish events.

**Response**: `void`

---

#### `CloseAccountCommand`

**Package**: `com.atlashub.pay.ledger.application.commands.CloseAccount`

```java
public record CloseAccountCommand(
    Long ledgerAccountId,
    Long requestedByUserId
) {}
```

**Handler**: `CloseAccountHandler extends Command<CloseAccountCommand, Void>`

**RBAC**: `pay:ledger:close` (platform admin only)

**Processing steps:**
1. Load `LedgerAccount`. Throw `LedgerAccountNotFoundException` if absent.
2. Query current balance. If non-zero, throw `LedgerAccountNotEmptyException`.
3. Call `account.close()`.
4. Save and publish events.

**Response**: `void`

---

### Queries

#### `GetAccountBalanceQuery`

**Package**: `com.atlashub.pay.ledger.application.queries.GetAccountBalance`

```java
public record GetAccountBalanceQuery(
    Long organizationId,
    Long accountId
) {}
```

**Handler**: `GetAccountBalanceHandler extends Query<GetAccountBalanceQuery, AccountBalanceResult>`

**Strategy**: Retrieve latest `BalanceSnapshot`. Sum all `LedgerEntry` amounts posted after the snapshot. Return computed balance.

**Result**:
```java
public record AccountBalanceResult(
    Long accountId,
    String accountType,
    BigDecimal balance,
    String currency,
    ZonedDateTime asOf
) {}
```

---

#### `GetWalletBalancesQuery`

**Package**: `com.atlashub.pay.ledger.application.queries.GetWalletBalances`

```java
public record GetWalletBalancesQuery(Long organizationId) {}
```

**Handler**: `GetWalletBalancesHandler extends Query<GetWalletBalancesQuery, WalletBalancesResult>`

**Returns**: All ledger accounts for the org and their current balances — used by the dashboard wallet overview.

**Result**:
```java
public record WalletBalancesResult(
    Long organizationId,
    Map<String, BigDecimal> balancesByAccountType,
    String currency
) {}
```

**Returns**: Single result (not pageable — bounded set of accounts per org).

---

#### `GetLedgerHistoryQuery`

**Package**: `com.atlashub.pay.ledger.application.queries.GetLedgerHistory`

```java
public record GetLedgerHistoryQuery(
    Long organizationId,
    Long accountId,       // nullable — all accounts if absent
    LocalDate dateFrom,
    LocalDate dateTo,
    int page,
    int size
) {}
```

**Handler**: `GetLedgerHistoryHandler extends Query<GetLedgerHistoryQuery, PageResult<LedgerTransactionResult>>`

**Returns**: `PageResult<LedgerTransactionResult>` — pageable because ledger history can be very large.

**Result**:
```java
public record LedgerTransactionResult(
    Long transactionId,
    String reference,
    String sourceSystem,
    String sourceReferenceId,
    String description,
    String currency,
    ZonedDateTime postedAt,
    List<LedgerEntryResult> entries
) {}
```

---

## Infrastructure Layer

### Persistence

**JPA Entity — `LedgerAccountJpa`:**
```java
@Entity
@Table(name = "ledger_accounts")
public class LedgerAccountJpa {
    @Id @GeneratedValue Long id;
    Long organizationId;
    @Enumerated(EnumType.STRING) LedgerAccountType accountType;
    Long outletId;
    String currency;
    @Enumerated(EnumType.STRING) LedgerAccountStatus status;
    BigDecimal currentBalance;   // denormalized for fast balance reads
    ZonedDateTime createdAt;
}
```

**JPA Entity — `LedgerTransactionJpa`:**
```java
@Entity
@Table(name = "ledger_transactions")
public class LedgerTransactionJpa {
    @Id @GeneratedValue Long id;
    Long organizationId;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<LedgerEntryJpa> entries;
    @Enumerated(EnumType.STRING) SourceSystem sourceSystem;
    String sourceReferenceId;
    String description;
    String currency;
    ZonedDateTime postedAt;
    @Column(unique = true) String reference;
}
```

**JPA Entity — `LedgerEntryJpa`:**
```java
@Entity
@Table(name = "ledger_entries")
public class LedgerEntryJpa {
    @Id @GeneratedValue Long id;
    Long transactionId;
    Long accountId;
    @Enumerated(EnumType.STRING) EntryType type;
    BigDecimal amount;
    String currency;
    BigDecimal runningBalance;
}
```

**Spring Data Repositories:**
- `SpringDataLedgerAccountRepository extends JpaRepository<LedgerAccountJpa, Long>`
- `SpringDataLedgerTransactionRepository extends JpaRepository<LedgerTransactionJpa, Long>`
- `SpringDataBalanceSnapshotRepository extends JpaRepository<BalanceSnapshotJpa, Long>`

**Adapters:**
- `LedgerAccountRepositoryAdapter implements LedgerAccountRepository`
- `LedgerTransactionRepositoryAdapter implements LedgerTransactionRepository`
- `BalanceSnapshotRepositoryAdapter implements BalanceSnapshotRepository`

**Mappers:**
- `LedgerAccountMapper`
- `LedgerTransactionMapper`

---

## Presentation Layer

### Controller: `LedgerController`

**Package**: `com.atlashub.pay.ledger.presentation.rest`

| Method | Path | Auth | RBAC | Request | Response |
|---|---|---|---|---|---|
| `GET` | `/api/v1/pay/ledger/balance` | Bearer JWT | `pay:ledger:read` | Query: `accountType` | `AccountBalanceResponse` |
| `GET` | `/api/v1/pay/ledger/balances` | Bearer JWT | `pay:ledger:read` | — | `WalletBalancesResponse` |
| `GET` | `/api/v1/pay/ledger/history` | Bearer JWT | `pay:ledger:read` | Query: `accountId`, `dateFrom`, `dateTo`, `page`, `size` | `PageResult<LedgerTransactionResponse>` |
| `POST` | `/api/v1/pay/ledger/freeze/{accountId}` | Bearer JWT | `pay:ledger:freeze` | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/pay/ledger/close/{accountId}` | Bearer JWT | `pay:ledger:close` | — | `ApiResponse<Void>` |

### DTOs

**Package**: `com.atlashub.pay.ledger.presentation.dto`

**`AccountBalanceResponse`**
```java
public record AccountBalanceResponse(
    Long accountId,
    String accountType,
    BigDecimal balance,
    String currency,
    ZonedDateTime asOf
) {}
```

**`WalletBalancesResponse`**
```java
public record WalletBalancesResponse(
    Long organizationId,
    Map<String, BigDecimal> balancesByAccountType,
    String currency
) {}
```

**`LedgerTransactionResponse`**
```java
public record LedgerTransactionResponse(
    Long transactionId,
    String reference,
    String sourceSystem,
    String description,
    String currency,
    ZonedDateTime postedAt,
    List<LedgerEntryResponse> entries
) {}
```

---

## RBAC Table

| Permission | Granted To | Operation |
|---|---|---|
| `pay:ledger:read` | `OWNER`, `ADMIN`, `FINANCE`, `ACCOUNTANT` | View balances and ledger history |
| `pay:ledger:freeze` | Platform admin only | Freeze a ledger account |
| `pay:ledger:close` | Platform admin only | Close a ledger account |

---

## Maker-Checker

Not applicable at the ledger level. `PostLedgerTransaction` is always system-called from handlers that have already had maker-checker applied (e.g., payroll approval, payout approval). The ledger does not add a second approval layer.

---

## Socket Events

No WebSocket push from this submodule. `LedgerTransactionPostedEvent` is a background accounting event — no human is waiting for it in a browser. Balance updates are available on the next dashboard query.

---

## Domain Events Table

| Event | Kafka Topic | Published When | Consumed By |
|---|---|---|---|
| `LedgerTransactionPostedEvent` | `pay-events` | Any ledger transaction posted | `accounting` (GL bridge), `pay:tx-query` |
| `LedgerAccountFrozenEvent` | `pay-events` | Account frozen by admin | `notifications` |
| `LedgerAccountUnfrozenEvent` | `pay-events` | Account unfrozen | `notifications` |
| `LedgerAccountClosedEvent` | `pay-events` | Account permanently closed | `notifications` |

---

## Distributed Architecture

### Locking Strategy

> [!IMPORTANT]
> `PostLedgerTransactionHandler` **MUST** acquire pessimistic write locks on all affected `LedgerAccount` records in **ascending `accountId` order** before posting any entries. This is non-negotiable. Failing to respect the ordering causes deadlocks when two concurrent transactions affect overlapping account sets.

- **Pessimistic Write Lock** (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) on `LedgerAccount` during `PostLedgerTransaction`.
- **Optimistic Lock** (`@Version`) on `LedgerAccount` for all other mutations (freeze, close, unfreeze).

### Idempotency
- `PostLedgerTransactionHandler` checks for an existing `LedgerTransaction` by `reference` before posting. Duplicate `reference` → return existing result, no re-post.
- `Idempotency-Key` header on the API is mapped to `reference` for callers that supply it.

### Outbox
- `LedgerTransactionPostedEvent` is written to the outbox table **in the same DB transaction** as the ledger post. If the transaction rolls back, the event is never published.

---

## Complete File List

```
com.atlashub.pay.ledger/
├── application/
│   ├── commands/
│   │   ├── CloseAccount/
│   │   │   ├── CloseAccountCommand.java
│   │   │   └── CloseAccountHandler.java
│   │   ├── CreateLedgerAccount/
│   │   │   ├── CreateLedgerAccountCommand.java
│   │   │   ├── CreateLedgerAccountHandler.java
│   │   │   └── CreateLedgerAccountResponse.java
│   │   ├── FreezeAccount/
│   │   │   ├── FreezeAccountCommand.java
│   │   │   └── FreezeAccountHandler.java
│   │   └── PostLedgerTransaction/
│   │       ├── PostLedgerTransactionCommand.java
│   │       ├── PostLedgerTransactionHandler.java
│   │       └── PostLedgerTransactionResponse.java
│   └── queries/
│       ├── GetAccountBalance/
│       │   ├── GetAccountBalanceQuery.java
│       │   ├── GetAccountBalanceHandler.java
│       │   └── AccountBalanceResult.java
│       ├── GetLedgerHistory/
│       │   ├── GetLedgerHistoryQuery.java
│       │   ├── GetLedgerHistoryHandler.java
│       │   └── LedgerTransactionResult.java
│       └── GetWalletBalances/
│           ├── GetWalletBalancesQuery.java
│           ├── GetWalletBalancesHandler.java
│           └── WalletBalancesResult.java
├── domain/
│   ├── entities/
│   │   ├── BalanceSnapshot.java
│   │   ├── LedgerAccount.java
│   │   ├── LedgerEntry.java
│   │   └── LedgerTransaction.java
│   ├── events/
│   │   ├── LedgerAccountClosedEvent.java
│   │   ├── LedgerAccountFrozenEvent.java
│   │   ├── LedgerAccountUnfrozenEvent.java
│   │   └── LedgerTransactionPostedEvent.java
│   ├── exceptions/
│   │   ├── CurrencyMismatchException.java
│   │   ├── DuplicateLedgerReferenceException.java
│   │   ├── InsufficientFundsException.java
│   │   ├── LedgerAccountClosedException.java
│   │   ├── LedgerAccountFrozenException.java
│   │   ├── LedgerAccountNotEmptyException.java
│   │   ├── LedgerAccountNotFoundException.java
│   │   └── UnbalancedLedgerTransactionException.java
│   ├── repositories/
│   │   ├── BalanceSnapshotRepository.java
│   │   ├── LedgerAccountRepository.java
│   │   └── LedgerTransactionRepository.java
│   └── valueobject/
│       ├── EntryType.java
│       ├── LedgerAccountStatus.java
│       ├── LedgerAccountType.java
│       ├── Money.java
│       └── SourceSystem.java
├── infrastructure/
│   ├── persistence/
│   │   ├── adapters/
│   │   │   ├── BalanceSnapshotRepositoryAdapter.java
│   │   │   ├── LedgerAccountRepositoryAdapter.java
│   │   │   └── LedgerTransactionRepositoryAdapter.java
│   │   ├── entities/
│   │   │   ├── BalanceSnapshotJpa.java
│   │   │   ├── LedgerAccountJpa.java
│   │   │   ├── LedgerEntryJpa.java
│   │   │   └── LedgerTransactionJpa.java
│   │   ├── mappers/
│   │   │   ├── LedgerAccountMapper.java
│   │   │   └── LedgerTransactionMapper.java
│   │   └── repositories/
│   │       ├── SpringDataBalanceSnapshotRepository.java
│   │       ├── SpringDataLedgerAccountRepository.java
│   │       └── SpringDataLedgerTransactionRepository.java
└── presentation/
    ├── dto/
    │   ├── AccountBalanceResponse.java
    │   ├── LedgerTransactionResponse.java
    │   └── WalletBalancesResponse.java
    └── rest/
        └── LedgerController.java
```
