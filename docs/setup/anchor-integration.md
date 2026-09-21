# Anchor Integration

## Role in AtlasHub

Anchor is the **banking-as-a-service (BaaS) provider** for AtlasHub's core financial infrastructure. Through Anchor's API, AtlasHub provisions:

- **NUBANs (Nigerian Uniform Bank Account Numbers)**: Virtual accounts linked to each organization, customer, or outlet
- **Sub-accounts**: Logical accounts for multi-pocket wallet architecture (Operating, Escrow, Payroll Reserve, Tax Holding, Split Holding)
- **Collections**: Receiving inbound transfers to virtual accounts
- **Settlements**: Sweeping collected funds to organization bank accounts

Anchor provides the regulated financial plumbing. AtlasHub's own ledger tracks the logical money movement on top of Anchor's settlement layer.

---

## What AtlasHub Uses Anchor For

| Capability | Used For |
|---|---|
| Virtual account issuance (NUBAN) | Per-org, per-customer, per-outlet virtual accounts |
| Inbound transfer webhooks | Detecting when a NUBAN is funded |
| Sub-account management | Logical pockets within the wallet architecture |
| Sweep / settlement | Paying out to org or customer bank accounts |

AtlasHub does NOT use Anchor for:
- Card payments (Paystack handles that)
- USSD charges (Paystack or Moniepoint handles that)

---

## Integration Architecture

Anchor is wrapped behind the `VirtualAccountPort` and `SettlementPort` interfaces in `atlashub-pay:application/port/out`. No module outside Pay ever directly references Anchor types.

```java
// Ports — domain language, no Anchor types
public interface VirtualAccountPort {
    IssuedAccountResult issueVirtualAccount(IssueAccountRequest request);
    VirtualAccountDetails getAccountDetails(String nuban);
}

public interface SettlementPort {
    SettlementResult initiateSettlement(SettlementRequest request);
    SettlementStatus getSettlementStatus(String reference);
}

// Adapter — translates AtlasHub domain ↔ Anchor API
@Component
public class AnchorVirtualAccountAdapter implements VirtualAccountPort {

    private final AnchorApiClient client;

    @Override
    public IssuedAccountResult issueVirtualAccount(IssueAccountRequest request) {
        AnchorCreateAccountRequest anchorReq = AnchorCreateAccountRequest.builder()
            .fullName(request.accountName())
            .email(request.email())
            .type(resolveAccountType(request.purpose()))
            .build();

        AnchorCreateAccountResponse resp = client.createVirtualAccount(anchorReq);

        if (!resp.success()) {
            throw new ExternalServiceException(PayErrorCode.VIRTUAL_ACCOUNT_ISSUANCE_FAILED,
                "Anchor: " + resp.errorMessage());
        }

        return new IssuedAccountResult(
            resp.data().accountNumber(),  // NUBAN
            resp.data().bankName(),
            resp.data().bankCode()
        );
    }
}
```

---

## Inbound Transfer Webhook

When a NUBAN receives an inbound bank transfer, Anchor sends a webhook to AtlasHub:

```http
POST /api/v1/webhooks/anchor
X-Anchor-Signature: sha256={hmac}
Content-Type: application/json

{
  "event": "virtualaccount.credited",
  "data": {
    "accountNumber": "0123456789",
    "amount": 50000.00,
    "currency": "NGN",
    "reference": "ANC-TXN-12345",
    "senderAccountName": "ACME Corp",
    "senderAccountNumber": "9876543210",
    "narration": "Payment for invoice INV-042"
  }
}
```

AtlasHub's `AnchorWebhookController` verifies the signature and delegates to `ProcessInboundTransferUseCase`:

```java
@RestController
@RequestMapping("/api/v1/webhooks/anchor")
public class AnchorWebhookController {

    private final AnchorSignatureVerifier verifier;
    private final ProcessInboundTransferUseCase processInboundTransfer;
    private final AnchorWebhookMapper mapper;

    @PostMapping
    public ResponseEntity<Void> onWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Anchor-Signature") String signature) {

        verifier.verify(rawBody, signature);  // throws if invalid

        AnchorWebhookPayload payload = mapper.parse(rawBody);

        if ("virtualaccount.credited".equals(payload.event())) {
            processInboundTransfer.execute(new ProcessInboundTransferCommand(
                payload.data().accountNumber(),
                payload.data().amount(),
                payload.data().reference(),
                payload.data().narration()
            ));
        }

        return ResponseEntity.ok().build();
    }
}
```

### Idempotency
`ProcessInboundTransferUseCase` uses the Anchor `reference` as an idempotency key. Anchor may send the same webhook multiple times (retries). The use case checks `virtualAccountTransactionRepository.existsByExternalReference(ref)` before processing.

---

## Configuration

```yaml
atlashub:
  integrations:
    anchor:
      base-url: ${ANCHOR_BASE_URL:https://api.anchor.com/v1}
      api-key: ${ANCHOR_API_KEY}
      webhook-secret: ${ANCHOR_WEBHOOK_SECRET}
      timeout-seconds: 10
```

---

## Error Handling

All Anchor API errors are caught in the adapter layer and translated to `ExternalServiceException`:

```java
catch (AnchorApiException ex) {
    log.error("Anchor API error: {} — {}", ex.errorCode(), ex.message());
    throw new ExternalServiceException(PayErrorCode.ANCHOR_SERVICE_ERROR,
        "Banking provider error. Please try again.");
}
catch (ConnectTimeoutException | ReadTimeoutException ex) {
    throw new ExternalServiceException(PayErrorCode.ANCHOR_TIMEOUT,
        "Banking provider did not respond in time. Please retry.");
}
```

Raw Anchor exceptions and error codes **never** propagate past the adapter boundary. The application layer receives only domain-typed exceptions.
