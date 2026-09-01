# Cross-Module Sagas & Compensation Strategies

Because AtlasHub uses a decoupled, event-driven Hexagonal Architecture, a single database transaction cannot span across multiple modules (e.g., Commerce and Payment). Instead, we use **Choreography-Based Sagas** to manage distributed transactions.

Below is the **exact, highly detailed** step-by-step movement, locking strategy, and compensation logic for every critical distributed operation across the platform.

---

## 1. Checkout & Stock Reservation Saga
**Modules Involved**: `atlashub-commerce`, `atlashub-pay`, `atlashub-accounting`, `atlashub-logistics`
**Goal**: Guarantee that a customer is only charged if stock is successfully reserved, and stock is strictly released if the payment fails.

### Step-by-Step Movement
1. **Initiation (`commerce`)**: A customer submits a checkout request. Commerce executes `CreateSalesOrderCommand`. A `SalesOrder` is created with status `PENDING`.
2. **Stock Reservation (`commerce`)**: Commerce executes an internal `ReserveStockCommand`.
   - It acquires a **Pessimistic Lock** (`@Lock(PESSIMISTIC_WRITE)`) on the `Inventory` entity to prevent concurrent overselling.
   - If stock is insufficient, the order is marked `FAILED` and the process stops.
   - If successful, stock is deducted (reserved), and the order status becomes `PAYMENT_PENDING`.
3. **Cross-Module Trigger (`commerce` -> `pay`)**: Commerce publishes an `OrderPaymentRequestedEvent` to the message broker using the **Outbox Pattern** to guarantee delivery.
4. **Payment Processing (`pay`)**: 
   - The Payment module's **Inbox** (`EventDeliveryTracker`) consumes the event, ensuring exactly-once processing.
   - It executes `ProcessChargeCommand` to hit the external payment gateway or deduct from a wallet.
5. **Resolution Path**:
   - **Scenario A (Success)**: 
     - Payment succeeds. `pay` uses the Outbox to publish `PaymentSuccessfulEvent`.
     - `commerce` Inbox consumes the event. Executes `CompleteSalesOrderCommand`, marking `SalesOrder` as `COMPLETED`. 
     - `accounting` Inbox consumes the event and executes `RecordJournalEntryCommand` (Debit Cash/Receivable, Credit Sales Revenue).
     - If delivery is required, `logistics` Inbox consumes the event and executes `CreateShipmentCommand`.
   - **Scenario B (Failure - COMPENSATION)**:
     - Payment fails (e.g., insufficient funds). `pay` publishes `PaymentFailedEvent`.
     - `commerce` Inbox consumes `PaymentFailedEvent`.
     - **Compensation Action**: Commerce executes `CancelStockReservationCommand`. It acquires a **Pessimistic Lock** on `Inventory`, adds the reserved quantity back to the available stock, and marks the `SalesOrder` as `FAILED`.

---

## 2. Payroll Disbursement Saga
**Modules Involved**: `atlashub-hr`, `atlashub-pay`, `atlashub-accounting`
**Goal**: Ensure salary disbursements accurately reflect wallet balances, and the payroll run can be explicitly retried if the corporate wallet has insufficient funds.

### Step-by-Step Movement
1. **Initiation (`hr`)**: HR Admin approves a payroll run. HR executes `ApprovePayrollCommand`.
2. **State Change (`hr`)**: `PayrollRun` status changes to `PROCESSING` (using Optimistic Locking `@Version`). 
3. **Cross-Module Trigger (`hr` -> `pay`)**: HR publishes `PayrollApprovedEvent` containing a list of employee bank details and net pay amounts via the **Outbox**.
4. **Payout Execution (`pay`)**:
   - The Payment module's **Inbox** consumes the event exactly once.
   - It executes `ProcessBulkPayoutCommand`.
   - It acquires a **Pessimistic Lock** on the Organization's `Wallet` to verify and deduct the total payroll amount safely.
5. **Resolution Path**:
   - **Scenario A (Success)**: 
     - `Wallet` has sufficient funds, payouts pushed to the banking gateway. `pay` publishes `BulkPayoutCompletedEvent`.
     - `hr` Inbox consumes the event, executes `MarkPayrollDisbursedCommand` (`PayrollRun` -> `DISBURSED`).
     - `accounting` Inbox consumes the event and executes `RecordJournalEntryCommand` (Debit Salary Expense, Credit Wallet Liability).
   - **Scenario B (Failure - COMPENSATION)**:
     - `Wallet` has insufficient funds. `pay` publishes `BulkPayoutFailedEvent` (with failure reason).
     - `hr` Inbox consumes `BulkPayoutFailedEvent`.
     - **Compensation Action**: HR executes `RevertPayrollProcessingCommand`. The `PayrollRun` status is rolled back from `PROCESSING` to `APPROVED` (or `FAILED`), allowing the admin to fund the wallet and retry the disbursement without losing the payroll calculation.

---

## 3. Platform Subscription Renewal Saga
**Modules Involved**: `atlashub-platform:billing`, `atlashub-pay`, `atlashub-platform:identity`
**Goal**: Automatically charge an organization for their platform usage and securely suspend product access if the payment gracefully fails.

### Step-by-Step Movement
1. **Initiation (`billing`)**: A chron job detects a subscription is due. `billing` executes `GenerateInvoiceCommand`, creating a `BillingInvoice` (`DRAFT`).
2. **Cross-Module Trigger (`billing` -> `pay`)**: Publishes `SubscriptionRenewalRequestedEvent` via **Outbox**.
3. **Payment Processing (`pay`)**:
   - `pay` Inbox consumes event. Executes `ProcessSubscriptionChargeCommand`.
   - Acquires **Pessimistic Lock** on the Organization's `Wallet` or hits the saved card gateway.
