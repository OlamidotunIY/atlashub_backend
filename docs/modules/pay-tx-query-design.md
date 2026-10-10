# Pay Transaction Query Design (`atlashub-pay:tx-query`)

## Purpose and ownership

`tx-query` is the organization-facing financial activity read model. It combines provider-neutral charge,
funding, payout, settlement, and ledger-posting events into one searchable transaction history without reading
another Pay module's database or importing another module's domain/application code.

This module owns its projection, persistence, filters, and REST APIs. It never moves money. All projection writes
are event-driven and idempotent; all public operations are reads scoped to the authenticated organization and API
environment.

## Aggregate

`TransactionRecord` is the persisted projection aggregate:

- identity: AtlasHub transaction-record ID
- tenant scope: `organizationId` and `ApiEnvironment`
- lifecycle: `TransactionType` and `TransactionStatus`
- money: amount, provider fee, net amount, and currency
- provider context: channel, provider, and provider-neutral reference
- business attribution: source system/reference, customer/vendor party type/reference, and outlet
- payout context: recipient name and account number when applicable
- display/audit context: description, string metadata, created/completed/updated timestamps
- `TransactionAccountEntry` children: ledger account ID, optional account type/name, debit/credit entry type,
  account-relative direction, amount, and currency

The supported types are `CHARGE`, `REFUND`, `PAYOUT`, `SETTLEMENT`, `ACCOUNT_FUNDING`, `ACCOUNT_TRANSFER`, and
`LEDGER_POST`. Status values are `PENDING`, `SUCCESSFUL`, `FAILED`, `REFUND_PENDING`, `REFUNDED`, `DISPUTED`, and
`REVERSED`.

The unique business key is `(organization_id, api_environment, reference)`. Events for the same real-world
transaction enrich that record rather than creating duplicates. For example, a charge event establishes payment
metadata and the later ledger-posting event attaches account entries using the same reference.

## Event projections

Infrastructure listeners on `pay-events` delegate only to application command handlers:

| Source event | Projection result |
|---|---|
| `ChargeInitializedEvent` | Pending charge with customer/source attribution |
| `ChargeSuccessfulEvent` / `ChargeFailedEvent` | Successful or failed charge, including provider fee |
| Refund/dispute charge events | Update the existing charge status |
| `OrganizationAccountFundedEvent` | Operating-account funding activity |
| `ReservedAccountFundedEvent` | Customer/vendor transfer collection activity |
| `LedgerTransactionPostedEvent` | Ledger entries and account-relative directions; enriches an existing provider record when references match |
| `ProviderSettlementReceivedEvent` | Confirmed two-sided settlement only |
| `PayoutCompletedEvent` | Completed outbound payout when the transfers module publishes it |

Kafka inbox tracking deduplicates each event for its consumer group. Repository uniqueness is the final replay
guard. Browser callbacks and unverified provider payloads never feed this module directly.

## Persistence

| Table | Purpose | Important constraints/indexes |
|---|---|---|
| `pay_transaction_records` | Top-level searchable transaction record | unique organization/environment/reference; organization/environment/time, status, party, and source indexes |
| `pay_transaction_account_entries` | Account entries attached to a transaction | unique record/account/entry-type; record and account lookup indexes |

The repository uses JPA specifications for combined filters and caps page size at 100. Ordering is stable by
`createdAt` and then ID. `metadata` is stored as MySQL `json`.

## Queries and filters

`ListTransactionsQuery` supports:

- account ID
- type and status
- account-relative direction
- channel and provider
- source system and source reference
- party type and party reference (customers and vendors)
- outlet ID
- currency and exact transaction reference
- free-text search over reference, description, and source reference
- minimum/maximum amount
- from/to timestamps
- page, size, and ascending/descending time order

Additional queries retrieve a transaction by ID, retrieve it by reference, and calculate monthly charge/payout
volume for the active organization/environment.

## REST API

All endpoints require `pay:ledger:read`. Tenant/environment scope comes only from `AuthenticatedPrincipal`.

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/v1/pay/transactions` | Advanced filtered, paginated organization-wide activity |
| `GET` | `/api/v1/pay/transactions/{id}` | Transaction details and account entries |
| `GET` | `/api/v1/pay/transactions/by-reference?reference=...` | Exact reference lookup |
| `GET` | `/api/v1/pay/transactions/volume?month=YYYY-MM` | Monthly charge/payout volume |
| `GET` | `/api/v1/pay/accounts/{accountId}/transactions` | Paginated transactions touching one ledger account |
| `GET` | `/api/v1/pay/customers/{customerId}/transactions` | Paginated customer-attributed activity |
| `GET` | `/api/v1/pay/vendors/{vendorId}/transactions` | Paginated vendor-attributed activity |

Responses use presentation DTOs and `ApiResponse`. Account-specific results expose direction relative to the
requested account rather than whichever ledger entry happened to be loaded first.

## Boundaries

- `tx-query` imports only `atlashub-shared` and framework libraries.
- It does not import Paystack/Anchor infrastructure or other Pay module internals.
- Cross-module writes arrive as events; there are no synchronous calls to charge, settlement, ledger, or account
  handlers/repositories.
- The source modules remain authoritative for execution state. This projection is optimized for organization-facing
  search and audit views.
