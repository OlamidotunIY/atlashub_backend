# Sagas & Distributed Workflows

> **Source**: *Microservices Patterns* — Chris Richardson, Ch. 4 (Managing Transactions); *Enterprise Integration Patterns* — Hohpe & Woolf (Process Manager); *Designing Data-Intensive Applications* — Kleppmann, Ch. 9 (Consistency and Consensus).

---

## What Is a Saga?

A saga is a sequence of local transactions — each owned by a different bounded context — coordinated by domain events. Unlike a distributed transaction (2-phase commit), a saga does not hold locks across services. Each step commits locally and publishes an event. If a later step fails, compensating transactions undo the earlier committed steps.

**Sagas replace distributed ACID transactions in AtlasHub.** Because each module owns its own database schema, there is no shared transaction boundary. Sagas provide the only correct approach to multi-step cross-module consistency.

### Style Used in AtlasHub

AtlasHub uses **choreography-based sagas exclusively**. There is no central saga orchestrator class. Each module listens for domain events on Kafka, invokes its own Handler, and publishes the next domain event. The "saga" is the emergent chain of these event→handler→event hops.

```
Module A: Handler publishes EventA
    ↓ (Kafka topic)
Module B: Listener receives EventA → invokes HandlerB → publishes EventB
    ↓ (Kafka topic)
Module C: Listener receives EventB → invokes HandlerC → publishes EventC
    ...
```

Compensation works in reverse: a failure event triggers compensating handlers in previously committed modules.

---

## Saga 1: Commerce Checkout (Choreography)

### Trigger
`POST /api/v1/commerce/pos/checkout` — cashier completes a sale.

### Steps

```
1. Commerce: CreateSalesOrderHandler
   → SalesOrder created (PENDING)
   → Stock reservation placed (Inventory)
   → SalesOrderCreatedEvent published

2. Pay: InitializeChargeHandler (listens on SalesOrderCreatedEvent)
   → Charge record created (PAYMENT_PENDING)
   → Payment gateway called
   → ChargeInitializedEvent published

3a. SUCCESS PATH — Pay: HandleChargeSuccessHandler (listens on gateway webhook)
    → Charge transitions to SUCCESSFUL
    → ChargeSuccessfulEvent published
    → Commerce: CompletePosSaleHandler (listens on ChargeSuccessfulEvent)
        → SalesOrder → COMPLETED
        → Stock permanently deducted
        → PosSaleCompletedEvent published
    → Accounting: PostSaleJournalEntryHandler (listens on PosSaleCompletedEvent)
    → Notifications: SendReceiptHandler (listens on PosSaleCompletedEvent)
    → Analytics: UpdateDailySalesProjectionHandler (listens on PosSaleCompletedEvent)

3b. FAILURE PATH — Pay: HandleChargeFailedHandler (listens on gateway webhook or timeout)
    → Charge transitions to FAILED
    → ChargeFailedEvent published
    → Commerce: FailPosSaleHandler (listens on ChargeFailedEvent)
        → SalesOrder → FAILED
        → Stock reservation released
        → PosSaleFailedEvent published
```

### Compensation
- **Stock reservation timeout (15 minutes)**: `StockReservationExpiryScheduler` in Commerce releases reservation → publishes `PosSaleFailedEvent`.
- **Charge success received after order already failed**: `CompletePosSaleHandler` is idempotent — if `SalesOrder.status == COMPLETED`, skip without error. If `FAILED`, issue automatic refund via Pay.

### State Transitions
```
SalesOrder:  PENDING → PAYMENT_PENDING → COMPLETED
                                     └→ FAILED

Inventory:   available → reserved → deducted (on success)
                                └→ available (on failure / compensation)
```

### Modules Involved
`atlashub-commerce` · `atlashub-pay` · `atlashub-accounting` · `atlashub-analytics` · `atlashub-platform:notifications`

---

## Saga 2: HR Payroll Disbursement (Choreography)

### Trigger
`ApprovePayrollRunHandler` — checker approves a payroll run.

### Steps

```
1. HR: ApprovePayrollRunHandler
   → PayrollRun → APPROVED (maker-checker: SelfApprovalNotAllowedException if same user)
   → repository.save() → PayrollApprovedEvent published

2. Pay: ExecuteBulkPayoutHandler (listens on PayrollApprovedEvent)
   → Creates one Payout record per employee
   → Initiates bank transfers via Moniepoint/Paystack for each
   → As each transfer resolves:
       SUCCESS → marks individual Payslip as DISBURSED
                 → PayslipDisbursedEvent published
       FAIL    → marks Payslip as FAILED, adds to retry queue
   → When all payouts have settled:
       → BulkPayoutSettledEvent published
         (carries: successCount, failCount, failedEmployeeIds)

3. HR: HandleBulkPayoutSettledHandler (listens on BulkPayoutSettledEvent)
   → ALL SUCCESS:     PayrollRun → DISBURSED → PayrollDisbursedEvent published
   → PARTIAL FAILURE: PayrollRun → PARTIALLY_DISBURSED → alert HR team
   → TOTAL FAILURE:   PayrollRun → APPROVED (reverted for retry)

4. Notifications: (listens on PayrollDisbursedEvent)
   → SMS to each successfully paid employee: "Your salary of ₦X has been credited"
```

