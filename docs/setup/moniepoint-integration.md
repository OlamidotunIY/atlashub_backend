# Moniepoint Integration

## Role in AtlasHub

Moniepoint is the **POS terminal and agency banking provider** in AtlasHub. It complements Paystack by enabling:

- **Card-present transactions** (physical POS terminals at stores and outlets)
- **Agency banking** (accepting deposits from walk-in customers at agent locations)
- **QR code payments** (customers scan and pay via Moniepoint QR)

Moniepoint is the right provider for organizations that need physical card-acceptance infrastructure — something Paystack alone does not offer.

---

## Integration Architecture

Moniepoint integrates through the same `PaymentGatewayPort` interface as Paystack where capabilities overlap, and via a dedicated `PosTerminalPort` for POS-specific operations.

```java
public interface PosTerminalPort {
    TerminalSessionResult initializeSession(String terminalId, Long organizationId);
    TransactionResult processCardPresent(CardPresentRequest request);
    TerminalStatus getTerminalStatus(String terminalId);
    List<Terminal> listTerminals(Long organizationId);
}
```

---

## POS Terminal Lifecycle

### Terminal Provisioning
Organizations apply for POS terminals through the AtlasHub dashboard. Upon approval, Moniepoint provisions terminal hardware. AtlasHub stores the terminal metadata:

```java
// Terminal (Entity in pay module)
Terminal
├── id: Long
├── organizationId: Long
├── outletId: Long
├── terminalId: String             ← Moniepoint terminal ID
├── serialNumber: String
├── status: TerminalStatus         ← ACTIVE, INACTIVE, BLOCKED
└── assignedAt: ZonedDateTime
```

### Transaction Flow (Card-Present)

```
1. Cashier selects "Card" payment on POS screen
2. Commerce calls pay:InitiateChargeUseCase with paymentMethod=CARD_PRESENT + terminalId
3. Pay calls PosTerminalPort.initializeSession(terminalId)
4. Moniepoint activates the terminal — customer taps/inserts card
5. Moniepoint notifies AtlasHub via webhook: "transaction.successful"
6. Pay:ProcessPosTransactionUseCase marks Charge as SUCCESSFUL
7. Commerce:ChargeSuccessfulListener completes the sale
```

---

## Webhook Handling

```java
@RestController
@RequestMapping("/api/v1/webhooks/moniepoint")
public class MoniepointWebhookController {

    private final MoniepointSignatureVerifier verifier;
    private final ProcessPosTransactionUseCase processPosTransaction;
    private final MoniepointWebhookMapper mapper;

    @PostMapping
    public ResponseEntity<Void> onWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Moniepoint-Signature") String signature) {

        verifier.verify(rawBody, signature);

        MoniepointWebhookPayload payload = mapper.parse(rawBody);

        switch (payload.event()) {
            case "transaction.successful" -> processPosTransaction.execute(
                new ProcessPosTransactionCommand(
                    payload.data().terminalId(),
                    payload.data().reference(),
                    payload.data().amount(),        // Moniepoint sends in kobo — adapter converts
                    payload.data().maskedPan(),
                    payload.data().cardScheme()
                )
            );
            case "transaction.failed" -> handlePosFailure.execute(
                new HandlePosFailureCommand(
                    payload.data().terminalId(),
                    payload.data().reference(),
                    payload.data().responseCode()
                )
            );
        }

        return ResponseEntity.ok().build();
    }
}
```

---

## QR Code Payments

For POS deployments where hardware terminals are unavailable, QR payments are an alternative:

```java
@Override
public ChargeInitResult initializeQrCharge(InitializeChargeRequest req) {
    MoniepointQrRequest qrReq = MoniepointQrRequest.builder()
        .amount(req.amount().multiply(BigDecimal.valueOf(100)).longValue())  // kobo
        .reference(req.reference())
        .merchantCode(req.organizationExternalCode())
        .build();

    MoniepointQrResponse resp = client.generateQrCode(qrReq);

    if (!resp.success()) {
        throw new ExternalServiceException(PayErrorCode.QR_GENERATION_FAILED,
            "Moniepoint: " + resp.message());
    }

    return new ChargeInitResult(null, null, req.reference(), resp.data().qrImageUrl());
}
```

---

## Configuration

```yaml
atlashub:
  integrations:
    moniepoint:
      base-url: ${MONIEPOINT_BASE_URL:https://api.moniepoint.com/v1}
      api-key: ${MONIEPOINT_API_KEY}
      merchant-code: ${MONIEPOINT_MERCHANT_CODE}
      webhook-secret: ${MONIEPOINT_WEBHOOK_SECRET}
      timeout-seconds: 10
```

---

## Error Handling

All Moniepoint errors translate to domain errors at the adapter boundary. Moniepoint response codes and raw API responses never cross into the application layer.

```java
catch (MoniepointApiException ex) {
    PayErrorCode code = switch (ex.responseCode()) {
        case "57"  -> PayErrorCode.CARD_DECLINED;       // transaction not permitted
        case "51"  -> PayErrorCode.INSUFFICIENT_FUNDS;
        case "91"  -> PayErrorCode.GATEWAY_TIMEOUT;     // issuer timeout
        default    -> PayErrorCode.GATEWAY_ERROR;
    };
    throw new ExternalServiceException(code, "POS transaction failed: " + ex.message());
}
```
