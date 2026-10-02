# Pay Accounts Design (`atlashub-pay:accounts`)

## Role & Purpose

The `accounts` submodule owns AtlasHub's model of Anchor banking resources and their lifecycle. Every banking-enabled AtlasHub organization receives:

1. An Anchor `CURRENT` **DepositAccount** for the verified business customer.
2. An Anchor **SubAccount** belonging to that business customer and parented by AtlasHub's FBO deposit account.

Organizations may then issue Anchor **ReservedAccounts** to their customers and marketplace vendors. Each reserved account routes collections to the issuing organization's Anchor subaccount.

AtlasHub is the system of record for ownership, provisioning intent, lifecycle, routing, idempotency, and links to local organizations/customers/vendors. Anchor is the banking execution system and supplies confirmed external identifiers, account numbers, bank details, and externally effective state.

This submodule does **not** own balances. AtlasHub balances and money movement are owned by `pay:ledger`. Anchor balances and transactions are reconciliation evidence.

---

## 1. Terminology and Boundaries

| Concept | Meaning |
|---|---|
| `BusinessDepositAccount` | Full Anchor `DepositAccount` (`CURRENT`) issued to the AtlasHub organization's verified Anchor `BusinessCustomer` |
| `BusinessSubAccount` | Anchor subledger account for the organization, parented by AtlasHub's FBO root account |
| `ReservedAccount` | Permanent collection account assigned to an organization's customer or vendor and routed to the organization's subaccount |
| `VirtualNuban` | Anchor account-number resource/pointer associated with an Anchor account; stored as external banking details, not used as the aggregate name |
| `LedgerAccount` | AtlasHub-only double-entry account in `pay:ledger`; never interchangeable with an Anchor account/subaccount |

The former generic `VirtualAccount` aggregate is deprecated because it conflated these resources.

---

## 2. Platform Anchor Programme Account

AtlasHub's FBO account is a platform-level resource and the parent of every organization subaccount.

