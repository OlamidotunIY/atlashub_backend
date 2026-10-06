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
├── environment: ApiEnvironment       ← TEST or LIVE; accounts never cross environments
├── accountType: LedgerAccountType     ← OPERATING, PAYROLL_RESERVE, TAX_HOLDING,
│                                         ESCROW, SUSPENSE, TILL, SPLIT_HOLDING,
│                                         PROVIDER_CLEARING,
│                                         CUSTOMER_FUNDS, VENDOR_PAYABLE
├── outletId: Long                     ← nullable; only for TILL accounts
├── partyType: LedgerPartyType         ← nullable; CUSTOMER or VENDOR for party accounts
├── partyReferenceId: String           ← nullable; AtlasHub customer/vendor ID
├── currency: Currency
├── status: LedgerAccountStatus        ← ACTIVE, FROZEN, CLOSED
├── activeRestrictions: Set<LedgerRestrictionType>
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
| `freeze(restrictionType)` | account must not be `CLOSED` | adds the restriction and sets `status = FROZEN` | `LedgerAccountFrozenEvent` |
| `unfreeze(restrictionType)` | matching restriction must exist | removes only that restriction; sets `ACTIVE` only when none remain | `LedgerAccountUnfrozenEvent` when usable again |
| `close()` | status must be `ACTIVE` or `FROZEN`; balance must be zero | sets `status = CLOSED` | `LedgerAccountClosedEvent` |

**Exceptions:**

| Exception | When |
|---|---|
| `LedgerAccountNotFoundException` | Account lookup fails |
| `LedgerAccountFrozenException` | Posting attempted on a `FROZEN` account |
| `LedgerAccountClosedException` | Any mutation attempted on a `CLOSED` account |
| `LedgerAccountNotEmptyException` | `close()` attempted when balance is non-zero |

**Account ownership rules:**

- Organization-level accounts have no `outletId`, `partyType`, or `partyReferenceId`.
- `TILL` accounts require `outletId` and may not have party fields.
- `CUSTOMER_FUNDS` requires `partyType=CUSTOMER` and a customer reference.
- `VENDOR_PAYABLE` requires `partyType=VENDOR` and a vendor reference.
- Party accounts are AtlasHub ledger records, not Anchor subaccounts or reserved accounts.
- One active party account is allowed per `(organizationId, accountType, partyReferenceId, currency)`.
- Restriction sources such as `ORGANIZATION_BAN`, `COMPLIANCE`, `MANUAL`, and `RISK` are independent. Removing one source never clears another source's freeze.

---

### Aggregate Root: `LedgerTransaction`

**Package**: `com.atlashub.pay.ledger.domain.entities`

```
LedgerTransaction
├── id: Long
├── organizationId: Long
├── environment: ApiEnvironment          TEST | LIVE
├── entries: List<LedgerEntry>         ← at least 2: one DEBIT, one CREDIT
├── sourceSystem: SourceSystem
├── sourceReferenceId: String          ← ID of the originating business entity
├── description: String
├── currency: Currency
├── postedAt: ZonedDateTime
└── reference: String                  ← unique with environment
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
| `LedgerAccountType` | `OPERATING`, `PAYROLL_RESERVE`, `TAX_HOLDING`, `ESCROW`, `SUSPENSE`, `PROVIDER_CLEARING`, `TILL`, `SPLIT_HOLDING`, `CUSTOMER_FUNDS`, `VENDOR_PAYABLE` | The functional purpose of each account |
| `LedgerAccountStatus` | `ACTIVE`, `FROZEN`, `CLOSED` | Lifecycle state |
| `EntryType` | `DEBIT`, `CREDIT` | Direction of ledger entry |
| `LedgerPartyType` | `CUSTOMER`, `VENDOR` | Party owning/benefiting from a dynamic party ledger account |
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
    Optional<LedgerAccount> findByOrganizationIdAndParty(
        Long organizationId,
        LedgerAccountType type,
        LedgerPartyType partyType,
        String partyReferenceId,
        String currency);
    List<LedgerAccount> findAllByIdInWithLock(List<Long> ids);  // ascending ID order for deadlock prevention
}

public interface LedgerTransactionRepository {
    LedgerTransaction save(LedgerTransaction transaction);
    Optional<LedgerTransaction> findById(Long id);
    Optional<LedgerTransaction> findByReferenceAndEnvironment(String reference, ApiEnvironment environment);
    List<LedgerTransaction> findByOrganizationIdAndEnvironment(Long organizationId, ApiEnvironment environment, Pageable pageable);
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
    String environment,
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
1. Check idempotency: if a `LedgerTransaction` with `(environment, reference)` already exists, return its result without re-posting.
2. Validate the caller-provided `entries` list: at least one DEBIT and one CREDIT must be present.
3. **Collect all unique `accountId`s from `entries`. Sort them in ascending order. Acquire `PESSIMISTIC_WRITE` locks on each `LedgerAccount` in that sorted order.** This prevents deadlocks when concurrent transactions affect overlapping account sets.
4. Verify each locked account belongs to `organizationId` and is `ACTIVE` (not FROZEN or CLOSED). Throw `LedgerAccountFrozenException` or `LedgerAccountClosedException` if violated.
5. Do not persist or mutate a current-balance column. Balances remain derived from the latest snapshot plus later entries.
6. Construct `LedgerTransaction` — `validateBalance()` is called in the constructor. If unbalanced, `UnbalancedLedgerTransactionException` is thrown and no persistence occurs.
7. Save `LedgerTransaction` and all `LedgerEntry` records.
8. Publish `LedgerTransactionPostedEvent` via outbox. No mutable current-balance column is stored.

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
    String environment,
    String accountType,
    Long outletId,       // nullable
    String partyType,    // nullable; CUSTOMER or VENDOR
    String partyReferenceId, // nullable
    String currency
) {}
```

