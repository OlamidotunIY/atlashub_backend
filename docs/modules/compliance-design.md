# Compliance / KYB Module Design (`atlashub-platform:compliance`)

## Role & Purpose

The `compliance` module owns AtlasHub's business eligibility and KYB journey. It decides whether an organization is eligible for AtlasHub's banking programme, coordinates Anchor business-customer verification, records provider-requested documents, and exposes the effective compliance decision to the rest of the platform.

AtlasHub is the system of record for the onboarding application, supported-business policy, officers, uploaded files, terms acceptance, status history, retry state, and the effective platform decision. Anchor is the banking/KYB provider and supplies confirmed provider facts such as the Anchor customer ID, provider-requested documents, document decisions, and provider verification outcome.

The module does **not** own:

- Organization identity or legal registration fields (`accounts`)
- Authentication or authorization (`auth`, `iam`)
- Deposit accounts, FBO subaccounts, or reserved accounts (`pay:accounts`)
- Internal balances (`pay:ledger`)
- Withdrawal beneficiaries (`pay:transfers`)

An organization is banking-approved only when AtlasHub policy says it is eligible, the current service agreement is accepted, and Anchor KYB is approved.

---

## 1. Supported Business Policy

### Legal Registration Types

Initial supported types:

| AtlasHub value | Anchor code | Status |
|---|---|---|
| `BUSINESS_NAME` | `Business_Name` | Enabled |
| `PRIVATE_INCORPORATED` | `Private_Incorporated` | Enabled |

Known but initially disabled types:

- `INCORPORATED_TRUSTEES`
- `FREE_ZONE`
- `GOVERNMENT`
- `PRIVATE_INCORPORATED_GOVERNMENT`
- `COOPERATIVE_SOCIETY`
- `PUBLIC_INCORPORATED`

Disabled types fail during organization registration. Enabling one is a policy change requiring its own validation rules, officer rules, test fixtures, and provider certification.

### Supported Industries

Initial industry allowlist:

- `COMMERCE_PHYSICAL_GOODS`
- `COMMERCE_DIGITAL_SERVICES`
- `COMMERCE_PHYSICAL_SERVICES`
- `COMMERCE_PROFESSIONAL_SERVICES`
- `HOSPITALITY_HOTELS`
- `HOSPITALITY_RESTAURANTS`
- `LOGISTICS_COURIER_SERVICES`
- `LOGISTICS_FREIGHT_SERVICES`
- `RETAIL`
- `WHOLESALE`
- `RESTAURANTS`

Financial services, gaming, public/government entities, political organizations, and other enhanced-risk categories are not part of the initial programme. Client-provided free-form industry strings are never sent to Anchor; every AtlasHub value maps explicitly to one Anchor industry code.

### KYB Versus Additional Documents

Every organization receiving an Anchor deposit account or subaccount must complete Anchor KYB. What varies by legal registration type and registration date is the set of officers and additional documents Anchor requests.

AtlasHub may call Anchor's document-requirements endpoint before submission to preview the likely requirements. The authoritative requirements are those returned for the created Anchor customer, especially through `customer.identification.awaitingDocument` or the customer-document query. Requirements must not be hard-coded as a fixed list.

---

## 2. Onboarding Journey

AtlasHub retains a five-step onboarding journey. The steps are the platform's way of collecting, validating, and saving the complete application before any provider submission. Anchor's asynchronous verification statuses are not additional UI steps and do not replace this journey.

```
1. BUSINESS_PROFILE
   Legal name, registration type/date/number, business BVN,
   industry, description, and website

2. CONTACT_INFO
   General/support/dispute emails, phone number,
   main operating address, and registered address

3. OWNERS_AND_OFFICERS
   One or more OWNER/DIRECTOR records according to policy

4. COMPLIANCE_DOCUMENTS
   AtlasHub's preflight document requirements based on legal
   registration type and registration date

5. SERVICE_AGREEMENT
   Terms version, acceptedAt, source IP address, final review,
   and consent to submit the saved application to Anchor
```

