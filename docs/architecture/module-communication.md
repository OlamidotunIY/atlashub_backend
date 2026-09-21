# Module Communication Patterns

## The Problem

In a modular monolith, modules share the same JVM and the same database. The temptation is to call a use case from another module directly, import its repository, or query its database table. This creates **hidden coupling** — change one module and another silently breaks.

AtlasHub uses two patterns from Eric Evans' DDD and the Enterprise Integration Patterns (EIP) book to eliminate this:

1. **Open Host Service (OHS)** — for synchronous data queries
2. **Published Language / Domain Events** — for asynchronous state propagation

No module calls another module's internal class. Ever.

---

## Pattern 1: Open Host Service (Synchronous Reads)

### When to Use
Use this when Module B needs a piece of data owned by Module A **right now**, in the same request, and the result influences control flow (e.g., "Is this org's compliance approved? If not, reject the payment").

### How It Works

**Step 1 — Define the port interface in `atlashub-shared`.**
The interface is owned conceptually by Module A (the data provider), but lives in shared so Module B can depend on it without creating a direct dependency on A's internals.

```java
// atlashub-shared
// Owned by: accounts module
public interface OrganizationQueryPort {
    Optional<OrganizationDto> findById(Long orgId);
    String getBaseCurrency(Long orgId);
    boolean existsById(Long orgId);
}

// Owned by: compliance module
public interface ComplianceQueryPort {
    boolean isApproved(Long organizationId);
    ComplianceStatus getStatus(Long organizationId);
}

// Owned by: billing module
public interface EntitlementQueryPort {
    boolean hasActiveSubscription(Long orgId, String productCode);
    boolean isSubscriptionSuspended(Long orgId);
}

// Owned by: iam module
public interface MembershipQueryPort {
    boolean isMemberOf(Long userId, Long orgId);
    Set<String> getPermissions(Long userId, Long orgId);
    MemberStatus getMemberStatus(Long userId, Long orgId);
}
```

**Step 2 — Implement the port in the owning module.**
```java
// In atlashub-platform:accounts
@Component
public class OrganizationQueryAdapter implements OrganizationQueryPort {
    private final SpringDataOrganizationRepository jpa;
    private final OrganizationMapper mapper;

    @Override
    public Optional<OrganizationDto> findById(Long orgId) {
        return jpa.findById(orgId).map(mapper::toDto);
    }

    @Override
    public String getBaseCurrency(Long orgId) {
        return jpa.findById(orgId)
            .map(org -> org.getBaseCurrency().name())
            .orElseThrow(() -> new NotFoundException(AccountsErrorCode.ORGANIZATION_NOT_FOUND));
    }
}
```

**Step 3 — Inject and call the port in the consuming module.**
```java
// In atlashub-pay — uses the port, no import of accounts classes
@Service
public class InitiateChargeUseCase extends BaseUseCase<InitiateChargeCommand, ChargeResult> {

    private final ComplianceQueryPort complianceQueryPort;     // injected — not accounts internals
    private final EntitlementQueryPort entitlementQueryPort;    // injected
    private final ChargeRepository chargeRepository;

    @Override
    @Transactional
    public ChargeResult execute(InitiateChargeCommand command) {
        // Guard: org must be KYC approved
        if (!complianceQueryPort.isApproved(command.orgId())) {
            throw new BusinessRuleException(PayErrorCode.ORGANIZATION_NOT_APPROVED);
        }
        // Guard: org must have active Pay subscription
        if (!entitlementQueryPort.hasActiveSubscription(command.orgId(), "ATLAS_PAY")) {
            throw new BusinessRuleException(PayErrorCode.NO_ACTIVE_SUBSCRIPTION);
        }
        // ... proceed with charge
    }
}
```

### Dependency Direction
```
atlashub-pay → atlashub-shared:ComplianceQueryPort ← atlashub-platform:compliance
```
`pay` depends on the port interface (stable abstraction). `compliance` implements it. Neither knows about the other's internals. Circular dependency is **impossible** because `compliance` never imports `pay`.