**Handler**: `CreateLedgerAccountHandler extends Command<CreateLedgerAccountCommand, CreateLedgerAccountResponse>`

**RBAC**: Internal system command (triggered on organization banking activation, outlet creation, and reserved-account activation). Not exposed directly as an API endpoint.

**Processing steps:**
1. Verify no account of this `accountType` and scope already exists: organization-level, `outletId` for TILL, or party identity for customer/vendor accounts.
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
    Long requestedByUserId,
    String restrictionType
) {}
```

**Handler**: `FreezeAccountHandler extends Command<FreezeAccountCommand, Void>`

**Authorization**: Platform payment-operations authority. This is not an organization IAM permission.

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

**Authorization**: Platform payment-operations authority. This is not an organization IAM permission.

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

**Returns**: Organization-level ledger accounts and their current balances — used by the dashboard wallet overview. Party accounts are excluded from this map and queried through dedicated customer/vendor balance queries or summarized separately.

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

#### Party Balance Queries

- `GetPartyBalanceQuery(organizationId, partyType, partyReferenceId, currency)` returns the balance of the matching `CUSTOMER_FUNDS` or `VENDOR_PAYABLE` account.
- `GetPartyLedgerHistoryQuery(organizationId, partyType, partyReferenceId, dateFrom, dateTo, page, size)` returns pageable entries for that party account.
- Both handlers verify the party account belongs to the authenticated organization. Organization wallet queries do not expose individual party balances.

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
    ApiEnvironment environment;
    @Enumerated(EnumType.STRING) LedgerAccountType accountType;
    Long outletId;
    @Enumerated(EnumType.STRING) LedgerPartyType partyType;
    String partyReferenceId;
    String currency;
    @Enumerated(EnumType.STRING) LedgerAccountStatus status;
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
    ApiEnvironment environment;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    List<LedgerEntryJpa> entries;
    @Enumerated(EnumType.STRING) SourceSystem sourceSystem;
    String sourceReferenceId;
    String description;
    String currency;
    ZonedDateTime postedAt;
    String reference; // composite unique index with api_environment
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

### Kafka Listeners — `infrastructure/messaging/listeners/`

The ledger is the **single source of truth** for all money movements on the platform. Every monetary event from any other module triggers a listener here that posts the corresponding `LedgerTransaction`. No module may move money without the ledger knowing.

> Idempotency: Every listener checks `reference` uniqueness before posting. A duplicate event replay returns the existing `LedgerTransactionPostedEvent` without re-posting.

#### `OrganizationBankingActivatedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-org-banking-activated` |
| **Event** | `OrganizationBankingActivatedEvent` |
| **Payload fields** | `organizationId`, `bankingProfileId`, `businessDepositAccountId`, `businessSubAccountId`, `currency`, `activatedAt` |
| **Command called** | `CreateLedgerAccountHandler` (multiple times) |
| **Flow** | For each account type in `[OPERATING, PAYROLL_RESERVE, TAX_HOLDING, ESCROW, SUSPENSE, PROVIDER_CLEARING, SPLIT_HOLDING]`, create the organization-level account if absent. Idempotent. Reserved-account activation never repeats this bootstrap. |

#### `ReservedAccountActivatedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-reserved-account-activated` |
| **Event** | `ReservedAccountActivatedEvent` |
| **Payload fields** | `reservedAccountId`, `organizationId`, `ownerType`, `ownerReferenceId`, `currency` |
| **Command called** | `CreateLedgerAccountHandler` |
| **Flow** | Idempotently creates `CUSTOMER_FUNDS` for a customer owner or `VENDOR_PAYABLE` for a vendor owner. These are internal party ledger accounts, not Anchor subaccounts. |

#### `OutletCreatedListener`
| Attribute | Value |
|---|---|
| **Topic** | `accounts-events` |
| **Group ID** | `pay-ledger-outlet-created` |
| **Event** | `OutletCreatedEvent` |
| **Payload fields** | `outletId`, `organizationId`, `outletName`, `currency` |
| **Command called** | `CreateLedgerAccountHandler` |
| **Flow** | Creates one `TILL` ledger account for the new outlet. `outletId` is stored on the `LedgerAccount` for TILL-type accounts. |

#### `ChargeSuccessfulListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-charge-successful` |
| **Event** | `ChargeSuccessfulEvent` |
| **Payload fields** | `chargeId`, `organizationId`, `chargeReference`, `gatewayReference`, `amount`, `currency`, `channel`, `sourceSystem`, `sourceReferenceId`, `customerId`, `succeededAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts `Dr PROVIDER_CLEARING → Cr OPERATING`. This recognizes the successful collection while retaining the unsettled provider receivable. Reference = `chargeReference`; SourceSystem = `CARD_CHARGE`. It does not claim the provider has settled cash. |