All five steps are AtlasHub-owned collection steps. Each step can be saved as a draft and resumed. Step completion is based on semantic validation of that step's required fields, not simply the presence of a step name in a set.

Before Step 4, AtlasHub uses the selected registration type and registration date to preview the expected Anchor document types. Files and textual values are collected and stored locally. After Step 5 submission, AtlasHub creates the Anchor customer and triggers KYB. When Anchor returns customer-specific document IDs, AtlasHub maps the saved documents by type and uploads them.

If Anchor asks for an additional or replacement document, AtlasHub marks Step 4 incomplete again, sets the application to `ACTION_REQUIRED`, and lets the organization supply only the outstanding item. The other completed steps and approved documents remain intact.

The former `SETTLEMENT_ACCOUNT` compliance step is removed. Anchor issues the organization's bank accounts after KYB. Any external withdrawal destination belongs to `pay:transfers`, not compliance.

---

## 3. Domain Model

### `ComplianceRecord` (Aggregate Root)

```
ComplianceRecord
├── id: Long
├── organizationId: Long                         unique, immutable
├── legalRegistrationType: LegalRegistrationType
├── registrationDate: LocalDate
├── businessRegistrationNumber: String
├── taxIdentificationNumber: String              encrypted, nullable until requested
├── businessBvn: String                           encrypted
├── businessDescription: String
├── industry: SupportedIndustry
├── website: String                               nullable
├── contact: BusinessContactData
├── serviceAgreement: ServiceAgreementData
├── currentStep: ComplianceStep
├── stepProgress: Map<ComplianceStep, StepStatus>
├── anchorBusinessCustomerId: String              unique, nullable before provider creation
├── eligibilityStatus: AtlasHubEligibilityStatus
├── anchorVerificationStatus: AnchorVerificationStatus
├── status: ComplianceStatus                      effective platform status
├── failureCode: String                            nullable
├── rejectionReason: String                       nullable
├── submittedAt: ZonedDateTime                    nullable
├── approvedAt: ZonedDateTime                     nullable
├── createdAt: ZonedDateTime
├── updatedAt: ZonedDateTime
└── version: Long
```

**Key business methods:**

- `updateBusinessRegistration(...)`
- `updateContactAndAddresses(...)`
- `replaceOfficers(...)`
- `saveComplianceDocument(...)`
- `acceptServiceAgreement(ipAddress, acceptedAt, termsVersion)`
- `evaluateEligibility(policy)`
- `submit()`
- `recordAnchorCustomerCreated(anchorCustomerId, officerMappings)`
- `recordVerificationTriggered()`
- `recordRequiredDocuments(requirements)`
- `recordDocumentApproved(anchorDocumentId)`
- `recordDocumentRejected(anchorDocumentId, reason)`
- `recordAnchorApproved()`
- `recordAnchorRejected(reason)`
- `recordAnchorError(code, message)`
- `suspend(reason)`
- `reinstate()`

**Domain rules:**

- Only policy-enabled registration types and industries may proceed.
- The organization country must be supported by the active banking programme; the initial Anchor programme is Nigerian/NGN.
- Required local sections must be valid before provider submission.
- The five `ComplianceStep` values are `BUSINESS_PROFILE`, `CONTACT_INFO`, `OWNERS_AND_OFFICERS`, `COMPLIANCE_DOCUMENTS`, and `SERVICE_AGREEMENT`.
- `StepStatus` is `NOT_STARTED`, `IN_PROGRESS`, `COMPLETE`, or `ACTION_REQUIRED`.
- A provider document request can move only `COMPLIANCE_DOCUMENTS` from `COMPLETE` back to `ACTION_REQUIRED`; it does not erase the other steps.
- An Anchor business customer is created at most once per organization.
- Provider customer and document identifiers are globally unique when present.
- `APPROVED` requires `ELIGIBLE`, accepted current terms, and Anchor status `APPROVED`.
- An AtlasHub administrator cannot override an Anchor rejection into banking approval.
- Rejection or suspension never deletes application, document, event, or audit history.