4. **Resolution Path**:
   - **Scenario A (Success)**: 
     - `pay` publishes `PaymentSuccessfulEvent`. 
     - `billing` Inbox consumes it. Executes `MarkInvoicePaidCommand` (`BillingInvoice` -> `PAID`).
     - `billing` uses Optimistic Locking (`@Version`) to update `OrganizationProduct` expiry date.
   - **Scenario B (Failure - COMPENSATION)**:
     - `pay` publishes `PaymentFailedEvent`.
     - `billing` Inbox consumes it. Executes `MarkInvoiceFailedCommand`. 
     - **Compensation Action**: If the failure puts the account past the grace period, `billing` publishes `SubscriptionSuspendedEvent`. The `identity` module's Inbox consumes this and executes `RevokeProductAccessCommand`, locking the users out of the specific module.

---

## 4. Warehouse Stock Transfer Saga
**Modules Involved**: `atlashub-commerce`, `atlashub-logistics`, `atlashub-accounting`
**Goal**: Safely move stock between branches, ensuring inventory is perfectly tracked while in transit and losses are financially reconciled.

### Step-by-Step Movement
1. **Initiation (`commerce`)**: Admin executes `InitiateStockTransferCommand`. 
2. **Source Deduction (`commerce`)**: Commerce acquires a **Pessimistic Lock** on the Source `Inventory`, deducts the stock, and places the `StockTransfer` record into a `DISPATCHED` state (virtually "In-Transit").
3. **Cross-Module Trigger (`commerce` -> `logistics`)**: Publishes `StockTransferRequestedEvent` via **Outbox**.
4. **Transit (`logistics`)**: 
   - `logistics` Inbox consumes event. Executes `CreateInternalShipmentCommand`. A Rider is assigned and goods are moved.
5. **Resolution Path**:
   - **Scenario A (Success)**: 
     - `logistics` executes `DeliverShipmentCommand` and publishes `StockTransferDeliveredEvent`. 
     - `commerce` Inbox consumes it. Executes `ReceiveStockTransferCommand`, acquiring a **Pessimistic Lock** on the Destination `Inventory` to add the stock.
   - **Scenario B (Failure - COMPENSATION)**:
     - `logistics` executes `FailShipmentCommand` (e.g., items lost in transit) and publishes `StockTransferFailedEvent`.
     - `commerce` Inbox consumes it. 
     - **Compensation Action**: Commerce marks the `StockTransfer` as `FAILED`. It **does not** add the stock to the destination. Instead, it publishes `InventoryLostEvent`.
     - `accounting` Inbox consumes `InventoryLostEvent` and executes `RecordJournalEntryCommand` (Debit Inventory Loss Expense, Credit Inventory Asset) to financially write off the lost goods.

---

## 5. Layaway (Customer Deposit) Saga
**Modules Involved**: `atlashub-commerce`, `atlashub-pay`
**Goal**: Handle partial payments for retail items accurately, reserving the physical item only when funds are confirmed.

### Step-by-Step Movement
1. **Initiation (`commerce`)**: Cashier executes `CreateLayawayCommand`. A `CustomerDeposit` record is created (`PENDING`).
2. **Cross-Module Trigger (`commerce` -> `pay`)**: Publishes `DepositPaymentRequestedEvent` for the partial amount via **Outbox**.
3. **Payment Processing (`pay`)**: `pay` Inbox consumes the event and executes `ProcessChargeCommand`.
4. **Resolution Path**:
   - **Scenario A (Success)**: 
     - `pay` publishes `PaymentSuccessfulEvent`. 
     - `commerce` Inbox consumes it. Executes `ActivateLayawayCommand`. Commerce acquires a **Pessimistic Lock** on `Inventory` to deduct/reserve the physical item, marking `CustomerDeposit` as `ACTIVE`.
   - **Scenario B (Failure - COMPENSATION)**:
     - `pay` publishes `PaymentFailedEvent`. 
     - `commerce` Inbox consumes it. 
     - **Compensation Action**: Commerce executes `VoidLayawayCommand`. The `CustomerDeposit` is marked `FAILED` and no inventory lock or deduction occurs.

---

## 6. Supply Refund (Return Outwards) Saga
**Modules Involved**: `atlashub-commerce`, `atlashub-logistics`, `atlashub-pay`
**Goal**: Return damaged/unsold goods to a supplier and programmatically recover funds.

### Step-by-Step Movement
1. **Initiation (`commerce`)**: Admin executes `InitiateSupplyRefundCommand`. Commerce acquires a **Pessimistic Lock** on `Inventory` and deducts the item from physical stock.
2. **Cross-Module Trigger (`commerce` -> `logistics`)**: Publishes `SupplierReturnRequestedEvent` via **Outbox**.
3. **Transit (`logistics`)**: `logistics` Inbox consumes event. Executes `CreateReturnShipmentCommand`.
4. **Resolution Path**:
   - **Scenario A (Success)**: 
     - Supplier receives goods. `logistics` publishes `ReturnDeliveredEvent`. 
     - `pay` Inbox consumes it. Executes `RequestSupplierRefundCommand` to pull funds from the supplier's virtual account into the corporate wallet.
   - **Scenario B (Failure - COMPENSATION)**:
     - Logistics fails to deliver (e.g., lost). `logistics` publishes `ReturnFailedEvent`.
     - `commerce` Inbox consumes it. 
     - **Compensation Action**: Commerce marks the `SupplyRefund` as `FAILED`. It publishes `InventoryLostEvent`.
     - `accounting` Inbox consumes it to execute `RecordJournalEntryCommand` writing off the lost inventory.
