# AtlasHub — Complete End-to-End User & Platform Flow

This document traces the **complete lifecycle** of an organization and its members on the AtlasHub platform.
Every cross-module interaction is shown as an async event chain. No sync cross-module write calls appear here.

---

## 1. Organization Registration & Bootstrap

### 1.1 Developer Registers a New Organization (via API)

```
POST /api/v1/organizations/register
Body: { businessName, ownerFirstName, ownerLastName, email, password, phone, businessType }
```

**Handler**: `RegisterOrganizationHandler` (accounts module)

**Step-by-step:**
1. `RegisterOrganizationHandler` validates the request.
2. Creates `User` aggregate (owner).
3. Creates `Organization` aggregate (status = `PENDING_COMPLIANCE`).
4. Saves both via `UserRepository` and `OrganizationRepository`.
5. `User.create()` registers `UserCreatedEvent`.
6. `Organization.create()` registers `OrganizationCreatedEvent`.
7. `repository.save()` publishes both events via outbox to `accounts-events` topic.

**Event chain from `UserCreatedEvent` (topic: `accounts-events`):**
```
UserCreatedEvent.payload
├── userId         : Long
├── email          : String
├── firstName      : String
├── lastName       : String
└── createdAt      : ZonedDateTime

→ auth: UserCreatedListener
      Creates AuthAccount (hashed password, emailVerified=false)
      Sends OTP verification email via NotificationPort
```

**Event chain from `OrganizationCreatedEvent` (topic: `accounts-events`):**
```
OrganizationCreatedEvent.payload
├── organizationId : Long
├── ownerId        : Long
├── businessName   : String
├── businessType   : String
└── createdAt      : ZonedDateTime

→ iam: OrganizationCreatedListener
      Creates OWNER OrganizationMember record for the owner user
      Seeds built-in roles (OWNER, ADMIN, MEMBER)

→ billing: OrganizationCreatedListener
      Creates Subscription (status=TRIAL, trialEndsAt=now+30days)

→ compliance: OrganizationCreatedListener
      Creates ComplianceRecord (status=NOT_STARTED)

→ pay:accounts: [NOT triggered yet — waits for compliance approval]
```

**API Response:**
```json
{ "organizationId": 123, "userId": 456, "status": "PENDING_EMAIL_VERIFICATION" }
```

---

### 1.2 Owner Verifies Email

```
POST /api/v1/auth/verify-email
Body: { otp }
```

**Handler**: `VerifyEmailHandler` (auth module)

1. Validates OTP from Redis.
2. `authAccount.recordEmailVerified()` → registers `AuthEmailVerifiedEvent`.
3. Saves → event published to `auth-events`.

No cross-module cascade. Email verification is auth-internal.

---

### 1.3 Owner Logs In

```
POST /api/v1/auth/login
Body: { email, password }
```

**Handler**: `LoginHandler` (auth module)

1. Loads `AuthAccount` by email.
2. Verifies password hash (BCrypt).
3. Checks account is not locked.
4. `authAccount.recordSuccessfulLogin(ip)` — updates `lastLoginAt`, `lastLoginIp`.
5. Issues JWT (contains `userId`, `organizationId`, `permissions[]`).
6. Stores refresh token in Redis session store.

**Response:**
```json
{ "accessToken": "...", "refreshToken": "...", "expiresIn": 900 }
```

All subsequent API calls include `Authorization: Bearer <accessToken>`.

---

## 2. KYC / Compliance

### 2.1 Owner Submits KYC Documents

```
POST /api/v1/compliance/business-profile   → UpdateBusinessProfileHandler
POST /api/v1/compliance/contact-info       → UpdateContactInfoHandler
POST /api/v1/compliance/owner-identity     → UpdateOwnerIdentityHandler
POST /api/v1/compliance/settlement-account → UpdateSettlementAccountHandler
POST /api/v1/compliance/service-agreement  → AcceptServiceAgreementHandler
POST /api/v1/compliance/submit             → SubmitComplianceHandler
```

**SubmitComplianceHandler** step-by-step:
1. Validates all 5 steps are complete.
2. `complianceRecord.submit()` → status = `SUBMITTED` → registers `ComplianceSubmittedEvent`.
3. Saves → event published to `compliance-events`.

**Event chain from `ComplianceSubmittedEvent`:**
```
ComplianceSubmittedEvent.payload
├── complianceRecordId : Long
├── organizationId     : Long
├── businessName       : String
└── submittedAt        : ZonedDateTime

→ admin: ComplianceSubmittedListener
      Creates KycReviewTask and assigns to available admin reviewer
      Sends internal Slack/email notification to AtlasHub compliance team
```

---

### 2.2 AtlasHub Admin Reviews & Approves KYC

**AtlasHub internal staff** logs into admin dashboard.

```
POST /api/v1/admin/kyc/approve
Body: { complianceRecordId, notes }
```

**Handler**: `ApproveKycHandler` (admin module)

1. Loads `KycReviewTask`.
2. Marks task resolved.
3. Publishes `KycApprovedEvent` to `admin-events` topic.

**Event chain from `KycApprovedEvent`:**
```
KycApprovedEvent.payload
├── complianceRecordId : Long
├── organizationId     : Long
├── reviewerId         : Long
└── approvedAt         : ZonedDateTime

→ compliance: KycApprovedListener
      complianceRecord.approve() → status = APPROVED
      Publishes OrganizationComplianceApprovedEvent to compliance-events

→ notifications: ComplianceNotificationListener
      Sends "KYC Approved" email to org owner
```