### Anti-Pattern to Avoid
```java
// ❌ WRONG — never do this
import com.atlashub.accounts.domain.repository.OrganizationRepository; // imports accounts internals
import com.atlashub.accounts.application.usecase.GetOrgDetailsUseCase;  // imports accounts use case
```

---

## Pattern 2: Published Language / Domain Events (Asynchronous State Propagation)

### When to Use
Use this when Module A's state change needs to cause a side effect in Module B, but Module A should not know that Module B exists. Examples:
- Payment succeeds → Commerce completes the sale
- Org compliance approved → Pay issues NUBAN
- Payroll approved → Pay executes bulk payouts

### How It Works

**Step 1 — Define the event record in the publishing module.**
```java
// In atlashub-pay:domain/event
public record ChargeSuccessfulEvent(
    String eventId,
    String aggregateId,           // chargeId
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ChargeSuccessfulEvent.Payload> {
    public record Payload(
        Long chargeId,
        Long organizationId,
        String reference,
        BigDecimal amount,
        String currency,
        SourceSystem sourceSystem,
        String sourceReferenceId   // e.g., salesOrderId
    ) {}
}
```

**Step 2 — Register the event in the aggregate and publish via outbox.**
```java
// In the Charge aggregate
public void markSuccessful(String gatewayRef, String gatewayResponse) {
    this.status = ChargeStatus.SUCCESSFUL;
    this.completedAt = ZonedDateTime.now();
    this.gatewayReference = gatewayRef;
    registerEvent(new ChargeSuccessfulEvent(
        UUID.randomUUID().toString(),
        String.valueOf(this.id),
        ZonedDateTime.now(),
        new ChargeSuccessfulEvent.Payload(this.id, this.organizationId, this.reference,
            this.amount.amount(), this.amount.currency().name(), this.sourceSystem, this.sourceReferenceId)
    ));
}

// In the use case
chargeRepository.save(charge);
publishEvents(charge, publisher);  // writes to outbox in same transaction
```

**Step 3 — The consuming module copies the event record (Anti-Corruption Layer).**
Module B (Commerce) does NOT import `ChargeSuccessfulEvent` from Pay. It defines its own identical copy:
```java
// In atlashub-commerce:domain/event — COPY of the event
public record ChargeSuccessfulEvent(
    String eventId,
    String aggregateId,
    ZonedDateTime occurredAt,
    Payload payload
) implements DomainEvent<ChargeSuccessfulEvent.Payload> {
    public record Payload(
        Long chargeId,
        Long organizationId,
        String reference,
        BigDecimal amount,
        String currency,
        String sourceSystem,     // String here — Commerce doesn't need SourceSystem enum from Pay
        String sourceReferenceId
    ) {}
}
```
This is the **Published Language** pattern: the event schema is the contract, and each module owns its own copy of the type. If Pay adds a new field, Commerce is unaffected until it chooses to consume it.

**Step 4 — Listen and react.**
```java
// In atlashub-commerce:adapter/in/messaging
@Slf4j
@Component
public class PaymentResultListener extends BaseKafkaEventListener {

    private static final String GROUP_ID = "commerce-payment-group";

    private final CompletePosSaleUseCase completeUseCase;
    private final FailPosSaleUseCase failUseCase;

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void onPaymentResult(String payload) {
        processEventIfMatches(payload, "ChargeSuccessfulEvent",
            ChargeSuccessfulEvent.class, log, GROUP_ID, event -> {
                if ("COMMERCE_CHECKOUT".equals(event.payload().sourceSystem())) {
                    Long orderId = Long.parseLong(event.payload().sourceReferenceId());
                    completeUseCase.execute(new CompletePosSaleCommand(orderId, event.payload().chargeId()));
                }
            });

        processEventIfMatches(payload, "ChargeFailedEvent",
            ChargeFailedEvent.class, log, GROUP_ID, event -> {
                if ("COMMERCE_CHECKOUT".equals(event.payload().sourceSystem())) {
                    Long orderId = Long.parseLong(event.payload().sourceReferenceId());
                    failUseCase.execute(new FailPosSaleCommand(orderId, event.payload().reason()));
                }
            });
    }
}
```

