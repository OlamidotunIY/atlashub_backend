# Accounting AP/AR Design (`atlashub-accounting` / `com.atlashub.accounting.ap` & `com.atlashub.accounting.ar`)

## Role & Purpose

The `ap` (Accounts Payable) and `ar` (Accounts Receivable) subpackages track money the organization owes suppliers and money owed to the organization by credit customers.

- **AP** is populated automatically when a `PurchaseOrderReceivedEvent` arrives from Commerce.
- **AR** is populated when a credit sale is completed in Commerce.
- Both are cleared via corresponding events from `atlashub-pay`.

These are event-driven sub-ledgers — they do not have their own independent payment workflows; they reconcile with Pay module events.

Gradle module: `atlashub-accounting`  
Package: `com.atlashub.accounting.ap` and `com.atlashub.accounting.ar`

---

## Domain Layer

### `SupplierInvoice` (Aggregate Root — Accounts Payable)

**Package:** `com.atlashub.accounting.ap.domain.entities`

```
SupplierInvoice
├── id              : Long
├── organizationId  : Long
├── supplierId      : Long
├── purchaseOrderId : Long
├── invoiceNumber   : String          ← supplier's invoice reference
├── amount          : Money
├── dueDate         : LocalDate
├── status          : ApStatus        ← UNPAID | PARTIALLY_PAID | PAID | DISPUTED
└── paidAt          : ZonedDateTime   ← nullable
```

**Business methods (on entity):**
- `markPaid(payoutReference)` → UNPAID/PARTIALLY_PAID → PAID; registers `SupplierInvoicePaidEvent`
- `dispute(reason)` → UNPAID → DISPUTED
- `resolveDispute()` → DISPUTED → UNPAID

---

### `CustomerReceivable` (Aggregate Root — Accounts Receivable)

**Package:** `com.atlashub.accounting.ar.domain.entities`

```
CustomerReceivable
├── id              : Long
├── organizationId  : Long
├── customerId      : Long
├── salesOrderId    : Long
├── amount          : Money
├── dueDate         : LocalDate
└── status          : ArStatus        ← OUTSTANDING | PARTIALLY_COLLECTED | COLLECTED | WRITTEN_OFF
```

**Business methods (on entity):**
- `recordPayment(amount)` — reduces outstanding balance; if `balance == 0` → COLLECTED; registers `ReceivableCollectedEvent`
- `writeOff(reason)` → OUTSTANDING/PARTIALLY_COLLECTED → WRITTEN_OFF; registers `ReceivableWrittenOffEvent`; posts GL write-off entry

---

### Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `SupplierInvoiceCreatedEvent` | Invoice created from PO received | `notifications` (payable alert) |
| `SupplierInvoicePaidEvent` | Supplier invoice marked paid | `notifications` |
| `ReceivableCollectedEvent` | Customer pays their debt | `notifications`, `analytics` |
| `ReceivableWrittenOffEvent` | Receivable written off | `accounting:gl` (Dr Bad Debt Expense, Cr AR) |

---

### Domain Exceptions

**AP — `com.atlashub.accounting.ap.domain.exceptions`:**
```java
public class SupplierInvoiceNotFoundException extends NotFoundException {
    public SupplierInvoiceNotFoundException(Long id) { super("Supplier invoice not found: " + id); }
}
public class InvoiceAlreadyPaidException extends ConflictException {
    public InvoiceAlreadyPaidException() { super("This supplier invoice has already been paid"); }
}
public class InvoiceDisputedException extends BusinessRuleException {
    public InvoiceDisputedException() { super("This invoice is under dispute and cannot be processed"); }
}
```

**AR — `com.atlashub.accounting.ar.domain.exceptions`:**
```java
public class ReceivableNotFoundException extends NotFoundException {
    public ReceivableNotFoundException(Long id) { super("Customer receivable not found: " + id); }
}
public class ReceivableAlreadyCollectedException extends ConflictException {
    public ReceivableAlreadyCollectedException() { super("This receivable has already been collected"); }
}
public class ReceivableWrittenOffException extends BusinessRuleException {
    public ReceivableWrittenOffException() { super("Cannot process a written-off receivable"); }
}
```

---

## Application Layer

### AP Commands — `com.atlashub.accounting.ap.application.commands`

#### `RecordSupplierInvoiceCommand`
```java
record RecordSupplierInvoiceCommand(Long organizationId, Long supplierId,
                                    Long purchaseOrderId, String invoiceNumber,
                                    Money amount, LocalDate dueDate)
```
**Handler:** `RecordSupplierInvoiceHandler` | **Response:** `Long invoiceId`  
**Invocation source:** `PurchaseOrderReceivedListener` (automatic) or controller (manual)

---

#### `MarkSupplierInvoicePaidCommand`
```java
record MarkSupplierInvoicePaidCommand(Long invoiceId, String payoutReference)
```
**Handler:** `MarkSupplierInvoicePaidHandler` | **Response:** `void`  
**Invocation source:** `PayoutCompletedListener` (automatic from pay module)  
**Flow:** Load `SupplierInvoice` → `invoice.markPaid(payoutReference)` → save → `SupplierInvoicePaidEvent` published

---

#### `DisputeSupplierInvoiceCommand`
```java
record DisputeSupplierInvoiceCommand(Long invoiceId, String reason)
```
**Handler:** `DisputeSupplierInvoiceHandler` | **Response:** `void`

---

### AP Queries — `com.atlashub.accounting.ap.application.queries`