### `BusinessOfficer` (Entity)

```
BusinessOfficer
├── id: Long
├── complianceRecordId: Long
├── role: OfficerRole                           OWNER | DIRECTOR
├── firstName: String
├── middleName: String                           nullable
├── lastName: String
├── maidenName: String                           nullable
├── nationality: String
├── dateOfBirth: LocalDate
├── email: EmailAddress
├── phoneNumber: PhoneNumber
├── residentialAddress: AddressData
├── bvn: String                                  encrypted
├── title: String
├── percentageOwned: BigDecimal                  OWNER only
├── anchorOfficerId: String                      nullable
└── verificationStatus: OfficerVerificationStatus
```

Minimum officer/owner composition is defined by registration-type policy, not by controller code. `percentageOwned` is accepted only for `OWNER` records.

### `ComplianceDocumentRequirement` (Entity)

```
ComplianceDocumentRequirement
├── id: Long
├── complianceRecordId: Long
├── anchorDocumentId: String                     unique when present
├── documentType: String
├── description: String
├── required: Boolean
├── source: RequirementSource                    PREFLIGHT | ANCHOR_CUSTOMER
├── status: DocumentStatus                       REQUESTED | UPLOADED | UNDER_REVIEW | APPROVED | REJECTED
├── storageObjectKey: String                      nullable until upload
├── textValue: String                             RC/TIN-style textual value, encrypted if sensitive
├── rejectionReason: String                       nullable
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

Documents remain in AtlasHub-controlled object storage. The Anchor adapter streams the file to the provider's multipart upload endpoint. Public or permanent document URLs are not stored.

---

## 4. Status Model

### `AtlasHubEligibilityStatus`

`PENDING`, `ELIGIBLE`, `ACTION_REQUIRED`, `INELIGIBLE`

### `AnchorVerificationStatus`

`NOT_CREATED`, `CUSTOMER_CREATED`, `VERIFICATION_TRIGGERED`, `AWAITING_DOCUMENTS`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `ERROR`

### `ComplianceStatus`

`NOT_STARTED`, `IN_PROGRESS`, `SUBMITTED`, `ACTION_REQUIRED`, `UNDER_REVIEW`, `APPROVED`, `REJECTED`, `SUSPENDED`

The effective status is recomputed after relevant transitions. Provider status is retained independently so AtlasHub can distinguish a local policy failure, missing user action, provider review, provider rejection, and technical failure.

---

## 5. Anchor Port

```java
public interface AnchorCompliancePort {
    AnchorBusinessCustomerResult createBusinessCustomer(BusinessCustomerRequest request);
    void triggerBusinessVerification(String anchorCustomerId);
    List<AnchorDocumentRequirement> previewDocumentRequirements(
        String registrationType, LocalDate registrationDate);
    List<AnchorDocumentRequirement> fetchCustomerDocumentRequirements(
        String anchorCustomerId);
    void uploadDocument(
        String anchorCustomerId,
        String anchorDocumentId,
        StoredDocument document,
        String textValue);
    AnchorBusinessCustomerDetails fetchBusinessCustomer(String anchorCustomerId);
}
```

Anchor JSON:API request/response types stay in infrastructure. Domain/application code receives normalized records. Calls use bounded timeouts, retry only safe failures, and preserve the local submission when the provider is unavailable.

---

## 6. Application Flow

### Local Collection

- `InitializeComplianceRecordCommand`
- `UpdateBusinessRegistrationCommand`
- `UpdateContactAndAddressesCommand`
- `ReplaceBusinessOfficersCommand`
- `UploadComplianceDocumentCommand`
- `AcceptServiceAgreementCommand`
- `SubmitComplianceCommand`

`SubmitComplianceHandler`:

1. Loads the record with optimistic locking.
2. Validates all five AtlasHub collection steps and the current service-agreement version.
3. Evaluates registration type, industry, country, and programme policy.
4. Rejects locally when the organization is ineligible; Anchor is not called.
5. Persists `SUBMITTED` and writes `ComplianceSubmittedEvent` to the outbox.

### Provider Orchestration

`ComplianceSubmittedListener`/orchestrator:

1. Creates the Anchor `BusinessCustomer` if `anchorBusinessCustomerId` is absent.
2. Persists the returned customer ID and officer-ID mappings.
3. Triggers business verification.
4. Marks the record `UNDER_REVIEW` unless documents are immediately requested.
5. Never repeats customer creation after a timeout without first reconciling by the AtlasHub correlation/idempotency reference.

### Dynamic Documents

When `customer.identification.awaitingDocument` arrives:

1. Verify the webhook signature and deduplicate the provider event ID.
2. Find the existing record by `anchorBusinessCustomerId`.
3. Upsert the returned document requirements and Anchor document IDs.
4. Match already saved Step 4 documents by document type and upload them to Anchor.
5. If every requirement is matched, keep the application under provider review.
6. If any requirement is missing or rejected, mark Step 4 incomplete, set `anchorVerificationStatus = AWAITING_DOCUMENTS`, set effective status `ACTION_REQUIRED`, and notify the organization of only the outstanding requirements.

Document approval/rejection events update the matching requirement. A rejected document returns the journey to `ACTION_REQUIRED`; it does not discard the other approved documents.

### Final Decision

- `customer.identification.approved` calls `recordAnchorApproved()`.
- `customer.identification.rejected` calls `recordAnchorRejected(reason)`.
- `customer.identification.error` records a retryable or terminal provider error without pretending it is a compliance rejection.
- `OrganizationComplianceApprovedEvent` is emitted only when the effective status first becomes `APPROVED`.

---

## 7. Events

All local events are published on `compliance-events` and written to the outbox in the same transaction as the aggregate change.

| Event | Published When | Main Consumers |
|---|---|---|
| `ComplianceRecordInitializedEvent` | Organization record initialized | Internal |
| `ComplianceSubmittedEvent` | Eligible local application submitted | Compliance provider orchestrator, notifications |
| `ComplianceActionRequiredEvent` | Provider requests/rejects a document | Notifications |
| `ComplianceProviderErrorEvent` | Provider processing fails | Operations, notifications when user action is needed |
| `OrganizationComplianceApprovedEvent` | Effective status becomes `APPROVED` | `pay:accounts`, billing, notifications |
| `OrganizationComplianceRejectedEvent` | Local policy or Anchor reaches terminal rejection | Notifications |
| `OrganizationComplianceSuspendedEvent` | Previously approved compliance is suspended/revoked | `pay:accounts`, `pay:ledger`, billing, notifications |
| `OrganizationComplianceReinstatedEvent` | A suspended organization satisfies all effective approval conditions again | `pay:accounts`, `pay:ledger`, billing, notifications |

`OrganizationComplianceApprovedEvent.payload`:

```
organizationId
anchorBusinessCustomerId
legalRegistrationType
country
currency
approvedAt
complianceVersion
```

`OrganizationComplianceSuspendedEvent.payload`:

```
organizationId
anchorBusinessCustomerId
reason
suspendedAt
```

`OrganizationComplianceReinstatedEvent` carries `organizationId`, `anchorBusinessCustomerId`, `reason`, and `reinstatedAt`. It restores only the compliance restriction; it does not clear organization-ban, risk, or manual restrictions.

---

## 8. Webhook Listeners

| Anchor event | Action |
|---|---|
| `customer.identification.awaitingDocument` | Upsert customer-specific requirements and request user action |
| `document.approved` | Mark one requirement approved |
| `document.rejected` | Mark one requirement rejected with reason |
| `customer.identification.approved` | Record Anchor approval and recompute effective status |
| `customer.identification.rejected` | Record terminal provider rejection |
| `customer.identification.error` | Record provider error for retry/reconciliation |

Unknown Anchor customers/documents are quarantined for reconciliation. A webhook must never silently create an approved local compliance record.

---

## 9. Query Port

```java
public interface ComplianceQueryPort {
    ComplianceDecision getDecision(Long organizationId);
    boolean isApproved(Long organizationId); // compatibility convenience
    ComplianceStatus getStatus(Long organizationId);
}

