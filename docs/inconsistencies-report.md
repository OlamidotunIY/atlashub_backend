# Comprehensive Architecture Documentation Inconsistencies

This report outlines all identified contradictions, missing user flows, and event-driven inconsistencies across the AtlasHub documentation. 

## 1. The `pay:ledger` Listener Void (Massive Integration Gap)
The `pay-ledger-design.md` explicitly states that it is the "double-entry accounting engine" and that "no other module moves money without posting a corresponding ledger entry." 
**However, `pay-ledger-design.md` defines absolutely NO inbound Kafka listeners.** It expects synchronous API commands (`PostLedgerTransaction`, `CreateLedgerAccount`). 

Despite this, **multiple modules falsely claim that `pay:ledger` listens to their events:**
* **`pay-accounts-design.md`** claims `pay:ledger` listens to `VirtualAccountActivatedEvent` to bootstrap org ledger accounts.
* **`pay-transfers-design.md`** claims `pay:ledger` consumes `PayoutCompletedEvent` to post payout journal entries.
* **`commerce-inventory-design.md`** claims `pay:ledger` consumes `StockTransferReceivedEvent` to make inter-outlet stock/cash ledger entries.
* **`pay-charges-design.md`** claims `pay:ledger` consumes `ChargeSuccessfulEvent` to post collection entries.

**Impact:** If implemented exactly as documented, the ledger would never be updated by any of these actions because the ledger module has no listeners configured to catch these events.

## 2. Synchronous vs. Asynchronous Contradictions in `pay-charges`
**`pay-charges-design.md`** contradicts itself on how it communicates with the ledger:
* **The Text** states: "On a successful charge, this submodule posts the corresponding ledger entry (via `pay:ledger`) and publishes `ChargeSuccessfulEvent`..." — This implies a synchronous cross-module port call *before* publishing the event.
* **The Event Table** states: `ChargeSuccessfulEvent` is consumed by `pay:ledger (post entry)`. 
* **Impact:** It's unclear if the ledger entry is posted synchronously before the event fires, or asynchronously by the ledger reacting to the event (which, as noted above, the ledger does not actually do).

## 3. Phantom Command in `pay-accounts`
* **`pay-accounts-design.md`** states that when a bank transfer is received, it "calls `CreditWalletFromTransferHandler` in `pay:ledger`".
* **`pay-ledger-design.md`** does not contain any such command or handler. It only exposes `PostLedgerTransactionHandler`.
* **Impact:** The accounts module is trying to synchronously call a handler that doesn't exist.

## 4. Disconnect in HR Payout Reconciliation (`hr` vs `pay:transfers`)
* **`pay-transfers-design.md`** states that when a payout succeeds, it fires `PayoutCompletedEvent`, which is consumed by `hr` (to "mark salary disbursed").
* **`hr-payroll-design.md`** explicitly documents that it *only* listens to `BulkPayoutCompletedEvent`. It has no listener for the individual `PayoutCompletedEvent`.
* **`hr-loan-design.md`** (which also involves disbursements) does not mention payouts, `PayoutCompletedEvent`, or any reconciliation mechanism at all.
* **Impact:** Individual salary corrections, loan disbursements, or off-cycle single payouts will succeed in the payment gateway, but the HR module will never know about it because it isn't listening to the single-payout event.

## 5. The Missing `Outlet` Lifecycle (Ghost Aggregate)
* As established earlier, TILL ledger accounts, Storefront POS transactions, Inventory levels, and Reverse Logistics all depend heavily on an `outletId`.
* **`pay-ledger-design.md`** claims it triggers TILL account creation on "outlet creation".
* However, **across the entire architecture (`commerce`, `accounts`, `admin`, `hr`, etc.), there is no module that documents the creation of an `Outlet` aggregate or an `OutletCreatedEvent`**. 
* **Impact:** The foundational entity that physical commerce and physical ledger accounts rely on is missing from the system design, meaning developers have no specifications on how to onboard a new branch or location.

## 6. Ledger Freezing Gap in Admin Compliance
* **`admin-design.md`** states that banning an organization fires an `OrganizationBannedEvent`, which is consumed by `pay:accounts` (to freeze virtual accounts) and `billing` (to cancel subscriptions).
* **It does not mention `pay:ledger`**.
* **Impact:** A banned organization would have their cards and virtual accounts frozen, but their internal ledger balances would remain active, potentially allowing internal fund movements to continue.

