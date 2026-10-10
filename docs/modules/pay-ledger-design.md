# Pay Ledger Design (`atlashub-pay:ledger`)

## Purpose

`ledger` is AtlasHub Pay's append-only double-entry accounting engine and the owner of organization financial
accounts. Provider resources are not ledger accounts: Anchor owns the real operating bank account, Paystack owns
collection/settlement execution, while AtlasHub ledger accounts provide immediate, environment-scoped balances and
an auditable accounting trail.

Corrections use balanced reversals. Posted transactions and entries are never edited or deleted.

## Accounts

`LedgerAccount` contains organization, `TEST | LIVE` environment, type, display name, optional outlet/party scope,
currency, normal balance, status, independent restriction sources, and timestamps.

Scopes are derived from account type:

- `BUSINESS`: `OPERATING`, `PAYROLL_RESERVE`, `TAX_HOLDING`, `ESCROW`, `SPLIT_HOLDING`, and `CUSTOM`
- `SYSTEM`: `PROVIDER_CLEARING` and `SUSPENSE`
- `OUTLET`: `TILL`
- `CUSTOMER`: `CUSTOMER_FUNDS`
- `VENDOR`: `VENDOR_PAYABLE`

On every `OrganizationRegistered` replay, the ledger idempotently ensures only three foundation accounts in both
TEST and LIVE:

1. `OPERATING`
2. `PROVIDER_CLEARING`
3. `SUSPENSE`

Optional business accounts are created explicitly by an authorized organization user. Outlet, customer, vendor,
escrow, split, tax, and payroll accounts can also be created lazily by the workflow that actually needs them. Anchor
banking activation is a banking-status event and is not a ledger-bootstrap trigger.

Every created account receives a zero `BalanceSnapshot`. Balances are derived from the latest snapshot plus later
entries; there is no mutable current-balance column.

## Transactions

`LedgerTransaction` owns at least two immutable `LedgerEntry` values. Construction rejects unequal debit and credit
totals. Posting locks all affected accounts in ascending account-ID order, verifies organization/environment/currency
and account usability, saves the transaction, and publishes `LedgerTransactionPostedEvent`.

The unique `(environment, reference)` key is the posting idempotency boundary. Source systems include card charges,
external collection, payout, settlement, split, commerce, payroll, and `INTERNAL_TRANSFER`.

### Important postings

- Verified charge receipt: debit provider clearing, credit the applicable operating/party/business account.
- Charge refund: debit operating/customer funds, credit provider clearing.
- Confirmed Anchor funding: provider/account clearing against the attributed business or party account.
- Final Paystack settlement: only after settlement has both Paystack evidence and matching Anchor credit; clears the
  provider receivable without recognizing revenue again.
- Internal account transfer: debit the source business account and credit the destination business account.

## Application API

### Commands

- `ProcessLedgerEventCommand`: idempotently handles registration, verified charge/refund, funding, settlement, and
  payout event actions.
- `PostLedgerTransactionCommand`: internal balanced posting primitive.
- `CreateBusinessAccountCommand`: creates an optional business-scope account and initial snapshot; rejects duplicate
  names/types and system/outlet/party types.
- `TransferBetweenAccountsCommand`: moves available ledger value between two active business accounts in the same
  organization, environment, and currency.
- Administrative freeze, unfreeze, close, and snapshot commands remain platform-operations capabilities.

### Queries

- `ListFinancialAccountsQuery`: active business accounts with current balance, restrictions, and display metadata.
  The LIVE operating account includes masked Anchor account details when provisioned.
- `GetFinancialAccountQuery`: one owned active business account with the same balance/banking view.

Transaction-history queries do not live here. `pay:tx-query` owns the unified organization, account, customer, and
vendor transaction APIs populated from ledger/provider events.

## REST API

Tenant and environment always come from `AuthenticatedPrincipal`.

| Method | Endpoint | Permission | Purpose |
|---|---|---|---|
| `GET` | `/api/v1/pay/accounts` | `pay:ledger:read` | List active business accounts, balances, and operating-bank information |
| `GET` | `/api/v1/pay/accounts/{accountId}` | `pay:ledger:read` | Get one owned active business account |
| `POST` | `/api/v1/pay/accounts` | `pay:ledger:manage` | Create an optional business account |
| `POST` | `/api/v1/pay/account-transfers` | `pay:ledger:manage` | Transfer between owned business accounts |

Old public `/api/v1/ledger/...` balance/history endpoints are removed. Administrative platform endpoints are
unchanged.

## Cross-module contracts

- `BusinessBankingQueryPort` is a shared read contract implemented by `pay:accounts`. Ledger uses it only to attach
  safe, masked operating-bank details to the operating account response.
- All money-state writes from other modules arrive asynchronously as provider-neutral events.
- `LedgerTransactionPostedEvent` includes organization, environment, reference, source context, currency, time, and
  entries. `pay:tx-query` consumes it to attach account activity to the unified read model.

## Persistence and concurrency

- `pay_ledger_accounts` stores `account_name`; existing rows may use the type-derived name when the column is null.
- Account name/type lookups are organization/environment/currency scoped.
- Account locks are pessimistic and acquired in ascending ID order during posting.
- Transaction/replay uniqueness prevents duplicate balances.
- Account numbers never live in ledger persistence; only masked banking data is returned from the accounts-owned
  query adapter.

## Permissions

- `pay:ledger:read`: view financial accounts and unified transaction history.
- `pay:ledger:manage`: create optional business accounts and transfer between them.
- Freeze/unfreeze/close remain platform staff operations and are not organization permissions.