#### `ListSupplierInvoicesQuery`
```java
record ListSupplierInvoicesQuery(Long organizationId, Long supplierId, ApStatus status)
```
**Handler:** `ListSupplierInvoicesHandler` | **Result:** `List<SupplierInvoiceResult>` (bounded per org/supplier)

---

### AR Commands — `com.atlashub.accounting.ar.application.commands`

#### `CreateCustomerReceivableCommand`
```java
record CreateCustomerReceivableCommand(Long organizationId, Long customerId,
                                       Long salesOrderId, Money amount, LocalDate dueDate)
```
**Handler:** `CreateCustomerReceivableHandler` | **Response:** `Long receivableId`  
**Invocation source:** `CreditSaleCompletedListener`

---

#### `RecordReceivablePaymentCommand`
```java
record RecordReceivablePaymentCommand(Long receivableId, Money amountPaid)
```
**Handler:** `RecordReceivablePaymentHandler` | **Response:** `void`  
**Invocation source:** Controller (finance manager manually records payment) or payment event

---

#### `WriteOffReceivableCommand`
```java
record WriteOffReceivableCommand(Long receivableId, String reason)
```
**Handler:** `WriteOffReceivableHandler` | **Response:** `void`  
**RBAC:** `@PreAuthorize("hasAuthority('accounting:journal:approve')")`

---

### AR Queries — `com.atlashub.accounting.ar.application.queries`

#### `GetOpenReceivablesQuery`
```java
record GetOpenReceivablesQuery(Long organizationId)
```
**Handler:** `GetOpenReceivablesHandler` | **Result:** `List<CustomerReceivableResult>` (bounded per org — operational view)

---

## Infrastructure Layer

### Persistence — AP

| JPA Entity | Table | Locking |
|---|---|---|
| `SupplierInvoiceJpaEntity` | `accounting_supplier_invoices` | `@Version` optimistic |

**Repository Adapter:** `SupplierInvoiceRepositoryAdapter` → `accounting_supplier_invoice_seq`

### Persistence — AR

| JPA Entity | Table | Locking |
|---|---|---|
| `CustomerReceivableJpaEntity` | `accounting_customer_receivables` | `@Version` optimistic |

**Repository Adapter:** `CustomerReceivableRepositoryAdapter` → `accounting_customer_receivable_seq`

### Listeners

**AP:**

#### `PurchaseOrderReceivedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-ap-po-received` |
| **Action** | Calls `RecordSupplierInvoiceHandler` to create AP record |

#### `PayoutCompletedListener`
| Topic | `pay-events` |
|---|---|
| **Group ID** | `accounting-ap-payout-completed` |
| **Action** | Calls `MarkSupplierInvoicePaidHandler` if payout reference matches an invoice |

**AR:**

#### `CreditSaleCompletedListener`
| Topic | `commerce-events` |
|---|---|
| **Group ID** | `accounting-ar-credit-sale` |
| **Action** | Calls `CreateCustomerReceivableHandler` |

---

## Presentation Layer

### Controller: `AccountingApController` — `/api/v1/accounting/payables`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/payables` | — | `?orgId&supplierId&status` | `List<SupplierInvoiceResult>` |
| `POST` | `/payables` | `accounting:accounts:create` | `RecordSupplierInvoiceRequest` | `Long` |
| `POST` | `/payables/{id}/dispute` | — | `DisputeInvoiceRequest` | `void` |

### Controller: `AccountingArController` — `/api/v1/accounting/receivables`

| Method | Path | RBAC | Request | Response |
|---|---|---|---|---|
| `GET` | `/receivables` | — | `?orgId` | `List<CustomerReceivableResult>` |
| `POST` | `/receivables/{id}/payment` | — | `RecordReceivablePaymentRequest` | `void` |
| `POST` | `/receivables/{id}/write-off` | `accounting:journal:approve` | `WriteOffRequest` | `void` |

---

## Complete File List

```
atlashub-accounting/src/main/java/com/atlashub/accounting/
├── ap/
│   ├── domain/
│   │   ├── entities/ [SupplierInvoice]
│   │   ├── events/ [SupplierInvoiceCreatedEvent, SupplierInvoicePaidEvent]
│   │   ├── exceptions/ [SupplierInvoiceNotFoundException, InvoiceAlreadyPaidException, InvoiceDisputedException]
│   │   ├── repositories/ [SupplierInvoiceRepository]
│   │   └── valueobject/ [ApStatus]
│   ├── application/
│   │   ├── commands/ [RecordSupplierInvoice, MarkSupplierInvoicePaid, DisputeSupplierInvoice]
│   │   └── queries/ [ListSupplierInvoices]
│   ├── infrastructure/
│   │   ├── messaging/listeners/ [PurchaseOrderReceivedListener, PayoutCompletedListener]
│   │   └── persistence/ [adapters, entities, mappers, repositories]
│   └── presentation/ [dto/, rest/AccountingApController]
└── ar/
    ├── domain/
    │   ├── entities/ [CustomerReceivable]
    │   ├── events/ [ReceivableCollectedEvent, ReceivableWrittenOffEvent]
    │   ├── exceptions/ [ReceivableNotFoundException, ReceivableAlreadyCollectedException, ReceivableWrittenOffException]
    │   ├── repositories/ [CustomerReceivableRepository]
    │   └── valueobject/ [ArStatus]
    ├── application/
    │   ├── commands/ [CreateCustomerReceivable, RecordReceivablePayment, WriteOffReceivable]
    │   └── queries/ [GetOpenReceivables]
    ├── infrastructure/
    │   ├── messaging/listeners/ [CreditSaleCompletedListener]
    │   └── persistence/ [adapters, entities, mappers, repositories]
    └── presentation/ [dto/, rest/AccountingArController]
```