## 7. Cross-Module Write Ports (Architecture Violations)
The architecture explicitly dictates that state changes must happen via domain events, not synchronous ports. Several modules violate this:
* **`admin` -> `compliance`**: Admin executes KYC approval by synchronously calling `ComplianceActionPort.approve()`.
* **`commerce-storefront` -> `pay:charges`**: A cashier checkout synchronously calls `PayInitializeChargePort.initialize()` to create a `Charge` record.
* **`hr-payroll` -> `pay:transfers`**: Initiating payroll synchronously calls `PayBulkPayoutPort.initiateBulkPayout()`.
* **`pay-mandates` -> `pay:charges`**: Mandate scheduler synchronously calls `ChargePort` to initiate a charge.
* **`commerce-storefront` -> `commerce-inventory`**: POS checkout synchronously calls `inventory.reserveStock()`, `inventory.deductStock()`, and `inventory.releaseReservedStock()`.

## 8. Payload Mismatches (Broken Contracts)
* **Commerce Missing Order ID**: `commerce-storefront` listens to `ChargeSuccessfulEvent` and expects an `orderId` to complete the sale. `pay:charges` only provides `chargeReference` and `gatewayReference`.
* **Notifications Missing Email**: `notifications` consumes `VirtualAccountActivatedEvent` to email the org admin, but `pay-accounts` omits the email address from the payload.

## 9. Ignored Security Events (Catastrophic Gap)
* **`OrganizationBannedEvent`**: `admin-design.md` claims `billing` and `pay:accounts` consume this to freeze accounts and cancel subscriptions. **Neither `billing`, `pay:accounts`, `auth`, nor `pay:ledger` have a listener for this event.** A banned organization retains full system access.

## 10. Event Name & Listener Mismatches
* **Customer Returns Broken**: `logistics-returns` publishes `ReturnShipmentReceivedEvent`. `commerce-inventory` listens for `CustomerReturnShipmentReceivedEvent`.
* **Credit Sales Broken**: `accounting-ap-ar` listens to `CreditSaleCompletedEvent`. `commerce-storefront` handles `ProcessCreditSaleCommand` but never documents publishing this event.
* **Phantom Till Handlers**: `commerce-storefront` publishes `TillOpenedEvent` and `TillClosedEvent` and claims `pay` consumes them. `pay` has no listeners for these.
* **Mandate Notifications**: `pay-mandates` publishes Pause/Resume/Revoke events and claims `notifications` and `pay:webhooks` consume them. Neither module listens to them.
* **Settlement Notifications**: `pay-settlement` claims `notifications` consumes `SettlementConfirmedEvent`. `notifications` has no listener.
* **Analytics & Payroll**: `analytics` documents listening to `PayrollDisbursedEvent`, but `hr-payroll` does not list `analytics` as a consumer.
* **Leave & Attendance Missing Connection**: `hr-leave` publishes `LeaveApprovedEvent` and claims `hr-attendance` consumes it to mark the employee on leave. `hr-attendance` has no listener.
* **HR Ghost Access**: `hr-staff` claims `iam` listens to `EmployeeSuspendedEvent` (to revoke access) and `pay`, `accounting`, `notifications` listen to `EmployeeTerminatedEvent`. **None of these modules have the respective listeners.**

## 11. B2B2C Virtual Account & Transfer Failures
* **WalletFundedEvent Ignored**: When a customer transfers money into their virtual account, `pay-accounts` publishes `WalletFundedEvent` and claims `accounting`, `notifications`, and `pay:tx-query` consume it. **Not a single module has a listener for this event.** Inbound customer transfers disappear into the void without updating the ledger, notifying the customer, or appearing in transaction history.
* **Refunds Completely Broken**: `commerce-storefront` publishes `PosSaleRefundedEvent` and expects `pay` to issue a refund payout. **No module in `pay` (`pay-transfers`, `pay-charges`, etc.) listens to `PosSaleRefundedEvent`.** A refunded sale never actually returns money to the customer.
