# Payment Module Design (`atlashub-pay`)

## 1. Domain Entities & Aggregates

**`VirtualAccount` (`accounts`)**
- **Fields**: `id`, `organizationId`, `customerId`, `accountNumber`, `bankName`, `accountName`, `balance`: BigDecimal, `status` (ACTIVE, INACTIVE)
- **Methods**: `credit(BigDecimal amount)`, `debit(BigDecimal amount)`, `deactivate()`

**`Wallet` & `WalletTransaction` (`ledger`)**
- **Fields (Wallet)**: `id`, `organizationId`, `currency`, `balance`
- **Fields (Transaction)**: `id`, `walletId`, `amount`, `type` (CREDIT, DEBIT), `reference`, `description`, `createdAt`

**`PaymentTransaction` (`charges`)**
- **Fields**: `id`, `organizationId`, `amount`: BigDecimal, `currency`, `channel` (CARD, BANK_TRANSFER, USSD, POS_TERMINAL), `status` (PENDING, SUCCESS, FAILED, REFUNDED), `reference`, `gatewayResponse`
- **Methods**: `markSuccessful(String gatewayResponse)`, `markFailed(String reason)`, `refund()`

**`Payout` (`transfers`)**
- **Fields**: `id`, `organizationId`, `amount`, `destinationBankCode`, `destinationAccountNumber`, `status` (PENDING, PROCESSING, SUCCESS, FAILED), `reference`
- **Methods**: `process()`, `complete()`, `fail(String reason)`

**`PaymentSplit` (`splits`)**
- **Fields**: `id`, `organizationId`, `name`, `type` (PERCENTAGE, FLAT), `subaccounts`: `List<SplitAccount>` (accountId, share)

**`SubscriptionMandate` (`subscriptions`)**
- **Fields**: `id`, `organizationId`, `customerId`, `planId`, `authorizationCode`, `status` (ACTIVE, REVOKED), `nextChargeDate`

## 2. Domain Events
- `VirtualAccountCreatedEvent(Long accountId, String accountNumber)`
- `PaymentSuccessfulEvent(Long transactionId, String reference, BigDecimal amount, String channel)`
- `PaymentFailedEvent(Long transactionId, String reason)`
- `PayoutCompletedEvent(Long payoutId, String reference)`
- `PayoutFailedEvent(Long payoutId, String reason)`
- `WalletFundedEvent(Long walletId, BigDecimal amount)`
- `WalletDebitedEvent(Long walletId, BigDecimal amount)`

## 3. Exceptions & Errors
**`PayErrorCode`**:
- `INSUFFICIENT_FUNDS`, `WALLET_NOT_FOUND`, `ACCOUNT_NOT_FOUND`
- `TRANSACTION_FAILED`, `TRANSACTION_NOT_FOUND`, `DUPLICATE_REFERENCE`
- `PAYOUT_FAILED`, `INVALID_BANK_DETAILS`

## 4. Commands & Use Cases
- `CreateVirtualAccountCommand(orgId, customerId, accountName)` -> `CreateVirtualAccountUseCase`
- `FundWalletCommand(walletId, amount, reference)` -> `FundWalletUseCase` (Creates CREDIT `WalletTransaction`).
- `DebitWalletCommand(walletId, amount, reference)` -> `DebitWalletUseCase` (Creates DEBIT `WalletTransaction`).
- `InitializePaymentCommand(amount, currency, returnUrl, splitId)` -> `InitializePaymentUseCase` (Returns checkout URL/reference).
- `ProcessWebhookPaymentCommand(reference, status, gatewayData)` -> `ProcessWebhookPaymentUseCase` (Validates payload, updates `PaymentTransaction`, publishes `PaymentSuccessfulEvent`).
- `InitiatePayoutCommand(orgId, amount, bankCode, accountNo)` -> `InitiatePayoutUseCase`
- `ProcessBulkPayoutCommand(List<PayoutRequest>)` -> `ProcessBulkPayoutUseCase`
- `CreateSplitGroupCommand(...)`, `CreateSubscriptionMandateCommand(...)`

## 5. Queries
- `GetWalletBalanceQuery(orgId)`
- `ListWalletTransactionsQuery(orgId, dateFrom, dateTo)`
- `GetPaymentStatusQuery(reference)`
- `ListPayoutsQuery(orgId, status)`
- `ListTransactionsQuery(orgId, channel, status, dateFrom, dateTo)`

## 6. Listeners
- `DisbursePayrollListener`: Listens to `PayrollDisbursedEvent` (from HR) to execute `ProcessBulkPayoutCommand` for employee salaries.
- `PaymentSuccessfulListener`: Listens to its own `PaymentSuccessfulEvent` to execute `FundWalletCommand` (and handle splits if configured).

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Pessimistic Locking (@Lock(PESSIMISTIC_WRITE))**: **CRITICAL** for Wallet and VirtualAccount. When deducting funds, the account row must be locked to prevent double-spending vulnerabilities under concurrent load.

### Idempotency & Inbox/Outbox
- **API Idempotency (IdempotencyKey)**: All fund movement commands (FundWalletCommand, InitiatePayoutCommand) require a client-supplied Idempotency Key.
- **Outbox**: Used to publish PaymentSuccessfulEvent and PayoutCompletedEvent.
- **Inbox (EventDeliveryTracker)**: Processes payroll disbursement requests exactly once to prevent double-paying salaries.