**Event chain from `OrganizationComplianceApprovedEvent`:**
```
OrganizationComplianceApprovedEvent.payload
├── organizationId : Long
├── orgAdminEmail  : String
├── businessName   : String
└── approvedAt     : ZonedDateTime

→ pay:accounts: OrganizationComplianceApprovedListener
      IssueVirtualAccountHandler called
      Calls Anchor API → creates virtual NUBAN for the org
      Anchor responds asynchronously (webhook)

→ billing: OrganizationComplianceApprovedListener
      Activates subscription if PENDING_COMPLIANCE
      Organization now has full platform access
```

---

### 2.3 Virtual Account Activated (Anchor Webhook)

Anchor issues the NUBAN and sends a webhook to AtlasHub.

**Handler**: `AnchorWebhookAdapter` → `ActivateVirtualAccountHandler` (pay:accounts)

1. Validates Anchor webhook signature.
2. `virtualAccount.activate(nuban, bankName, anchorAccountId)` → registers `VirtualAccountActivatedEvent`.
3. Saves → event published to `pay-events`.

**Event chain from `VirtualAccountActivatedEvent`:**
```
VirtualAccountActivatedEvent.payload
├── virtualAccountId : Long
├── organizationId   : Long
├── nuban            : String
├── bankName         : String
├── accountName      : String
├── currency         : String
├── orgAdminEmail    : String
└── activatedAt      : ZonedDateTime

→ pay:ledger: VirtualAccountActivatedListener
      Creates 6 double-entry ledger accounts for the org:
        OPERATING_CASH, SUSPENSE, CUSTOMER_DEPOSITS,
        REVENUE, SPLIT_PAYABLE, PLATFORM_FEE_PAYABLE

→ notifications: PayEventListener
      Sends "Your business wallet is ready" email to org owner
      Includes NUBAN and bank name
```

---

## 3. Team Management

### 3.1 Inviting a Staff Member

```
POST /api/v1/iam/invitations
Body: { email, roleId }
```

**Handler**: `CreateInvitationHandler` (iam module)

1. Creates `Invitation` (status = `PENDING`, generates secure token).
2. Saves → registers `InvitationCreatedEvent`.

**Event chain from `InvitationCreatedEvent`:**
```
InvitationCreatedEvent.payload
├── invitationId   : Long
├── organizationId : Long
├── email          : String
├── roleId         : Long
├── token          : String   ← secure URL token
└── expiresAt      : ZonedDateTime

→ notifications: (consumed by ComplianceNotificationListener or dedicated InvitationListener)
      Sends invitation email with signup link
```

---

### 3.2 Invited User Accepts Invitation

```
POST /api/v1/iam/invitations/accept
Body: { token, firstName, lastName, password }
```

**Handler**: `AcceptInvitationHandler` (iam module)

1. Validates token and expiry.
2. Creates `User` in accounts (or links existing user).
3. Creates `OrganizationMember` with the specified role.
4. Publishes `InvitationAcceptedEvent` → `iam-events`.

**Event chain:**
```
InvitationAcceptedEvent.payload
├── userId         : Long
├── organizationId : Long
├── email          : String
└── acceptedAt     : ZonedDateTime

→ auth: InvitationAcceptedListener
      If new user: creates AuthAccount, sets emailVerified=true
      If existing user: links to organization

→ hr: MemberJoinedListener (from MemberJoinedEvent which iam publishes after membership creation)
      Creates draft Employee profile
```

---

### 3.3 Employee Onboarding (HR)

```
POST /api/v1/hr/employees
Body: { userId, jobTitle, department, startDate, salaryGradeId, bankAccountNumber, bankCode }
```

**Handler**: `OnboardEmployeeHandler` (hr:staff module)

1. Creates `Employee` aggregate (status = `ACTIVE`).
2. Saves → registers `EmployeeOnboardedEvent`.

**Event chain from `EmployeeOnboardedEvent`:**
```
EmployeeOnboardedEvent.payload
├── employeeId     : Long
├── organizationId : Long
├── userId         : Long
├── email          : String
├── firstName      : String
├── jobTitle       : String
└── startDate      : LocalDate

→ notifications: HrPayrollListener
      Sends "Welcome to [Business]" onboarding email to employee
```

---

## 4. Subscription & Billing

### 4.1 Trial Period

During the 30-day trial, the org has full access to all advanced features. `EntitlementQueryPort.hasActiveSubscription(orgId)` returns `true` for all modules.

### 4.2 Trial Expires — Invoice Generated

**Scheduler**: `InvoiceGenerationScheduler` runs on the 1st of each month at 00:05.

1. Finds all subscriptions where `nextBillingDate <= today`.
2. Calls `GenerateSubscriptionInvoiceHandler` for each.
3. Invoice created (status = `UNPAID`).
4. Publishes `InvoiceGeneratedEvent` → `billing-events`.

**Event chain:**
```
InvoiceGeneratedEvent.payload
├── invoiceId      : Long
├── organizationId : Long
├── amount         : BigDecimal
├── currency       : String
├── dueDate        : LocalDate
└── orgAdminEmail  : String

→ notifications: BillingNotificationListener
      Sends "Your invoice is ready" email with amount and due date
```

### 4.3 Org Pays Invoice