```
AnchorProgramAccount
├── id: Long
├── environment: AnchorEnvironment          SANDBOX | LIVE
├── currency: Currency                       NGN initially
├── accountType: ProgramAccountType          FBO
├── anchorAccountId: String                  unique per environment/currency/type
├── status: ProgramAccountStatus             ACTIVE | DISABLED
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

The Anchor FBO ID must never be hard-coded or accepted from an organization request. It is secure, environment-aware platform configuration managed by authorized operations staff.

---

## 3. Organization Banking Profile

### `OrganizationBankingProfile` (Aggregate Root)

Coordinates the two resources every organization must have.

```
OrganizationBankingProfile
├── id: Long
├── organizationId: Long                     unique
├── anchorBusinessCustomerId: String        supplied by approved compliance
├── businessDepositAccountId: Long          nullable until locally requested
├── businessSubAccountId: Long              nullable until locally requested
├── status: BankingProfileStatus
│   PENDING | PROVISIONING_DEPOSIT | PROVISIONING_SUBACCOUNT
│   PARTIALLY_PROVISIONED | ACTIVE | SUSPENDED | FAILED
├── activeRestrictions: Set<BankingRestrictionType>
├── failureCode: String                     nullable
├── failureMessage: String                  sanitized, nullable
├── createdAt: ZonedDateTime
├── updatedAt: ZonedDateTime
└── version: Long
```

**Rules:**

- One profile per organization.
- Provisioning starts only from an effective `OrganizationComplianceApprovedEvent`.
- The event's `anchorBusinessCustomerId` is mandatory.
- `ACTIVE` requires both the deposit account and subaccount to be `ACTIVE`.
- The profile is usable only when it has no active restriction.
- Partial success is retained and reconciled; successful Anchor resources are never recreated blindly.
- `OrganizationBankingActivatedEvent` is emitted only on the first transition to `ACTIVE`.

---

## 4. Business Deposit Account

### `BusinessDepositAccount` (Aggregate Root)

```
BusinessDepositAccount
├── id: Long                                  AtlasHub ID
├── organizationId: Long
├── bankingProfileId: Long
├── anchorAccountId: String                  unique, nullable until accepted by Anchor
├── anchorBusinessCustomerId: String
├── productType: DepositProductType          CURRENT
├── accountName: String                      nullable until confirmed
├── accountNumber: String                    encrypted at rest, nullable until confirmed
├── maskedAccountNumber: String              safe display value
├── bankName: String                         nullable until confirmed
├── bankCode: String                         nullable until confirmed
├── currency: Currency                       NGN initially
├── frozen: Boolean
├── status: ExternalAccountStatus
│   REQUESTED | PENDING | ACTIVE | SUSPENDED | FROZEN | CLOSED | FAILED
├── failureReason: String                    nullable
├── createdAt: ZonedDateTime
├── activatedAt: ZonedDateTime               nullable
├── updatedAt: ZonedDateTime
└── version: Long
```

The provider request creates an Anchor `DepositAccount` with `productName = CURRENT` and a `BusinessCustomer` relationship using the approved compliance customer ID. A synchronous `200/202` response records acceptance, not necessarily final activation. Confirmed account details arrive through the provider response, webhook, or reconciliation fetch.

Only one non-closed business deposit account is allowed per organization and currency.

---

## 5. Business Subaccount

### `BusinessSubAccount` (Aggregate Root)

```
BusinessSubAccount
├── id: Long                                  AtlasHub ID
├── organizationId: Long
├── bankingProfileId: Long
├── anchorSubAccountId: String               unique, nullable until accepted
├── anchorBusinessCustomerId: String
├── anchorParentFboAccountId: String
├── anchorVirtualNubanId: String             nullable
├── accountName: String                      nullable
├── accountNumber: String                    encrypted, nullable
├── maskedAccountNumber: String              nullable
├── bankName: String                         nullable
├── bankCode: String                         nullable
├── currency: Currency                       NGN initially
├── status: ExternalAccountStatus
├── failureReason: String                    nullable
├── createdAt: ZonedDateTime
├── activatedAt: ZonedDateTime               nullable
├── updatedAt: ZonedDateTime
└── version: Long
```

Anchor request shape:

```json
{
  "data": {
    "type": "SubAccount",
    "attributes": {
      "createVirtualNuban": true
    },
    "relationships": {
      "customer": {
        "data": {
          "id": "<organization-anchor-business-customer-id>",
          "type": "BusinessCustomer"
        }
      },
      "parentAccount": {
        "data": {
          "id": "<atlashub-fbo-anchor-account-id>",
          "type": "DepositAccount"
        }
      }
    }
  }
}
```

The parent is always AtlasHub's configured FBO account, **not** the organization's business deposit account.

Only one non-closed business subaccount is allowed per organization and currency.

---

## 6. Reserved Accounts

### `ReservedAccount` (Aggregate Root)

```
ReservedAccount
├── id: Long                                  AtlasHub ID
├── organizationId: Long
├── ownerType: ReservedAccountOwnerType      CUSTOMER | VENDOR
├── ownerReferenceId: String                 local customer/vendor ID
├── anchorReservedAccountId: String          unique, nullable until accepted
├── anchorCustomerId: String                 customer created/reused by Anchor
├── businessSubAccountId: Long
├── anchorPayoutSubAccountId: String
├── provider: ReservedAccountProvider        NINEPSB | PROVIDUS | ...
├── accountName: String                      nullable until confirmed
├── accountNumber: String                    encrypted, nullable until confirmed
├── maskedAccountNumber: String
├── bankName: String
├── bankCode: String                         nullable
├── currency: Currency                       NGN initially
├── status: ExternalAccountStatus
├── activeRestrictions: Set<AccountRestrictionType>
├── requestReference: String                 unique AtlasHub idempotency/correlation key
├── failureReason: String                    nullable
├── createdAt: ZonedDateTime
├── activatedAt: ZonedDateTime               nullable
├── updatedAt: ZonedDateTime
└── version: Long
```

**Rules:**

- The owner must be an existing customer/vendor belonging to the organization.
- The organization's banking profile and subaccount must be `ACTIVE`.
- The reserved account's Anchor `payoutAccount` is the organization's Anchor subaccount.
- The business deposit account is not used as the reserved-account payout route.
- Default uniqueness is `(organizationId, ownerType, ownerReferenceId, provider)` for non-closed accounts.
- Repeating an issuance command with the same idempotency key returns the existing local result.
- Reserved-account activation never bootstraps organization ledger accounts.

The API supports both individual and business customer identity variants without leaking Anchor request DTOs into the domain.

---

## 7. State Transitions

`ExternalAccountStatus` values are `REQUESTED`, `PENDING`, `ACTIVE`, `SUSPENDED`, `FROZEN`, `CLOSED`, and `FAILED`. `FROZEN` represents an externally frozen banking resource; `SUSPENDED` represents an AtlasHub-blocked resource that may or may not have a matching provider lifecycle operation.

Restriction sources are tracked independently, for example `COMPLIANCE`, `ORGANIZATION_BAN`, `MANUAL`, and `RISK`. Removing an organization ban clears only `ORGANIZATION_BAN`; it must not reactivate an account still restricted by compliance, risk, or a manual action.

```
REQUESTED → PENDING → ACTIVE → FROZEN/SUSPENDED → ACTIVE
                         └────────────────────────→ CLOSED
