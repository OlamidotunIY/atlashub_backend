# Paystack Integration

## Role in AtlasHub

Paystack is the **card and USSD payment provider** in AtlasHub. It handles:

- Card payments (inline checkout or redirect)
- USSD charges
- Bank transfer charges (via Paystack's dedicated virtual accounts — distinct from Anchor NUBANs)
- Direct debit mandates (recurring charges)
- Bank account name enquiry (used by HR to verify employee bank details)
- Identity verification: BVN and NIN validation (used by Compliance)
- Refunds

---

## Integration Architecture

Paystack is wrapped behind `PaymentGatewayPort` in `atlashub-pay:application/port/out`. The domain never references Paystack types.

```java
public interface PaymentGatewayPort {
    ChargeInitResult initializeCharge(InitializeChargeRequest request);
    ChargeVerifyResult verifyCharge(String reference);
    MandateResult createMandate(CreateMandateRequest request);
    MandateChargeResult chargeMandate(String mandateCode, BigDecimal amount, String reference);
    RefundResult initiateRefund(String chargeReference, BigDecimal amount);
    BankAccountDetails resolveAccountName(String accountNumber, String bankCode);
    BvnVerificationResult verifyBvn(String bvn, String firstName, String lastName, LocalDate dob);
    List<Bank> getSupportedBanks();
}
```

---

## Charge Initialization

```java
@Component
public class PaystackGatewayAdapter implements PaymentGatewayPort {

    private final PaystackApiClient client;

    @Override
    public ChargeInitResult initializeCharge(InitializeChargeRequest req) {
        // Convert to Paystack's unit: kobo (1 NGN = 100 kobo)
        long amountInKobo = req.amount().multiply(BigDecimal.valueOf(100)).longValue();

        PaystackInitRequest paystackReq = PaystackInitRequest.builder()
            .amount(amountInKobo)
            .currency(req.currency().name())
            .email(req.email())
            .reference(req.reference())
            .callbackUrl(req.redirectUrl())
            .metadata(Map.of(
                "atlashub_charge_id",    req.chargeId(),
                "atlashub_org_id",       req.organizationId(),
                "atlashub_purpose",      req.purpose().name()
            ))
            .build();

        PaystackInitResponse resp = client.initializeTransaction(paystackReq);

        if (!resp.status()) {
            throw new ExternalServiceException(PayErrorCode.GATEWAY_CHARGE_INIT_FAILED,
                "Paystack: " + resp.message());
        }

        return new ChargeInitResult(
            resp.data().authorizationUrl(),
            resp.data().accessCode(),
            req.reference()
        );
    }
}
```

---

## Webhook: Charge Successful

Paystack notifies AtlasHub when a payment completes:

```http
POST /api/v1/webhooks/paystack
X-Paystack-Signature: {sha512_hmac}
```

```java
@RestController
@RequestMapping("/api/v1/webhooks/paystack")
public class PaystackWebhookController {

    private final PaystackSignatureVerifier verifier;
    private final HandleChargeSuccessUseCase handleSuccess;
    private final HandleChargeFailureUseCase handleFailure;
    private final PaystackWebhookMapper mapper;

    @PostMapping
    public ResponseEntity<Void> onWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Paystack-Signature") String signature) {

        verifier.verify(rawBody, signature);

        PaystackWebhookPayload payload = mapper.parse(rawBody);

        switch (payload.event()) {
            case "charge.success" -> handleSuccess.execute(
                new HandleChargeSuccessCommand(
                    payload.data().reference(),
                    payload.data().amount(),      // in kobo — adapter converts back to NGN
                    payload.data().gatewayResponse()
                )
            );
            case "charge.failed" -> handleFailure.execute(
                new HandleChargeFailureCommand(
                    payload.data().reference(),
                    payload.data().gatewayResponse()
                )
            );
        }

        return ResponseEntity.ok().build();
    }
}
```

**Kobo conversion**: Paystack amounts are in kobo. The adapter always converts: `amountInNaira = amountInKobo / 100`. This conversion happens exclusively inside the adapter — the domain only ever sees NGN amounts.

### Signature Verification (SHA-512)

Paystack uses HMAC-SHA512 (not SHA-256):

```java
@Component
public class PaystackSignatureVerifier {
    private final String secretKey;

    public void verify(String payload, String signatureHeader) {
        String expected = hmacSha512Hex(payload, secretKey);
        if (!MessageDigest.isEqual(expected.getBytes(), signatureHeader.getBytes())) {
            throw new SecurityException("Invalid Paystack webhook signature");
        }
    }
}
```

---

## Bank Account Name Enquiry (HR Use Case)

When HR adds an employee bank account, the system verifies the account name before saving:

```java
// In AddEmployeeBankUseCase
BankAccountDetails account = paymentGatewayPort.resolveAccountName(
    command.accountNumber(), command.bankCode());

if (!nameMatchesEmployee(account.accountName(), command.employeeFullName())) {
    throw new BusinessRuleException(HrErrorCode.BANK_ACCOUNT_NAME_MISMATCH,
        "Account name '" + account.accountName() + "' does not match employee name");
}
```

---

## BVN Verification (Compliance Use Case)

```java
// In CompleteOwnerIdentityUseCase (Compliance module)
BvnVerificationResult bvn = paymentGatewayPort.verifyBvn(
    command.bvn(),
    command.firstName(),
    command.lastName(),
    command.dateOfBirth()
);

if (!bvn.isMatch()) {
    throw new BusinessRuleException(ComplianceErrorCode.BVN_VERIFICATION_FAILED,
        "BVN details do not match your provided identity information");
}
```

---

## Configuration

```yaml
atlashub:
  integrations:
    paystack:
      base-url: ${PAYSTACK_BASE_URL:https://api.paystack.co}
      secret-key: ${PAYSTACK_SECRET_KEY}
      webhook-secret: ${PAYSTACK_WEBHOOK_SECRET}    # same as secret-key for Paystack
      timeout-seconds: 15
      test-mode: ${PAYSTACK_TEST_MODE:true}
```

---

## Error Handling

```java
catch (PaystackApiException ex) {
    // Translate Paystack error codes to AtlasHub domain errors
    PayErrorCode code = switch (ex.paystackCode()) {
        case "DECLINED_CARD"         -> PayErrorCode.CARD_DECLINED;
        case "INSUFFICIENT_FUNDS"    -> PayErrorCode.INSUFFICIENT_FUNDS;
        case "INVALID_ACCOUNT"       -> PayErrorCode.INVALID_BANK_ACCOUNT;
        default                      -> PayErrorCode.GATEWAY_ERROR;
    };
    throw new ExternalServiceException(code, ex.message());
}
```