Org uses their dashboard to initiate a payment (charged via Paystack to AtlasHub's collection account).

```
POST /api/v1/pay/charges
Body: { invoiceId, amount, paymentMethod: CARD }
```

**Handler**: `InitializeChargeHandler` (pay:charges) with `sourceSystem=PLATFORM_BILLING`.

On successful charge → `ChargeSuccessfulEvent` published.

**Event chain from `ChargeSuccessfulEvent` (sourceSystem=PLATFORM_BILLING):**
```
→ billing: PaymentSuccessfulListener
      MarkInvoicePaidHandler called
      Subscription continues uninterrupted

→ pay:ledger: ChargeSuccessfulListener
      Posts journal entry: Dr Suspense → Cr Revenue
```

---

## 5. Catalogue & Product Setup

### 5.1 Creating Products

```
POST /api/v1/commerce/products
Body: { name, sku, categoryId, price, unit, isService, outletIds[] }
```

**Handler**: `CreateProductHandler` (commerce:catalog subpackage)

1. Creates `Product` aggregate.
2. Saves → registers `ProductCreatedEvent`.

**Event chain from `ProductCreatedEvent`:**
```
ProductCreatedEvent.payload
├── productId      : Long
├── organizationId : Long
├── sku            : String
├── isService      : boolean
├── outletIds      : Long[]
└── createdAt      : ZonedDateTime

→ commerce:inventory: ProductCreatedListener
      Creates Inventory record (quantity=0) for each outlet where isService=false
```

### 5.2 Stocking Inventory

```
POST /api/v1/commerce/inventory/adjust
Body: { productId, outletId, quantity, adjustmentType: STOCK_IN, reference }
```

**Handler**: `AdjustStockHandler` (commerce:inventory)

Increases `Inventory.quantity`. No cross-module event for simple stock adjustment.

---

## 6. Outlet & Till Management

### 6.1 Creating an Outlet

```
POST /api/v1/accounts/outlets
Body: { name, address, type: STORE | RESTAURANT | WAREHOUSE }
```

**Handler**: `CreateOutletHandler` (accounts module)

1. Creates `Outlet` aggregate.
2. Publishes `OutletCreatedEvent` → `accounts-events`.

**Event chain from `OutletCreatedEvent`:**
```
OutletCreatedEvent.payload
├── outletId       : Long
├── organizationId : Long
├── name           : String
├── type           : String
└── createdAt      : ZonedDateTime

→ pay:ledger: OutletCreatedListener
      Creates TILL ledger account for the outlet
      (used for tracking physical cash float)
```

### 6.2 Opening a Till

```
POST /api/v1/commerce/tills/open
Body: { outletId, cashierId, openingFloat }
```

**Handler**: `OpenTillHandler` (commerce:storefront)

1. Creates `TillSession` (status = `OPEN`).
2. Registers `TillOpenedEvent`.

**Event chain from `TillOpenedEvent`:**
```
TillOpenedEvent.payload
├── tillSessionId  : Long
├── organizationId : Long
├── outletId       : Long
├── cashierId      : Long
├── openingFloat   : BigDecimal
└── openedAt       : ZonedDateTime

→ pay:ledger: TillOpenedListener
      Posts Dr TILL account with opening float amount
      (records that cash was put into the till)
```

### 6.3 Closing a Till

```
POST /api/v1/commerce/tills/{id}/close
Body: { closingCash, notes }
```

**Handler**: `CloseTillHandler`

1. `tillSession.close(closingCash)` → registers `TillClosedEvent`.

**Event chain from `TillClosedEvent`:**
```
TillClosedEvent.payload
├── tillSessionId  : Long
├── organizationId : Long
├── outletId       : Long
├── cashierId      : Long
├── openingFloat   : BigDecimal
├── closingCash    : BigDecimal
├── totalSales     : BigDecimal
├── variance       : BigDecimal   ← closingCash - (openingFloat + totalSales)
└── closedAt       : ZonedDateTime

→ pay:ledger: TillClosedListener
      Sweeps TILL account balance → Cr OPERATING_CASH
      Records cash variance as TILL_VARIANCE journal entry
```

---

## 7. POS Checkout (Card Payment)

### 7.1 Cashier Creates a Sales Order

Business's POS app calls:
```
POST /api/v1/commerce/orders
Body: { outletId, cashierId, items[]: { productId, quantity, unitPrice } }
```

**Handler**: `CreateSalesOrderHandler` (commerce:storefront)

1. Creates `SalesOrder` (status = `PENDING_PAYMENT`).
2. For each item: `inventoryService.reserveStock(productId, outletId, qty)` — **in-module call** (same Gradle module).
3. Generates a UUID `chargeReference`.
4. Registers `SalesOrderPaymentInitiatedEvent`.

**Event chain from `SalesOrderPaymentInitiatedEvent`:**
```
SalesOrderPaymentInitiatedEvent.payload
├── salesOrderId   : Long
├── organizationId : Long
├── outletId       : Long
├── chargeReference: String   ← UUID; idempotency key
├── amount         : BigDecimal
├── currency       : String
├── paymentMethod  : String   ← CARD | POS_TERMINAL
├── cashierId      : Long
└── initiatedAt    : ZonedDateTime

→ pay:charges: SalesOrderPaymentInitiatedListener
      InitializeChargeHandler called
      sourceSystem=COMMERCE_CHECKOUT, sourceReferenceId=salesOrderId
      Calls Paystack/Moniepoint API to initialize charge
      Returns checkoutUrl or sends terminal push
```

---

### 7.2 Customer Pays (Paystack Webhook Received)

Paystack sends a webhook when the card is charged successfully.

**Adapter**: `PaystackWebhookAdapter` → `ProcessPaystackWebhookHandler` (pay:charges)

1. Validates `X-Paystack-Signature` HMAC.
2. Matches `reference` to existing `Charge`.
3. `charge.markSuccessful(gatewayRef, response)` → registers `ChargeSuccessfulEvent`.
4. Saves → event published to `pay-events`.

**Event chain from `ChargeSuccessfulEvent`:**
```
ChargeSuccessfulEvent.payload
├── chargeId           : Long
├── organizationId     : Long
├── reference          : String
├── sourceSystem       : String   ← COMMERCE_CHECKOUT
├── sourceReferenceId  : String   ← salesOrderId
├── amount             : BigDecimal
├── currency           : String
├── gatewayReference   : String
├── succeededAt        : ZonedDateTime
└── customerEmail      : String   ← nullable

→ commerce:storefront: ChargeSuccessfulListener
      salesOrder.completePayment()
      Deducts reserved stock (reservation → actual deduction)
      Registers SalesOrderCompletedEvent (for receipt)
      Registers PosSaleCompletedEvent (for KOT if restaurant)

→ pay:ledger: ChargeSuccessfulListener
      Posts journal entry: Dr Suspense → Cr OPERATING_CASH

→ pay:tx-query: ChargeSuccessfulListener
      Updates TransactionProjection read model

→ accounting:gl: ChargeSuccessfulListener
      Posts Dr Cash/Receivable → Cr Revenue

→ notifications: PayEventListener
      If customerEmail present: sends payment receipt email
```

**Event chain from `SalesOrderCompletedEvent`:**
```
SalesOrderCompletedEvent.payload
├── salesOrderId   : Long
├── organizationId : Long
├── outletId       : Long
├── totalAmount    : BigDecimal
├── items[]        : [{ productId, quantity, unitPrice }]
└── completedAt    : ZonedDateTime

→ analytics: CommerceAnalyticsListener
      Updates RevenueProjection, SalesCountProjection

→ notifications: CommerceReceiptListener
      Sends POS receipt if customer email provided
```

**Event chain from `PosSaleCompletedEvent` (restaurant use case):**
```
PosSaleCompletedEvent.payload
├── salesOrderId   : Long
├── outletId       : Long
├── tableId        : Long   ← nullable
├── items[]        : [{ name, quantity }]
└── completedAt    : ZonedDateTime

→ SelectiveWebSocketBroadcaster
      Broadcasts KotReadyEvent to /topic/outlet/{outletId}/kitchen
      Kitchen display system receives the new order
```

---

### 7.3 Payment Fails

Paystack webhook indicates failure.

**Handler**: `ProcessPaystackWebhookHandler`

1. `charge.markFailed(reason)` → registers `ChargeFailedEvent`.

**Event chain from `ChargeFailedEvent`:**
```
ChargeFailedEvent.payload
├── chargeId           : Long
├── organizationId     : Long
├── reference          : String
├── sourceSystem       : String
├── sourceReferenceId  : String   ← salesOrderId
├── failureReason      : String
└── failedAt           : ZonedDateTime

→ commerce:storefront: ChargeFailedListener
      salesOrder.failPayment()
      Releases reserved stock back to available inventory
      SalesOrder status → PAYMENT_FAILED
```

---

## 8. Online Storefront Order (e-Commerce)

### 8.1 Customer Places Order via Business's Website

Business's website calls:
```
POST /api/v1/commerce/orders/online
Body: { customerId, items[], deliveryAddress, paymentMethod: CARD }
```

This follows the same checkout flow as POS (section 7) but with `paymentMethod=ONLINE` and a delivery address captured for logistics.

**Additional event chain after `SalesOrderCompletedEvent`:**
```
→ logistics:shipping: OnlineOrderCreatedListener
      CreateShipmentHandler called
      Creates Shipment record (status=PENDING_DISPATCH)
      Assigns rider when available
      Publishes ShipmentCreatedEvent → logistics-events
```

---

## 9. Credit Sale (Sell Now, Collect Later)

```
POST /api/v1/commerce/orders/credit
Body: { customerId, items[], dueDate }
```

**Handler**: `CreateCreditSaleHandler`

1. Validates `customer.creditLimit >= orderTotal`.
2. `customer.debitCreditAccount(amount)` — reduces available credit.
3. Creates `SalesOrder` (status = `CREDIT_EXTENDED`).
4. Registers `CreditSaleCompletedEvent`.

**Event chain from `CreditSaleCompletedEvent`:**
```
CreditSaleCompletedEvent.payload
├── salesOrderId   : Long
├── organizationId : Long
├── customerId     : Long
├── amount         : BigDecimal
├── dueDate        : LocalDate
└── completedAt    : ZonedDateTime

→ accounting:ap-ar: CreditSaleCompletedListener
      Creates AR (Accounts Receivable) record
      Dr Receivable → Cr Revenue

→ notifications: CommerceReceiptListener
      Sends credit sale confirmation to customer
```

When customer pays their credit balance:
```
POST /api/v1/commerce/customers/{id}/credit/repay
```
`accounting:ap-ar` marks AR as settled.

---

## 10. Layaway / Deposit

```
POST /api/v1/commerce/layaways
Body: { customerId, items[], depositAmount, targetDate }
```

**Handler**: `CreateLayawayHandler`

1. Collects deposit (via `pay:charges`).
2. Creates `Layaway` record.
3. Holds stock reservation until fully paid.

When fully paid → converts to `SalesOrder` → follows normal checkout flow (section 7.2 onwards).

---

## 11. Customer Return & Refund

### 11.1 Logistics Collects Return Shipment

```
POST /api/v1/logistics/returns
Body: { salesOrderId, outletId, customerAddress, items[] }
```

**Handler**: `CreateReturnShipmentHandler` (logistics:returns)

1. Creates `ReturnShipment` (status = `IN_TRANSIT`).
2. Assigns rider for collection.

When rider delivers return to outlet:
```
POST /api/v1/logistics/returns/{id}/receive
```

**Handler**: `ReceiveReturnShipmentHandler`

1. `returnShipment.markReceived()` → registers `ReturnShipmentReceivedEvent`.

**Event chain from `ReturnShipmentReceivedEvent`:**
```
ReturnShipmentReceivedEvent.payload
├── returnShipmentId : Long
├── salesOrderId     : Long
├── organizationId   : Long
├── outletId         : Long
├── items[]          : [{ productId, quantity }]
└── receivedAt       : ZonedDateTime

→ commerce:inventory: ReturnShipmentReceivedListener
      ApproveCustomerReturnHandler called
      customerReturn.approve()
      Restores stock for each returned item
      Registers CustomerReturnApprovedEvent → commerce-events
```

**Event chain from `CustomerReturnApprovedEvent`:**
```
CustomerReturnApprovedEvent.payload
├── returnId        : Long
├── salesOrderId    : Long
├── organizationId  : Long
├── customerId      : Long
├── refundAmount    : BigDecimal
├── currency        : String
├── recipientNuban  : String
├── recipientBankCode: String
└── approvedAt      : ZonedDateTime

→ pay:transfers: CustomerReturnApprovedListener (a.k.a ChargeRefundInitiatedListener)
      InitiatePayoutHandler called
      sourceSystem=REFUND, sourceReferenceId=returnId
      Payout created (status=PENDING_APPROVAL if > threshold, else auto-approved)

→ accounting:ap-ar: CustomerReturnApprovedListener
      Reverses the original AR/Revenue entry
      Dr Revenue → Cr Refund Payable
```

---

## 12. Outbound Shipment (Delivery to Customer)

```
POST /api/v1/logistics/shipments
Body: { salesOrderId, recipientName, recipientPhone, deliveryAddress, carrierId }
```

**Handler**: `CreateShipmentHandler` (logistics:shipping)

**Flow:** Shipment created → assigned to rider → dispatched → delivered.

Each state change publishes events:
```
ShipmentDispatchedEvent → notifications: customer SMS with tracking
ShipmentLocationUpdatedEvent → SelectiveWebSocketBroadcaster:
      /topic/shipment/{trackingNumber}  ← customer tracks live
      /user/{riderId}/queue/notifications

ShipmentDeliveredEvent.payload
├── shipmentId     : Long
├── salesOrderId   : Long
├── organizationId : Long
├── outletId       : Long
└── deliveredAt    : ZonedDateTime

→ commerce:storefront: ShipmentDeliveredListener
      salesOrder.markDelivered()
      If COD payment: initiates charge flow (COD collection)
```

---

## 13. Inventory Replenishment (Inbound GRN)

```
POST /api/v1/logistics/purchase-orders
Body: { vendorId, items[], expectedDeliveryDate }
```

**Handler**: `CreatePurchaseOrderHandler` (logistics:receiving)

When supplier delivers:
```
POST /api/v1/logistics/grn
Body: { purchaseOrderId, items[]: { productId, receivedQuantity, unitCost } }
```

**Handler**: `CreateGrnHandler`

1. Creates `GoodsReceivedNote`.
2. Registers `GrnCreatedEvent`.

**Event chain from `GrnCreatedEvent`:**
```
GrnCreatedEvent.payload
├── grnId          : Long
├── organizationId : Long
├── outletId       : Long
├── items[]        : [{ productId, quantity, unitCost }]
└── receivedAt     : ZonedDateTime

→ commerce:inventory: GrnCreatedListener
      AdjustStockHandler called for each item
      Increases inventory quantity

→ accounting:ap-ar: GrnCreatedListener
      Creates AP (Accounts Payable) record for vendor invoice
      Dr Inventory → Cr Accounts Payable
```

---

## 14. Inter-Outlet Stock Transfer

```
POST /api/v1/commerce/stock-transfers
Body: { fromOutletId, toOutletId, items[]: { productId, quantity } }
```

**Handler**: `RequestStockTransferHandler`

Manager at source outlet approves → `StockTransferApprovedEvent`.
Manager at destination receives → `StockTransferReceivedEvent`.

**Event chain from `StockTransferApprovedEvent`:**
```
→ logistics:transfers: StockTransferApprovedListener
      Creates transfer logistics record
      Assigns rider for inter-outlet delivery
```

**Event chain from `StockTransferReceivedEvent`:**
```
→ commerce:inventory: (internal)
      Decreases source outlet inventory
      Increases destination outlet inventory
```

---

## 15. Payroll Processing

### 15.1 HR Initiates Payroll Run

```
POST /api/v1/hr/payroll/runs
Body: { month, year }
```

**Handler**: `InitiatePayrollRunHandler` (hr:payroll)

1. Creates `PayrollRun` (status = `DRAFT`).
2. `PayrollCalculationService.calculate(run)` — generates `Payslip` for each active employee.
   - Gross = base salary + allowances
   - Deductions = tax + pension + loan repayments
   - Net = Gross - Deductions
3. Saves run and payslips.

### 15.2 Payroll Approval (Maker-Checker)

```
POST /api/v1/hr/payroll/runs/{id}/approve
Body: { approverId }
```

**Handler**: `ApprovePayrollRunHandler`

1. `payrollRun.approve(approverId)` — enforces `approverId != initiatedBy` at domain level.
2. Status → `APPROVED`.
3. Registers `PayrollApprovedEvent`.

**Event chain from `PayrollApprovedEvent`:**
```
PayrollApprovedEvent.payload
├── payrollRunId   : Long
├── organizationId : Long
├── month          : int
├── year           : int
├── totalAmount    : BigDecimal
├── currency       : String
└── payslips[]     :
    ├── payslipId       : Long
    ├── employeeId      : Long
    ├── employeeName    : String
    ├── bankAccountNumber: String
    ├── bankCode        : String
    └── netAmount       : BigDecimal

→ pay:transfers: PayrollApprovedListener
      For each payslip: InitiatePayoutHandler called
      sourceSystem=PAYROLL, sourceReferenceId=payrollRunId
      Payout created per employee (status=PENDING_APPROVAL)
      All payouts grouped under the payroll run reference
```

### 15.3 Payout Approval & Execution

```
POST /api/v1/pay/transfers/{id}/approve
Body: { approverId }
```

**Handler**: `ApprovePayoutHandler` (pay:transfers)

1. `payout.approve(approverId)` — maker-checker enforced.
2. `ExecutePayoutHandler` triggered → calls bank transfer API.
3. `payout.complete(providerRef)` → registers `PayoutCompletedEvent`.

**Event chain from `PayoutCompletedEvent`:**
```
PayoutCompletedEvent.payload
├── payoutId           : Long
├── organizationId     : Long
├── sourceSystem       : String   ← PAYROLL
├── sourceReferenceId  : String   ← payrollRunId
├── amount             : BigDecimal
├── recipientName      : String
├── completedAt        : ZonedDateTime
└── providerReference  : String

→ hr:payroll: PayoutCompletedListener
      Marks individual Payslip as PAID
      When all payslips for the run are PAID → PayrollRun.status = DISBURSED
      Registers PayrollDisbursedEvent

→ pay:ledger: PayoutCompletedListener
      Posts journal: Dr OPERATING_CASH → Cr Suspense (outbound settlement)

→ pay:tx-query: PayoutCompletedListener
      Updates PayoutProjection read model
```

**Event chain from `PayrollDisbursedEvent`:**
```
PayrollDisbursedEvent.payload
├── payrollRunId   : Long
├── organizationId : Long
├── month          : int
├── year           : int
├── totalDisbursed : BigDecimal
├── employeeCount  : int
└── disbursedAt    : ZonedDateTime

→ analytics: PayrollAnalyticsListener
      Updates PayrollCostProjection

→ notifications: HrPayrollListener
      Sends "Salary Paid" SMS + email to each employee
```

---

## 16. Leave Management

### 16.1 Employee Requests Leave

```
POST /api/v1/hr/leave/requests
Body: { employeeId, leaveType, startDate, endDate, reason }
```

**Handler**: `RequestLeaveHandler` (hr:leave)

### 16.2 Manager Approves Leave

```
POST /api/v1/hr/leave/requests/{id}/approve
```

**Handler**: `ApproveLeaveHandler`

1. `leaveRequest.approve(approverId)` → registers `LeaveApprovedEvent`.

**Event chain from `LeaveApprovedEvent`:**
```
LeaveApprovedEvent.payload
├── leaveRequestId : Long
├── employeeId     : Long
├── organizationId : Long
├── startDate      : LocalDate
├── endDate        : LocalDate
├── leaveType      : String
└── approvedAt     : ZonedDateTime

→ hr:attendance: LeaveApprovedListener
      MarkAttendanceAsLeaveHandler called
      For each date in range: creates AttendanceRecord(status=ON_LEAVE)
      Ensures payroll deductions don't count these days as absences

→ notifications: HrPayrollListener
      Sends "Leave Approved" email to employee
```

---

## 17. Employee Loan

### 17.1 Employee Applies for Loan

```
POST /api/v1/hr/loans/apply
Body: { employeeId, principalAmount, monthlyDeduction }
```

### 17.2 HR Approves Loan

```
POST /api/v1/hr/loans/{id}/approve
Body: { startDate }
```

**Handler**: `ApproveLoanHandler`

1. `loan.approve(startDate)` → status = `APPROVED`.
2. Registers `LoanApprovedEvent` → `hr-events`.

**Event chain from `LoanApprovedEvent`:**
```
LoanApprovedEvent.payload
├── loanId         : Long
├── employeeId     : Long
├── organizationId : Long
├── amount         : BigDecimal
├── bankAccount    : String
├── bankCode       : String
└── approvedAt     : ZonedDateTime

→ pay:transfers: LoanApprovedListener
      InitiatePayoutHandler called
      sourceSystem=LOAN_DISBURSEMENT, sourceReferenceId=loanId
      Creates payout to employee's bank account
```

**Event chain from `PayoutCompletedEvent` (sourceSystem=LOAN_DISBURSEMENT):**
```
→ hr:loan: LoanDisbursementCompletedListener
      loan.markDisbursed() → status = ACTIVE
      Employee can now start repayments via payroll deduction
```

---

## 18. Mandate-Based Recurring Payments

*(Used by gyms, subscription services, SaaS businesses built on AtlasHub)*

### 18.1 Create a Mandate

```
POST /api/v1/pay/mandates
Body: { customerId, email, amount, currency, frequency, authorizationCode, firstChargeDate }
```

**Handler**: `CreateMandateHandler` (pay:mandates)

### 18.2 Daily Mandate Charge (Scheduled)

**Scheduler**: `MandateChargeScheduler` — runs daily at 09:00.

For each `ACTIVE` mandate where `nextChargeDate == today`:
1. `ChargeMandateHandler` called.
2. Pre-advances `nextChargeDate`.
3. Generates unique `chargeReference`.
4. Registers `MandateChargeDueEvent`.

**Event chain from `MandateChargeDueEvent`:**
```
MandateChargeDueEvent.payload
├── mandateId          : Long
├── organizationId     : Long
├── customerId         : String
├── email              : String
├── chargeReference    : String
├── amount             : BigDecimal
├── currency           : String
├── authorizationCode  : String   ← Paystack card token
├── paymentChannel     : String   ← CARD
├── provider           : String   ← PAYSTACK
├── sourceReferenceId  : String   ← mandateId as String
└── dueAt              : ZonedDateTime

→ pay:charges: MandateChargeDueListener
      InitializeChargeHandler called (Paystack recurring charge)
      sourceSystem=MANDATE_DEBIT
      Charge processed immediately (card token, no redirect)
```

On success → `ChargeSuccessfulEvent` (sourceSystem=MANDATE_DEBIT):
```
→ pay:mandates: MandateChargeSuccessfulListener
      Publishes MandateChargedEvent

→ notifications: PayEventListener
      Sends charge receipt to customer
```

On failure → `ChargeFailedEvent` (sourceSystem=MANDATE_DEBIT):
```
→ pay:mandates: MandateChargeFailedListener
      If card expired/invalid: mandate.markExpired()
      Publishes MandateExpiredEvent

→ notifications: PayEventListener
      Sends charge failure email to customer and org admin
```

---

## 19. Settlement

Paystack settles collected funds to the org's registered bank account on their settlement cycle.

**Adapter**: `PaystackSettlementWebhookAdapter` → `RecordSettlementHandler` (pay:settlement)

1. Creates `Settlement` record.
2. `ConfirmSettlementHandler` called after bank confirms receipt.
3. Registers `SettlementConfirmedEvent`.

**Event chain from `SettlementConfirmedEvent`:**
```
SettlementConfirmedEvent.payload
├── settlementId   : Long
├── organizationId : Long
├── amount         : BigDecimal
├── settledAt      : ZonedDateTime
└── orgAdminEmail  : String

→ notifications: PayEventListener
      Sends "Settlement Received" email to org admin

→ pay:ledger: SettlementConfirmedListener
      Reconciles SUSPENSE → OPERATING_CASH for settled amount
```

---

## 20. Webhook Delivery to Business's System

When any payment event occurs, `pay:webhooks` delivers it to the business's registered endpoint.

**Listener**: e.g. `ChargeSuccessfulWebhookListener` — topic: `pay-events`.

1. Finds all active `WebhookSubscription` records for the org with matching event type.
2. Calls `DeliverWebhookHandler`.
3. `HttpWebhookDeliveryAdapter` makes signed HTTP POST to business's endpoint.
4. On failure: creates `WebhookDelivery` with status `FAILED`.

**Retry**: `WebhookRetryScheduler` runs every 5 minutes, retries `RETRYING` deliveries with exponential backoff (max 5 attempts).

---

## 21. Employee Suspension

```
POST /api/v1/hr/employees/{id}/suspend
Body: { reason }
```

**Handler**: `SuspendEmployeeHandler` (hr:staff)

1. `employee.suspend()` → status = `SUSPENDED`.
2. Registers `EmployeeSuspendedEvent`.

**Event chain from `EmployeeSuspendedEvent`:**
```
EmployeeSuspendedEvent.payload
├── employeeId     : Long
├── organizationId : Long
├── userId         : Long
├── reason         : String
└── suspendedAt    : ZonedDateTime

→ iam: EmployeeSuspendedListener
      DeactivateMemberHandler called
      OrganizationMember deactivated → MemberDeactivatedEvent

→ auth: MemberDeactivatedListener
      All refresh tokens revoked for userId
      User immediately logged out

→ notifications: HrPayrollListener
      Sends "Account Suspended" email to employee and HR manager
```

---

## 22. Employee Termination

```
POST /api/v1/hr/employees/{id}/terminate
Body: { reason, terminationDate, finalPayment }
```

**Handler**: `TerminateEmployeeHandler`

1. `employee.terminate()` → status = `TERMINATED`.
2. Registers `EmployeeTerminatedEvent`.

**Event chain from `EmployeeTerminatedEvent`:**
```
EmployeeTerminatedEvent.payload
├── employeeId     : Long
├── organizationId : Long
├── userId         : Long
├── reason         : String
└── terminatedAt   : ZonedDateTime

→ iam: EmployeeTerminatedListener (via EmployeeSuspendedListener same flow)
      Deactivates OrganizationMember → MemberDeactivatedEvent

→ auth: MemberDeactivatedListener
      All sessions revoked

→ hr:attendance: EmployeeTerminatedListener
      Closes any open attendance record on termination date

→ hr:loan: EmployeeTerminatedListener
      Marks all ACTIVE loans as TERMINATION_PENDING
      HR admin must decide: final salary settlement or write-off

→ pay:mandates: EmployeeTerminatedListener
      Revokes all ACTIVE mandates for the employee

→ accounting:gl: EmployeeTerminatedListener
      Creates final expense accrual note for outstanding obligations

→ notifications: HrPayrollListener
      Sends "Employment Terminated" email to employee and HR manager
```

---

## 23. Organization Banned by AtlasHub Admin

AtlasHub compliance team bans an org for regulatory or policy violation.

```
POST /api/v1/admin/organizations/{id}/ban
Body: { reason }
```

**Handler**: `BanOrganizationHandler` (admin module)

1. `organization.ban(reason)` → registers `OrganizationBannedEvent`.

**Event chain from `OrganizationBannedEvent`:**
```
OrganizationBannedEvent.payload
├── organizationId : Long
├── reason         : String
└── bannedAt       : ZonedDateTime

→ iam: OrganizationBannedListener
      ALL members deactivated (including OWNER)
      MemberDeactivatedEvent published for each member

→ auth: OrganizationBannedListener
      ALL sessions revoked for ALL org members
      Users immediately logged out; cannot log back in

→ billing: OrganizationBannedListener
      Subscription immediately cancelled
      Outstanding invoices voided

→ pay:accounts: OrganizationBannedListener
      SuspendVirtualAccountHandler called for all org virtual accounts
      Anchor API suspends NUBAN — no further inbound transfers

→ pay:ledger: OrganizationBannedListener
      FreezeAccountHandler called for all org ledger accounts

→ notifications: AdminEventListener
      Sends "Account Suspended" email to org owner with reason and appeal process
```

---

## 24. Organization Reinstated

```
POST /api/v1/admin/organizations/{id}/unban
```

**Handler**: `UnbanOrganizationHandler`

Publishes `OrganizationUnbannedEvent`.

**Event chain (reverse of ban):**
```
→ iam: OrganizationUnbannedListener    — reactivates members
→ auth: OrganizationUnbannedListener   — allows login again
→ billing: OrganizationUnbannedListener — restores subscription
→ pay:accounts: OrganizationUnbannedListener — reactivates NUBAN
→ pay:ledger: OrganizationUnbannedListener — unfreezes accounts
→ notifications: AdminEventListener   — sends reinstatement email to owner
```

---

## 25. Platform Subscription Suspension (Non-Payment)

**Scheduler**: `SuspensionScheduler` — runs daily at 08:05.

Finds invoices overdue by more than 7 days → `SuspendSubscriptionHandler`.

**Event chain from `SubscriptionSuspendedEvent`:**
```
SubscriptionSuspendedEvent.payload
├── subscriptionId : Long
├── organizationId : Long
└── suspendedAt    : ZonedDateTime

→ iam: SubscriptionSuspendedListener
      Deactivates all non-OWNER members
      (OWNER can still log in to pay the invoice)

→ notifications: BillingNotificationListener
      Sends "Service Suspended" email to owner with outstanding invoice link
```

---

## 26. Accounting & Reporting

### 26.1 Journal Entry (Manual)

```
POST /api/v1/accounting/journal-entries
Body: { description, entries[]: { accountCode, type: DEBIT|CREDIT, amount } }
```

**Handler**: `CreateJournalEntryHandler` (accounting:gl)

Maker-checker required for entries above configured threshold. Reviewer must be different from creator.

### 26.2 Financial Reports

All reports are query-only, pulling from the ledger and projection tables:

```
GET /api/v1/accounting/reports/profit-loss?from=&to=
GET /api/v1/accounting/reports/balance-sheet?asOf=
GET /api/v1/accounting/reports/cash-flow?from=&to=
GET /api/v1/analytics/revenue?period=MONTHLY&year=2026
```

---

## Event Topic Summary

| Topic | Publishers |
|---|---|
| `accounts-events` | `accounts` |
| `auth-events` | `auth` |
| `iam-events` | `iam` |
| `billing-events` | `billing` |
| `compliance-events` | `compliance` |
| `admin-events` | `admin` |
| `pay-events` | `pay:accounts`, `pay:charges`, `pay:transfers`, `pay:mandates`, `pay:ledger`, `pay:settlement` |
| `commerce-events` | `commerce:storefront`, `commerce:inventory`, `commerce:catalog` |
| `hr-events` | `hr:staff`, `hr:leave`, `hr:payroll`, `hr:loan` |
| `logistics-events` | `logistics:shipping`, `logistics:returns`, `logistics:receiving`, `logistics:transfers` |

---

## Async Principle Summary

Every cross-module state change follows this pattern:

```
Module A:
  handler.execute(command)
    → aggregate.businessMethod()
    → aggregate.registerEvent(event)
    → repository.save()          ← transaction commits, outbox entry written
    → event published to Kafka

Module B:
  XyzListener.listen(event)
    → filters/validates event
    → checks EventDeliveryTracker (idempotency)
    → handler.execute(derivedCommand)
    → new aggregate state saved
    → new events registered and published if needed
```

**No synchronous cross-module write calls exist anywhere in AtlasHub.**
Cross-module reads (e.g. `UserQueryPort`, `EntitlementQueryPort`) are the only synchronous cross-module calls permitted.