REQUESTED/PENDING → FAILED → REQUESTED (explicit retry after reconciliation)
```

Use separate domain methods:

- `markSubmitted(anchorResourceId)`
- `activate(confirmedBankingDetails)`
- `freeze(reason)` / `suspend(reason)`
- `reactivate()`
- `close()`
- `fail(code, reason)`

`activate()` is only for initial activation. Reactivating a suspended/frozen account must call `reactivate()`; the old design's reuse of `activate()` for unbanning is invalid.

Platform state can block use even when a particular provider resource lacks an equivalent lifecycle operation. The adapter calls only provider operations confirmed for that resource type; reconciliation records any difference between AtlasHub's desired state and Anchor's effective state.

---

## 8. Anchor Banking Port

```java
public interface AnchorBankingPort {
    DepositAccountProvisioningResult createBusinessDepositAccount(
        String anchorBusinessCustomerId,
        String productName,
        String requestReference);

    SubAccountProvisioningResult createBusinessSubAccount(
        String anchorBusinessCustomerId,
        String anchorParentFboAccountId,
        boolean createVirtualNuban,
        String requestReference);

    ReservedAccountProvisioningResult createReservedAccount(
        ReservedAccountCustomer customer,
        String provider,
        String anchorPayoutSubAccountId,
        String requestReference);

    AnchorDepositAccountDetails fetchDepositAccount(String anchorAccountId);
    AnchorSubAccountDetails fetchSubAccount(String anchorSubAccountId);
    AnchorReservedAccountDetails fetchReservedAccount(String anchorReservedAccountId);