#### `ProviderSettlementReceivedListener`

| Field | Value |
|---|---|
| **Topic** | `pay-events` |
| **Event** | `ProviderSettlementReceivedEvent` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts `Dr SUSPENSE → Cr PROVIDER_CLEARING` for the reconciled settlement amount. Operating/revenue is not credited again. Reference = provider settlement reference; SourceSystem = `SETTLEMENT`. |

#### `ReservedAccountFundedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-reserved-account-funded` |
| **Event** | `ReservedAccountFundedEvent` |
| **Payload fields** | `reservedAccountId`, `organizationId`, `ownerType`, `ownerReferenceId`, `businessSubAccountId`, `anchorTransferReference`, `amount`, `currency`, `senderAccountName`, `senderBankCode`, `receivedAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts `Dr SUSPENSE → Cr CUSTOMER_FUNDS` for customer accounts or `Dr SUSPENSE → Cr VENDOR_PAYABLE` for vendor accounts. Reference = `anchorTransferReference`; SourceSystem = `EXTERNAL_COLLECTION`. If an order/invoice already determines the economic classification, a separate idempotent business transaction reclassifies the party/suspense balance. Receipt alone is not treated as revenue. |

#### `OrganizationAccountFundedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-organization-account-funded` |
| **Event** | `OrganizationAccountFundedEvent` |
| **Payload fields** | `organizationId`, `businessAccountId`, `anchorTransferReference`, `amount`, `currency`, `receivedAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts `Dr SUSPENSE → Cr OPERATING` only for a confirmed organization-owned receipt. Reference = `anchorTransferReference`. |

#### `PayoutCompletedListener`
| Attribute | Value |
|---|---|
| **Topic** | `pay-events` |
| **Group ID** | `pay-ledger-payout-completed` |
| **Event** | `PayoutCompletedEvent` |
| **Payload fields** | `payoutId`, `organizationId`, `payoutReference`, `amount`, `currency`, `sourceSystem`, `sourceReferenceId`, `recipientName`, `recipientAccountNumber`, `completedAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts: `Dr Operating Account (or Payroll Reserve for PAYROLL payouts) → Cr Suspense Account`. Reference = `payoutReference`. SourceSystem = event's `sourceSystem`. |

#### `TillOpenedListener`
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `pay-ledger-till-opened` |
| **Event** | `TillOpenedEvent` |
| **Payload fields** | `tillSessionId`, `organizationId`, `outletId`, `cashierId`, `openingFloat`, `currency`, `openedAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts: `Dr TILL Account (outletId) → Cr Operating Account` for the opening float amount. Reference = `tillSessionId + "-open"`. SourceSystem = `INTER_OUTLET_TRANSFER`. |

#### `TillClosedListener`
| Attribute | Value |
|---|---|
| **Topic** | `commerce-events` |
| **Group ID** | `pay-ledger-till-closed` |
| **Event** | `TillClosedEvent` |
| **Payload fields** | `tillSessionId`, `organizationId`, `outletId`, `cashierId`, `closingCash`, `totalSales`, `currency`, `closedAt` |
| **Command called** | `PostLedgerTransactionHandler` |
| **Flow** | Posts: `Dr Operating Account → Cr TILL Account` to sweep TILL balance back to operating. Reference = `tillSessionId + "-close"`. SourceSystem = `CASH_BANKING`. |