public record ComplianceDecision(
    Long organizationId,
    ComplianceStatus status,
    boolean canProvisionBanking,
    String reasonCode,
    String anchorBusinessCustomerId
) {}
```

Consumers gate financial actions on `canProvisionBanking` or `APPROVED`, not on the existence of an Anchor customer ID alone.

---

## 10. Presentation API

| Method | Path | Permission | Purpose |
|---|---|---|---|
| `GET` | `/api/v1/compliance` | `compliance:read` | Five-step progress, provider status, and outstanding requirements |
| `PUT` | `/api/v1/compliance/business` | `compliance:manage` | Save Step 1: business profile and registration data |
| `PUT` | `/api/v1/compliance/contact` | `compliance:manage` | Save Step 2: contact and addresses |
| `PUT` | `/api/v1/compliance/officers` | `compliance:manage` | Save Step 3: validated officer/owner set |
| `POST` | `/api/v1/compliance/documents/{requirementId}` | `compliance:manage` | Save Step 4: requested file/text value |
| `POST` | `/api/v1/compliance/agreement` | `compliance:manage` | Save Step 5: accept current terms |
| `POST` | `/api/v1/compliance/submit` | `compliance:submit` | Submit the completed five-step application |

Responses redact BVN, TIN, identity numbers, storage keys, and provider secrets.

---

## 11. Persistence, Security, and Delivery

- `ComplianceRecordJpa` uses `@Version` optimistic locking.
- Officers and document requirements use normalized tables, not JSON blobs.
- BVN, TIN, identity numbers, and sensitive textual document values are encrypted at application level.
- Logs, events, metrics, and exception messages never contain raw sensitive identifiers.
- Object-store keys are private. Access uses short-lived pre-signed URLs only where necessary.
- Provider credentials and AtlasHub FBO identifiers come from environment-aware secure configuration.
- Webhooks require signature validation against the raw request body before JSON parsing.
- Inbox deduplication keys on the Anchor event ID plus consumer name.
- Every provider transition and manual operational action is audit logged.
- Outbox delivery is used for approval, rejection, action-required, and suspension events.

---

## 12. Integration With Banking Provisioning

`pay:accounts` starts organization banking provisioning only from `OrganizationComplianceApprovedEvent`.

```
effective compliance APPROVED
  → create Anchor CURRENT deposit account for anchorBusinessCustomerId
  → create business subaccount under AtlasHub FBO for the same customer
  → mark OrganizationBankingProfile ACTIVE when both are active
  → publish OrganizationBankingActivatedEvent
  → bootstrap AtlasHub internal ledger accounts
```

Compliance suspension freezes/suspends financial capability but preserves all history. Permanent external account closure requires a separate authorized banking closure workflow.

---

## 13. Migration From the Existing Design

1. Add legal-registration fields and the supported-industry mapping to `accounts`.
2. Add new compliance status columns without reinterpreting existing `APPROVED` rows.
3. Create normalized officer and document-requirement tables.
4. Migrate the existing single owner into an `OWNER` entity where data is complete.
5. Preserve old settlement-account data for audit, but remove it from the approval gate.
6. Reconcile every legacy approved organization with Anchor before granting `canProvisionBanking`.
7. Replace empty admin KYC listeners with the signed Anchor webhook listeners above.
8. Keep the five-step journey, but replace the fragile `completedSteps.size() == 5` check with semantic readiness validation for each of the five steps.

No legacy record is automatically treated as Anchor-approved merely because the old local status was `APPROVED`.