    void freezeDepositAccount(String anchorAccountId, FreezeReason reason);
    void unfreezeDepositAccount(String anchorAccountId);
}
```

Additional suspend/reactivate/close capabilities are exposed only after the matching Anchor operation is confirmed. Unsupported operations still block the resource locally and create an operations/reconciliation task; infrastructure must not invent provider success.

---

## 9. Provisioning Commands

### `ProvisionOrganizationBankingCommand`

Triggered only by `OrganizationComplianceApprovedEvent`.

1. Create/load `OrganizationBankingProfile` by organization ID.
2. Verify the event has an Anchor business-customer ID and the compliance decision remains approved.
3. Create a local `BusinessDepositAccount` in `REQUESTED` and persist the request intent.
4. Submit the Anchor `CURRENT` deposit-account request through an outbox-driven worker.
5. After the deposit account is active, create a local `BusinessSubAccount` in `REQUESTED`.
6. Resolve the active AtlasHub FBO programme account for environment/currency.
7. Submit the Anchor subaccount request with the business customer and FBO relationships.
8. Mark the profile `ACTIVE` only after both accounts are active.
9. Publish `OrganizationBankingActivatedEvent` once.

The workflow is resumable from every persisted state and never wraps a remote API call inside a database transaction.

### `IssueReservedAccountCommand`

```java
public record IssueReservedAccountCommand(
    Long organizationId,
    ReservedAccountOwnerType ownerType,
    String ownerReferenceId,
    ReservedAccountCustomer customer,
    String provider,
    String idempotencyKey
) {}
```

The handler validates ownership and banking readiness, persists the local `REQUESTED` aggregate, and writes a provider request to the outbox. The worker calls Anchor using the active organization subaccount as `payoutAccount`.

### Lifecycle Commands

- `SuspendReservedAccountCommand`
- `ReactivateReservedAccountCommand`
- `CloseReservedAccountCommand`
- `SuspendOrganizationBankingCommand`
- `ReactivateOrganizationBankingCommand`
- `ReconcileExternalAccountCommand`

Organization ban or compliance suspension affects the banking profile and all associated payment capability. It does not delete or automatically close external accounts.

- `OrganizationComplianceSuspendedEvent` adds the `COMPLIANCE` restriction.
- `OrganizationComplianceReinstatedEvent` removes only the `COMPLIANCE` restriction.
- `OrganizationBannedEvent` adds the `ORGANIZATION_BAN` restriction.
- An unban event removes only `ORGANIZATION_BAN` and reactivates capability only when no restrictions remain.
- Manual reserved-account suspension adds/removes only the `MANUAL` restriction.

---

## 10. Provider Webhooks and Reconciliation

Expected normalized provider facts include:

- Deposit account accepted/created/failed/frozen/unfrozen
- Subaccount created/failed and virtual-NUBAN relationship
- Reserved account created/failed
- Inbound transfer/collection

Webhook processing:

1. Validate the signature against the raw request body.
2. Deduplicate by `(anchorEventId, consumerName)`.
3. Resolve an existing local request by Anchor resource ID or AtlasHub request reference.
4. Apply an idempotent state transition.
5. Persist the state and outbox events in one local transaction.
6. Quarantine unknown or contradictory resources for reconciliation.

A scheduled reconciler fetches all non-terminal pending/failed-due-to-timeout resources and compares local desired state with Anchor's effective state. Reconciliation never overwrites ownership or routing using untrusted webhook payload fields.

---

## 11. Funding Events

Inbound Anchor transfers are normalized to `ReservedAccountFundedEvent` or `OrganizationAccountFundedEvent` after the destination is resolved locally.

`ReservedAccountFundedEvent.payload`:

```
reservedAccountId
organizationId
ownerType
ownerReferenceId
businessSubAccountId
anchorTransferReference
amount
currency
senderAccountName
senderBankCode
receivedAt
```

The Anchor transfer reference is the ledger idempotency key. A reserved-account receipt does not automatically prove revenue: the ledger posts to customer funds, vendor payable, matched commerce transaction, or suspense according to the business context.

---

## 12. Domain Events

Account-domain events are published on `pay-events` through the transactional outbox.

| Event | Published When | Consumers |
|---|---|---|
| `BusinessDepositAccountActivatedEvent` | Deposit account confirmed active | Banking orchestrator, notifications |
| `BusinessSubAccountActivatedEvent` | FBO-backed subaccount confirmed active | Banking orchestrator, notifications |
| `OrganizationBankingActivatedEvent` | Both mandatory business accounts are active | `pay:ledger`, notifications |
| `OrganizationBankingProvisioningFailedEvent` | Terminal provisioning failure | Operations, notifications |
| `ReservedAccountRequestedEvent` | Local issuance intent committed | Provider worker |
| `ReservedAccountActivatedEvent` | Anchor confirms reserved account | Notifications, transaction query |
| `ReservedAccountProvisioningFailedEvent` | Anchor rejects/fails issuance | Notifications, operations |
| `ReservedAccountSuspendedEvent` | Account blocked | Notifications |
| `ReservedAccountReactivatedEvent` | Account restored | Notifications |
| `ReservedAccountClosedEvent` | Account permanently closed | Notifications |
| `ReservedAccountFundedEvent` | Confirmed inbound transfer mapped to reserved account | `pay:ledger`, notifications, `pay:tx-query`, `pay:webhooks` |

`OrganizationBankingActivatedEvent.payload`:

```
organizationId
bankingProfileId
businessDepositAccountId
businessSubAccountId
currency
activatedAt
```

`ReservedAccountActivatedEvent.payload`:

```
reservedAccountId
organizationId
ownerType
ownerReferenceId
businessSubAccountId
anchorReservedAccountId
accountName
maskedAccountNumber
bankName
currency
activatedAt
```

---

## 13. Queries and REST API

### Queries

- `GetOrganizationBankingProfileQuery(organizationId)`
- `GetBusinessDepositAccountQuery(organizationId)`
- `GetBusinessSubAccountQuery(organizationId)`
- `GetReservedAccountQuery(organizationId, reservedAccountId)`
- `ListReservedAccountsQuery(organizationId, ownerType, ownerReferenceId, status, page, size)`

Reserved accounts are pageable; marketplace organizations can have many customers/vendors.

### Endpoints

| Method | Path | Permission | Purpose |
|---|---|---|---|
| `GET` | `/api/v1/pay/accounts/business` | `pay:accounts:read` | Banking profile and masked business accounts |
| `POST` | `/api/v1/pay/accounts/reserved` | `pay:accounts:create` | Issue customer/vendor reserved account |
| `GET` | `/api/v1/pay/accounts/reserved` | `pay:accounts:read` | Page through reserved accounts |
| `GET` | `/api/v1/pay/accounts/reserved/{id}` | `pay:accounts:read` | View one reserved account |
| `POST` | `/api/v1/pay/accounts/reserved/{id}/suspend` | `pay:accounts:suspend` | Block account use |
| `POST` | `/api/v1/pay/accounts/reserved/{id}/reactivate` | `pay:accounts:reactivate` | Restore suspended account |
| `POST` | `/api/v1/pay/accounts/reserved/{id}/close` | `pay:accounts:close` | Permanently close after provider capability check |

Organization and requester IDs come from authenticated context, not trusted request bodies. Full account numbers are returned only to authorized operations and are otherwise masked.

---

## 14. RBAC

| Permission | Operation |
|---|---|
| `pay:accounts:create` | Issue customer/vendor reserved accounts |
| `pay:accounts:read` | View organization banking profile and reserved accounts |
| `pay:accounts:suspend` | Suspend a reserved account |
| `pay:accounts:reactivate` | Reactivate a suspended reserved account |
| `pay:accounts:close` | Permanently close a reserved account |

Organization business deposit/subaccount creation is system-only. Organization-wide banking suspension/reactivation is a compliance/admin operation, not an organization self-service permission.

---

## 15. Persistence Constraints

- Unique active banking profile per organization.
- Unique Anchor resource ID per resource table.
- Unique non-closed deposit account per `(organizationId, currency)`.
- Unique non-closed subaccount per `(organizationId, currency)`.
- Unique non-closed reserved account per `(organizationId, ownerType, ownerReferenceId, provider)`.
- Unique `requestReference`/idempotency key.
- Optimistic locking on all account aggregates.
- Store encrypted full account numbers only where operationally required; store masked variants for ordinary reads.

Remote calls are outside database transactions. Local request intent is committed before dispatch. Provider responses/webhooks are applied in new transactions.

---

## 16. Legacy Migration

1. Add the new profile, deposit-account, subaccount, programme-account, and reserved-account tables.
2. Keep legacy `VirtualAccount` reads behind a compatibility facade.
3. Query Anchor for every legacy `anchorAccountId` to determine its actual resource type.
4. Migrate ownership and external IDs only after reconciliation; do not infer type from legacy `ownerType`.
5. Change ledger bootstrap from `VirtualAccountActivatedEvent` to `OrganizationBankingActivatedEvent`.
6. Change customer issuance endpoints to create `ReservedAccount` resources.
7. Stop legacy writes, monitor reconciliation, then remove the old aggregate and routes.

No legacy virtual account is automatically classified as a deposit account, subaccount, or reserved account without provider confirmation.
