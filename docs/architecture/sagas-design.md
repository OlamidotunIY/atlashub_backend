# Sagas & Distributed Workflows

> **Source**: *Microservices Patterns* — Chris Richardson, Ch. 4 (Managing Transactions); *Enterprise Integration Patterns* — Hohpe & Woolf (Process Manager); *Designing Data-Intensive Applications* — Kleppmann, Ch. 9 (Consistency and Consensus).

---

## What Is a Saga?

A saga is a sequence of local transactions — each owned by a different bounded context — coordinated by domain events. Unlike a distributed transaction (2-phase commit), a saga does not hold locks across services. Each step commits locally and publishes an event. If a later step fails, compensating transactions undo the earlier committed steps.

**Sagas replace distributed ACID transactions in AtlasHub.** Because each module owns its own database, there is no shared transaction boundary. Sagas provide the only correct approach to multi-step cross-module consistency.

There are two saga styles:
- **Choreography**: Each step publishes an event; the next step listens and reacts. Simple, no central coordinator.
- **Orchestration**: A central saga orchestrator sends commands to participants and reacts to their outcomes.

AtlasHub uses **choreography** for simple, linear sagas (2–3 steps) and **orchestration** for complex, conditional sagas (4+ steps or branching failure handling).

---

## Saga 1: POS Checkout (Choreography)

**Steps:**
1. Commerce: Create `SalesOrder` (PENDING)
2. Commerce: Reserve stock in `Inventory`
3. Pay: Initialize charge → PAYMENT_PENDING
4a. Pay: Charge succeeds → `ChargeSuccessfulEvent`
   → Commerce: Deduct stock, complete sale → `PosSaleCompletedEvent`
   → Accounting: Post journal entry
   → Notifications: Send receipt
4b. Pay: Charge fails / times out → `ChargeFailedEvent`
   → Commerce: Release reserved stock, fail sale → `PosSaleFailedEvent`

**Compensations:**
- Stock reservation timeout (15 minutes): Scheduled job releases reservation → `PosSaleFailedEvent`
- Charge success received after order already failed: Idempotency guard on `CompletePosSaleUseCase` prevents double-processing; refund issued automatically

**State transition diagram:**
```
SalesOrder: PENDING → PAYMENT_PENDING → COMPLETED
                                    └→ FAILED

Inventory:  available → reserved → deducted (on success)
                               └→ available (on failure / compensation)
```

---

## Saga 2: Payroll Disbursement (Orchestration)

This is a complex saga because it involves a bulk operation with partial failure handling — some employees may be paid while others fail.

**Orchestrator**: `PayrollSagaOrchestrator` (a stateful component in the HR module)

**Steps:**
```
1. HR: ApprovePayroll
   → PayrollRun transitions APPROVED
   → PayrollApprovedEvent emitted

2. Pay: ExecuteBulkPayout (receives PayrollApprovedEvent)
   → Creates individual Payout per employee
   → Initiates bank transfers via Moniepoint/Paystack
   → As each transfer resolves:
       SUCCESS → marks Payslip DISBURSED
       FAIL    → marks Payslip FAILED, adds to retry queue
   → When all payouts settled: publishes BulkPayoutSettledEvent
     (carries: successful count, failed count, failed employee IDs)

3. HR: HandleBulkPayoutSettled (receives BulkPayoutSettledEvent)
   SUCCESS (all paid):  PayrollRun → DISBURSED → PayrollDisbursedEvent
   PARTIAL FAIL:        PayrollRun → PARTIALLY_DISBURSED → alert HR team
   TOTAL FAIL:          PayrollRun → APPROVED (reverted for retry)

4. Notifications: PayrollDisbursedEvent
   → SMS to each successfully paid employee
```

**Compensation**: There is no rollback for salary that was already paid. Compensation for over-payment or payment to a wrong account goes through manual correction (refund + re-disbursement), triggered by HR admin.

---

## Saga 3: Marketplace Order (Choreography)

When a customer buys from a vendor in marketplace mode:

```
1. Commerce: CreateOnlineOrder (SalesOrder PENDING, stock reserved)
2. Pay: InitializeCharge → PAYMENT_PENDING
3a. SUCCESS: ChargeSuccessfulEvent
   → Pay: Apply SplitRule
       - Deduct AtlasHub platform fee (e.g., 1.5%)
       - Post remainder to Split Holding Account
       - Queue Payout for vendor share
   → Commerce: Complete sale, deduct stock
   → Logistics: Create shipment
   → Accounting: Post revenue entries
   → Notifications: Confirm order to customer, notify vendor
3b. FAIL: ChargeFailedEvent → Commerce releases stock, marks order FAILED
```

**Split Rule Application** is a synchronous operation within Pay — no saga step needed. The fund routing happens atomically within `ProcessSplitUseCase`.

---

## Saga 4: Stock Transfer (Choreography)

```
1. Commerce: RequestStockTransfer
   → StockTransferRequestedEvent
   → Commerce: Deduct stock from source outlet immediately

2. Logistics: Create Shipment (StockTransferRequestedEvent listener)
   → ShipmentCreatedEvent

3. Logistics: MarkDelivered (rider confirms POD)
   → ShipmentDeliveredEvent

4. Commerce: Add stock to destination outlet (ShipmentDeliveredEvent listener)
   → StockTransferReceivedEvent

5. Pay: Post inter-outlet ledger entry (StockTransferReceivedEvent listener)
6. Accounting: Post journal entry (StockTransferReceivedEvent listener)
```

**Compensation — transfer goes missing / undelivered:**
```
Logistics: MarkFailed → ShipmentFailedEvent
→ Commerce: Restore stock to source outlet (compensation)
→ Notifications: Alert ops team
```

---

## Saga 5: KYC Approval (Choreography)

```
1. Compliance: Organization submits compliance form
   → ComplianceSubmittedEvent
   → Admin: Create KycReviewTask

2. Admin: Compliance officer approves
   → KycApprovedEvent
   → Compliance: Transition to APPROVED → OrganizationComplianceApprovedEvent

3. Pay: Issue virtual accounts (OrganizationComplianceApprovedEvent)
   → Create NUBAN via Anchor for org
   → VirtualAccountIssuedEvent

4. Billing: Activate platform access (OrganizationComplianceApprovedEvent)
   → Subscription moves to ACTIVE if payment already received

5. Notifications: Welcome email with account details
```

---

## Failure Isolation Rule

Each saga step must handle the case where a preceding event was never received or was lost. Defensive checks in every `execute()` method:

```java
// In CompletePosSaleUseCase — handles ChargeSuccessfulEvent
SalesOrder order = orderRepository.findByChargeReference(command.chargeReference())
    .orElseThrow(() -> new BusinessRuleException(CommerceErrorCode.ORDER_NOT_FOUND));

// Guard: idempotent — if already completed, skip without error
if (order.getStatus() == OrderStatus.COMPLETED) {
    log.info("Order {} already completed — idempotent skip", order.getId());
    return null;
}

// Guard: only complete if in the right state
if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
    throw new BusinessRuleException(CommerceErrorCode.INVALID_ORDER_STATE,
        "Cannot complete order in state: " + order.getStatus());
}
```

---

## Saga Timeout Handling

For saga steps that wait on external systems (payment gateway, rider), timeouts are handled by scheduled jobs:

```java
// In Commerce: runs every 5 minutes
@Scheduled(cron = "*/5 * * * *")
@Transactional
public void expireStalePaymentPendingOrders() {
    ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
    List<SalesOrder> stale = orderRepository
        .findByStatusAndStatusChangedAtBefore(OrderStatus.PAYMENT_PENDING, cutoff);

    stale.forEach(order -> {
        order.failPayment("Payment timeout — no gateway confirmation received");
        // save() triggers outbox → PosSaleFailedEvent → Inventory releases stock
        orderRepository.save(order);
    });
}
```