### Compensation
There is no automated rollback for salary already paid — money has left the wallet. Compensation for over-payment or wrong-account payment goes through manual correction:
1. HR admin raises a correction request
2. Pay: RecoverPayoutHandler creates a reclaim charge to the employee's account
3. A corrected PayrollRun is initiated

### Failure Isolation
- If Pay module is unavailable when `PayrollApprovedEvent` arrives, the Kafka consumer retries. The outbox for `PayrollApprovedEvent` ensures it is not lost.
- Partial disbursement leaves `PayrollRun` in `PARTIALLY_DISBURSED` until a re-run addresses failed employees.

### Modules Involved
`atlashub-platform:hr` · `atlashub-pay` · `atlashub-platform:notifications` · `atlashub-accounting`

---

## Saga 3: Billing Subscription (Choreography)

### Trigger
`InvoiceGeneratedEvent` — billing module generates a new subscription invoice.

### Steps

```
1. Billing: GenerateSubscriptionInvoiceHandler
   → Invoice created (UNPAID)
   → InvoiceGeneratedEvent published
   → Notifications: sends invoice email to organization owner

2. Pay: InitializeSubscriptionChargeHandler (listens on InvoiceGeneratedEvent)
   → Charge created against organization's operating wallet
   → ChargeInitializedEvent published

3a. SUCCESS — Pay: HandleChargeSuccessHandler (listens on gateway webhook)
    → Charge → SUCCESSFUL
    → ChargeSuccessfulEvent published
    → Billing: MarkInvoicePaidHandler (listens on ChargeSuccessfulEvent where purpose=SUBSCRIPTION)
        → Invoice → PAID
        → InvoicePaidEvent published
    → Billing: ActivateSubscriptionHandler (listens on InvoicePaidEvent)
        → Subscription → ACTIVE (or renewal date extended)
        → SubscriptionRenewedEvent published
    → Notifications: sends renewal confirmation email

3b. FAILURE — Pay: HandleChargeFailedHandler
    → Charge → FAILED
    → ChargeFailedEvent published
    → Billing: HandleSubscriptionPaymentFailedHandler (listens on ChargeFailedEvent)
        → Invoice → PAYMENT_FAILED
        → Retry scheduled (3 attempts over 3 days)
        → If all retries exhausted: InvoiceOverdueEvent published
    → IAM: SuspendMembersHandler (listens on InvoiceOverdueEvent)
        → Organization members' access suspended
        → SubscriptionSuspendedEvent published
    → Notifications: sends suspension warning email
```

### Compensation
- **Grace period**: After first payment failure, org retains access for 3 days while retries run.
- **Reinstatement**: When payment eventually succeeds (manual retry or auto), `ActivateSubscriptionHandler` restores access.

### Modules Involved
`atlashub-billing` · `atlashub-pay` · `atlashub-platform:iam` · `atlashub-platform:notifications`

---

## Saga 4: Stock Transfer (Choreography)

```
1. Commerce: RequestStockTransferHandler
   → Stock deducted from source outlet immediately
   → StockTransferRequestedEvent published

2. Logistics: CreateStockTransferShipmentHandler (listens on StockTransferRequestedEvent)
   → Shipment created
   → ShipmentCreatedEvent published

3. Logistics: MarkShipmentDeliveredHandler (rider confirms POD)
   → Shipment → DELIVERED
   → ShipmentDeliveredEvent published

4. Commerce: ReceiveStockTransferHandler (listens on ShipmentDeliveredEvent where type=STOCK_TRANSFER)
   → Stock added to destination outlet
   → StockTransferReceivedEvent published

5. Accounting: PostStockTransferJournalEntryHandler (listens on StockTransferReceivedEvent)
```

**Compensation — transfer goes missing / undelivered:**
```
Logistics: MarkShipmentFailedHandler → ShipmentFailedEvent
    → Commerce: ReverseStockTransferHandler
        → Stock restored to source outlet
    → Notifications: alert operations team
```

---

## Saga 5: KYC / Compliance Approval (Choreography)

```
1. Compliance: SubmitComplianceHandler
   → ComplianceRecord → SUBMITTED
   → ComplianceSubmittedEvent published
   → Admin: CreateKycReviewTaskHandler creates a review task

2. Admin: ApproveComplianceHandler
   → KycApprovedEvent published
   → Compliance: TransitionToApprovedHandler
       → ComplianceRecord → APPROVED
       → OrganizationComplianceApprovedEvent published

3. Pay: IssueVirtualAccountHandler (listens on OrganizationComplianceApprovedEvent)
   → NUBAN created via Anchor
   → VirtualAccountIssuedEvent published

4. Billing: ActivatePlatformAccessHandler (listens on OrganizationComplianceApprovedEvent)
   → Subscription → ACTIVE if payment already received

5. Notifications: Welcome email with NUBAN details
```

---

## Failure Isolation Rule