---

## Pattern 3: Anti-Corruption Layer (External Systems)

When AtlasHub integrates with an external API (Paystack, Anchor, GIG Logistics), we define a **port interface** in the application layer and an **adapter** in the infrastructure layer. This prevents external API models from leaking into the domain.

```java
// Application layer port — domain language
public interface PaymentGatewayPort {
    ChargeInitResult initializeCharge(BigDecimal amount, Currency currency, String email,
                                       String reference, ChargePurpose purpose, String redirectUrl);
    boolean verifyCharge(String reference);
    PayoutResult initiateTransfer(BigDecimal amount, String bankCode, String accountNumber, String narration);
}

// Adapter — translates between domain and Paystack API
@Component
public class PaystackGatewayAdapter implements PaymentGatewayPort {
    private final PaystackApiClient apiClient;

    @Override
    public ChargeInitResult initializeCharge(...) {
        PaystackInitRequest req = new PaystackInitRequest(amount, currency, email, reference, metadata);
        PaystackInitResponse resp = apiClient.initializeTransaction(req);
        if (!resp.status()) throw new ExternalServiceException(PayErrorCode.GATEWAY_ERROR, resp.message());
        return new ChargeInitResult(resp.data().authorizationUrl(), resp.data().accessCode());
    }
}
```

---

## Complete Module Dependency Map

The following table shows which modules provide what to which consumers, and via which pattern:

| Provider | What It Provides | Consumer(s) | Pattern |
|---|---|---|---|
| `accounts` | `OrganizationQueryPort`, `UserQueryPort` | All modules | OHS Sync |
| `compliance` | `ComplianceQueryPort` | `pay`, `billing`, `commerce` | OHS Sync |
| `billing` | `EntitlementQueryPort` | `pay`, `commerce`, `logistics`, `hr`, `accounting` | OHS Sync |
| `iam` | `MembershipQueryPort`, `ApiKeyQueryPort` | `auth` | OHS Sync |
| `pay` | `ChargeSuccessfulEvent`, `PayoutCompletedEvent` | `commerce`, `hr`, `accounting`, `billing`, `analytics` | Kafka Events |
| `commerce` | `PosSaleCompletedEvent`, `PurchaseOrderSentEvent` | `accounting`, `logistics`, `analytics`, `notifications` | Kafka Events |
| `hr` | `PayrollApprovedEvent`, `EmployeeTerminatedEvent` | `pay`, `accounting`, `notifications`, `analytics` | Kafka Events |
| `logistics` | `ShipmentDeliveredEvent`, `StockTransferReceivedEvent` | `commerce`, `accounting`, `notifications`, `analytics` | Kafka Events |
| `compliance` | `OrganizationComplianceApprovedEvent` | `pay`, `billing`, `admin`, `notifications` | Kafka Events |
| `billing` | `SubscriptionSuspendedEvent`, `InvoiceIssuedEvent` | `iam`, `pay`, `accounts`, `notifications` | Kafka Events |

---

## Rules Summary

1. **Never import another module's internal class** — repository, entity, use case, mapper.
2. **Sync reads** go through `atlashub-shared` port interfaces injected via Spring DI.
3. **State changes** propagate via Kafka domain events through the Outbox.
4. **Event types** are copied per-module (Published Language) — no shared event type imports.
5. **External APIs** are wrapped in port + adapter (Anti-Corruption Layer).
6. **Circular dependencies are architecturally impossible** — `compliance` implements a port that `pay` uses, but `compliance` never imports `pay`.