#### `OrganizationBannedListener`
| Attribute | Value |
|---|---|
| **Topic** | `admin-events` |
| **Group ID** | `pay-ledger-org-banned` |
| **Event** | `OrganizationBannedEvent` |
| **Payload fields** | `organizationId`, `bannedByStaffId`, `reason`, `bannedAt` |
| **Command called** | `FreezeAccountHandler` (called in a loop for all org's accounts) |
| **Flow** | Adds the `ORGANIZATION_BAN` restriction to every non-closed ledger account. A banned org cannot move money. Existing compliance/manual/risk restrictions are preserved. |

#### `OrganizationComplianceSuspendedListener`
| Attribute | Value |
|---|---|
| **Topic** | `compliance-events` |
| **Group ID** | `pay-ledger-compliance-suspended` |
| **Event** | `OrganizationComplianceSuspendedEvent` |
| **Flow** | Adds the `COMPLIANCE` restriction to every non-closed ledger account. |

#### `OrganizationComplianceReinstatedListener`
| Attribute | Value |
|---|---|
| **Topic** | `compliance-events` |
| **Group ID** | `pay-ledger-compliance-reinstated` |
| **Event** | `OrganizationComplianceReinstatedEvent` |
| **Flow** | Removes only the `COMPLIANCE` restriction. Accounts with other restrictions remain frozen. |

#### `OrganizationUnbannedListener`
| Attribute | Value |
|---|---|
| **Topic** | `admin-events` |
| **Group ID** | `pay-ledger-org-unbanned` |
| **Event** | `OrganizationUnbannedEvent` |
| **Payload fields** | `organizationId`, `unbannedByStaffId`, `unbannedAt` |
| **Command called** | `UnfreezeAccountHandler` (called in a loop) |
| **Flow** | Removes only the `ORGANIZATION_BAN` restriction. An account returns to `ACTIVE` only when no compliance/manual/risk restriction remains. |

---

## Presentation Layer

### Controller: `LedgerController`

**Package**: `com.atlashub.pay.ledger.presentation.rest`

| Method | Path | Auth | RBAC | Request | Response |
|---|---|---|---|---|---|
| `GET` | `/api/v1/ledger-accounts/balance` | Bearer JWT | `pay:ledger:read` | Query: `accountType` | `AccountBalanceResponse` |
| `GET` | `/api/v1/ledger-accounts/balances` | Bearer JWT | `pay:ledger:read` | — | `WalletBalancesResponse` |
| `GET` | `/api/v1/ledger-accounts/party-balance` | Bearer JWT | `pay:ledger:read` | Query: `partyType`, `partyReferenceId`, `currency` | `AccountBalanceResponse` |
| `GET` | `/api/v1/ledger-entries` | Bearer JWT | `pay:ledger:read` | Query: `accountId`, `dateFrom`, `dateTo`, `page`, `size`; or required `partyType`, `partyReferenceId` for party history | `PageResult<LedgerTransactionResponse>` |
| `POST` | `/api/v1/admin/ledger-accounts/{accountId}/freeze` | Platform staff auth | platform payment-operations authority | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/admin/ledger-accounts/{accountId}/unfreeze` | Platform staff auth | platform payment-operations authority | — | `ApiResponse<Void>` |
| `POST` | `/api/v1/admin/ledger-accounts/{accountId}/close` | Platform staff auth | platform payment-operations authority | — | `ApiResponse<Void>` |

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
| Platform payment-operations authority | Platform staff only | Freeze, unfreeze, or close a ledger account through admin endpoints |

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
- `PostLedgerTransactionHandler` checks for an existing `LedgerTransaction` by `(environment, reference)` before posting. Duplicate reference in the same environment returns the existing result; TEST and LIVE never share postings or balances.
- `Idempotency-Key` header on the API is mapped to `reference` for callers that supply it.

### Outbox
- `LedgerTransactionPostedEvent` is written to the outbox table **in the same DB transaction** as the ledger post. If the transaction rolls back, the event is never published.

---

## Implementation Additions Required by This Design

- `LedgerPartyType` plus party fields on domain/JPA ledger accounts.
- Party-aware repository lookup and uniqueness constraints.
- `UnfreezeAccountCommand`/handler for the explicit reverse transition.
- `OrganizationBankingActivatedListener` replacing the legacy virtual-account bootstrap listener.
- `ReservedAccountActivatedListener` for customer/vendor ledger-account creation.
- `ReservedAccountFundedListener` and `OrganizationAccountFundedListener` replacing the ambiguous wallet-funded flow.
- Dedicated party-balance queries that enforce organization and party ownership.
- Migration/reconciliation for any legacy transaction referring to an undefined customer sub-ledger.