Each saga step must handle the case where a preceding event was never received or was received more than once. Every Handler is idempotent — duplicate event delivery produces no side effect.

```java
// In CompletePosSaleHandler — handles ChargeSuccessfulEvent
SalesOrder order = orderRepository.findByChargeReference(command.chargeReference())
    .orElseThrow(() -> new SalesOrderNotFoundException(command.chargeReference()));

// Guard: idempotent — if already completed, skip without error
if (order.getStatus() == OrderStatus.COMPLETED) {
    log.info("Order {} already completed — idempotent skip", order.getId());
    return null;
}

// Guard: only complete if in the right state
if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
    throw new InvalidSalesOrderStateException(
        "Cannot complete order in state: " + order.getStatus());
}
```

---

## Saga Timeout Handling

For saga steps that wait on external systems (payment gateway, rider), timeouts are handled by scheduled jobs.

```java
// In Commerce — StockReservationExpiryScheduler
// Runs every 5 minutes — cron "*/5 * * * *"
@Component
public class StockReservationExpiryScheduler {

    private final SalesOrderRepository orderRepository;
    private final FailPosSaleHandler failHandler;

    @Scheduled(cron = "*/5 * * * *")
    public void expireStaleReservations() {
        ZonedDateTime cutoff = ZonedDateTime.now().minusMinutes(15);
        List<SalesOrder> stale = orderRepository
            .findByStatusAndStatusChangedAtBefore(OrderStatus.PAYMENT_PENDING, cutoff);

        stale.forEach(order -> {
            order.failPayment("Payment timeout — no gateway confirmation received");
            orderRepository.save(order);  // publishes PosSaleFailedEvent via outbox
        });
    }
}
```

---

## How to Implement a New Saga

When implementing a new multi-module workflow, follow this pattern:

### 1. Identify the steps
Map each step to a single module and a single local transaction. A step = one Handler invocation.

### 2. Define the events
Each step publishes exactly one domain event on success. The event triggers the next step.

### 3. Implement the listener
```java
@Component
public class MyModuleStepTwoListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "mymodule-saga-step2";

    @PostConstruct
    public void init() { registerSubscription("StepOneCompletedEvent", GROUP_ID); }

    @KafkaListener(topics = "other-module-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "StepOneCompletedEvent", StepOneCompletedEvent.class,
            log, GROUP_ID,
            e -> e instanceof TimeoutException,
            event -> handler.execute(new StepTwoCommand(
                event.payload().entityId(),
                event.payload().relevantField()
            )));
    }
}
```

### 4. Implement the handler
```java
@Component
public class StepTwoHandler extends Command<StepTwoCommand, Void> {

    @Override
    public Void execute(StepTwoCommand command) {
        MyEntity entity = repository.findById(command.entityId())
            .orElseThrow(() -> new EntityNotFoundException(command.entityId()));

        if (entity.getStatus() == ExpectedStatus.ALREADY_DONE) return null; // idempotent

        entity.performStepTwo(command.relevantField()); // registers StepTwoCompletedEvent
        repository.save(entity); // publishes event via outbox
        return null;
    }
}
```

### 5. Add inbox idempotency
The `BaseKafkaEventListener.processEventIfMatches()` method checks `EventDeliveryTracker` before invoking the handler. Duplicate Kafka delivery will not double-process.

### 6. Design the compensation
For each step, define a compensating handler that undoes the step if called. Compensating handlers are triggered by failure events.

### 7. Add timeout recovery
If the saga can stall (e.g., waiting on external payment gateway), add a scheduled job that detects stale records and either retries or compensates.

---

## Domain Events Cross-Reference

| Saga | Event | Published By | Consumed By |
|---|---|---|---|
| Checkout | `SalesOrderCreatedEvent` | Commerce | Pay |
| Checkout | `ChargeSuccessfulEvent` | Pay | Commerce, Accounting, Analytics, Notifications |
| Checkout | `ChargeFailedEvent` | Pay | Commerce |
| Checkout | `PosSaleCompletedEvent` | Commerce | Accounting, Analytics, Notifications |
| Payroll | `PayrollApprovedEvent` | HR | Pay |
| Payroll | `BulkPayoutSettledEvent` | Pay | HR |
| Payroll | `PayrollDisbursedEvent` | HR | Notifications, Accounting, Analytics |
| Subscription | `InvoiceGeneratedEvent` | Billing | Pay, Notifications |
| Subscription | `InvoicePaidEvent` | Billing | Billing (ActivateSubscription), Notifications |
| Subscription | `InvoiceOverdueEvent` | Billing | IAM (suspend members), Notifications |
| Subscription | `SubscriptionRenewedEvent` | Billing | Notifications |
| Stock Transfer | `StockTransferRequestedEvent` | Commerce | Logistics |
| Stock Transfer | `ShipmentDeliveredEvent` | Logistics | Commerce, Accounting |
| KYC | `ComplianceSubmittedEvent` | Compliance | Admin |
| KYC | `OrganizationComplianceApprovedEvent` | Compliance | Pay, Billing, Notifications |
