# Compliance (KYC) Module Design (`atlashub-platform:compliance`)

## Role & Purpose

The `compliance` module owns the **KYC (Know Your Customer) and regulatory verification journey** for every organization on AtlasHub. This is a dedicated bounded context separate from `accounts` because KYC is a regulatory workflow — it has its own state machine, step tracking, document management, review process, and compliance events. It is not a property of identity.

Think of it this way: an `Organization` in `accounts` is a data record of a business. The `compliance` module answers the question: *has that business been verified to legally transact money?*

The compliance status acts as a **platform-wide gate**. Until an organization reaches `ComplianceStatus.APPROVED`, they cannot:
- Issue or receive payments through `pay`
- Access advanced features gated by KYC level
- Increase transaction limits

Other modules check compliance status by calling `ComplianceQueryPort.isApproved(orgId)` — a synchronous port implemented here and injected via `atlashub-shared`.

---

## 1. Features

### Multi-Step KYC Journey
Compliance is completed in 5 ordered steps. Each step must be completed before the next can be submitted. The `ComplianceRecord` aggregate tracks which steps are done.

```
STEP 1 — BUSINESS_PROFILE
  Business description, industry, annual transaction volume, expected monthly volume,
  number of staff

STEP 2 — CONTACT_INFO
  Support email, dispute resolution email, WhatsApp contact number, physical address

STEP 3 — OWNER_IDENTITY
  Business owner's BVN, NIN, date of birth, government-issued ID type and number,
  government ID front image URL, government ID back image URL, selfie image URL

STEP 4 — SETTLEMENT_ACCOUNT
  Settlement bank account (bank code + account number),
  auto-verified account name via Paystack's bank account verification API

STEP 5 — SERVICE_AGREEMENT
  Electronic acceptance of AtlasHub's Terms of Service, Privacy Policy,
  and Money Transmission Agreement. Records timestamp and IP of acceptance.
```

### Document Storage
All uploaded documents (ID images, selfies) are stored as URLs in the `compliance` module. The actual files are stored in an object store (S3/R2) via the `file-storage` infrastructure service.

### Admin Review
Once all 5 steps are complete and submitted, `compliance` publishes `ComplianceSubmittedEvent`, which the `admin` module reacts to by creating a review task. The admin can then approve or reject with a detailed reason.

### Rejection & Re-submission
On rejection, the organization receives the reason via `notifications`. They can correct the failing steps and re-submit. The `ComplianceRecord` tracks each review cycle.

### Compliance Levels (Future)
In the future, compliance will support tiered levels (Basic, Standard, Enhanced) with different transaction limits per tier. For MVP, only a single approval level exists.

---

## 2. Domain Entities & Aggregates

### `ComplianceRecord` (Aggregate Root)

One `ComplianceRecord` per `Organization`. Created when `OrganizationCreatedEvent` is received.

```
ComplianceRecord
├── id: Long
├── organizationId: Long                     ← immutable reference
├── status: ComplianceStatus                 ← NOT_STARTED → IN_PROGRESS → SUBMITTED → UNDER_REVIEW → APPROVED | REJECTED
├── currentStep: ComplianceStep              ← BUSINESS_PROFILE, CONTACT_INFO, OWNER_IDENTITY, SETTLEMENT_ACCOUNT, SERVICE_AGREEMENT
├── completedSteps: Set<ComplianceStep>      ← tracks which steps have been filled
├── reviewedBy: Long                         ← admin userId, nullable
├── reviewedAt: ZonedDateTime                ← nullable
├── rejectionReason: String                  ← nullable, set on rejection
├── submittedAt: ZonedDateTime               ← nullable
├── approvedAt: ZonedDateTime                ← nullable
├── createdAt: ZonedDateTime
└── updatedAt: ZonedDateTime
```

**Business Methods:**
- `updateBusinessProfile(BusinessProfileData data)` → validates, stores, marks step BUSINESS_PROFILE complete
- `updateContactInfo(ContactInfoData data)` → validates, stores, marks step CONTACT_INFO complete
- `updateOwnerIdentity(OwnerIdentityData data)` → validates, stores, marks step OWNER_IDENTITY complete
- `updateSettlementAccount(SettlementAccountData data)` → validates, stores, marks step SETTLEMENT_ACCOUNT complete
- `acceptServiceAgreement(String ipAddress, ZonedDateTime acceptedAt)` → marks step SERVICE_AGREEMENT complete
- `submit()` — guard: all 5 steps must be in `completedSteps`; transitions status to SUBMITTED → registers `ComplianceSubmittedEvent`
- `markUnderReview(Long adminId)` → transitions to UNDER_REVIEW
- `approve(Long adminId)` → transitions to APPROVED → registers `OrganizationComplianceApprovedEvent`
- `reject(Long adminId, String reason)` → transitions to REJECTED → registers `OrganizationComplianceRejectedEvent`
- `reopen()` → admin action to allow re-submission after rejection, transitions back to IN_PROGRESS

**Domain Rules:**
- Steps must be completed in order: a step cannot be submitted before its predecessor is complete
- Once APPROVED, status is immutable (cannot be reverted except by admin `reopen()`)
- Submission requires ALL 5 steps to be in `completedSteps`

---

### Embedded Value Objects on `ComplianceRecord`

**`BusinessProfileData`**
```
businessDescription: String
industry: String
annualTransactionVolume: String    ← e.g., "BELOW_1M", "1M_TO_10M", "ABOVE_10M"
expectedMonthlyVolume: Money
staffCount: Integer
```

**`ContactInfoData`**
```
supportEmail: EmailAddress
disputeEmail: EmailAddress
whatsappNumber: PhoneNumber
physicalAddress: AddressData       ← street, city, state, country
```

**`OwnerIdentityData`**
```
bvn: String                        ← 11-digit Nigerian BVN
nin: String                        ← optional
dateOfBirth: LocalDate
govIdType: GovernmentIdType        ← NIN_SLIP, DRIVERS_LICENSE, INTL_PASSPORT, VOTERS_CARD
govIdNumber: String
govIdFrontUrl: String
govIdBackUrl: String
selfieUrl: String
```

**`SettlementAccountData`**
```
bankCode: String
accountNumber: String
accountName: String                ← verified via Paystack name enquiry
```

**`ServiceAgreementData`**
```
acceptedAt: ZonedDateTime
ipAddress: String
termsVersion: String               ← version of the T&C document accepted
```

---

### `ComplianceStatus` (Enum)
```
NOT_STARTED  → Organization created but no KYC data submitted
IN_PROGRESS  → At least one step completed
SUBMITTED    → All 5 steps done, awaiting admin review
UNDER_REVIEW → Admin has picked up the task
APPROVED     → KYC verified, organization can transact
REJECTED     → Failed review — reason provided
```

### `ComplianceStep` (Enum)
`BUSINESS_PROFILE`, `CONTACT_INFO`, `OWNER_IDENTITY`, `SETTLEMENT_ACCOUNT`, `SERVICE_AGREEMENT`

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `ComplianceRecordInitializedEvent` | `OrganizationCreatedEvent` received | Internal only |
| `ComplianceStepCompletedEvent` | Any step completed | `notifications` (progress update to org) |
| `ComplianceSubmittedEvent` | All steps done, submitted | `admin` (create review task), `notifications` (confirm submission) |
| `OrganizationComplianceApprovedEvent` | Admin approves | `pay:accounts` (issue NUBAN), `billing` (unlock paid subscriptions), `notifications` (email org) |
| `OrganizationComplianceRejectedEvent` | Admin rejects | `notifications` (email org with rejection reason and instructions) |

---

## 4. Outbound Port (Open Host Service)

```java
// In atlashub-shared
public interface ComplianceQueryPort {
    boolean isApproved(Long organizationId);
    ComplianceStatus getStatus(Long organizationId);
}
```

This port is called synchronously by `pay` and `billing` before allowing financial operations, without importing any compliance internals.

---

## 5. Exceptions & Errors

**`ComplianceErrorCode`**:
- `COMPLIANCE_RECORD_NOT_FOUND`
- `STEP_OUT_OF_ORDER` — trying to complete Step 3 before Step 2
- `STEP_NOT_COMPLETE` — trying to submit when not all steps are done
- `ALREADY_SUBMITTED` — trying to re-submit when already SUBMITTED or APPROVED
- `ALREADY_APPROVED` — trying to modify a completed compliance record
- `SETTLEMENT_ACCOUNT_VERIFICATION_FAILED` — Paystack name check failed
- `INVALID_BVN_FORMAT`, `INVALID_NIN_FORMAT`

---

## 6. Commands & Use Cases

- `InitializeComplianceRecordCommand(orgId)` → `InitializeComplianceRecordUseCase` ← triggered by `OrganizationCreatedEvent`
- `UpdateBusinessProfileCommand(orgId, data)` → `UpdateBusinessProfileUseCase`
- `UpdateContactInfoCommand(orgId, data)` → `UpdateContactInfoUseCase`
- `UpdateOwnerIdentityCommand(orgId, data)` → `UpdateOwnerIdentityUseCase`
- `UpdateSettlementAccountCommand(orgId, bankCode, accountNumber)` → `UpdateSettlementAccountUseCase`
  - Calls Paystack name enquiry API to verify account name before saving
- `AcceptServiceAgreementCommand(orgId, ipAddress, termsVersion)` → `AcceptServiceAgreementUseCase`
- `SubmitComplianceCommand(orgId)` → `SubmitComplianceUseCase`
- `ApproveComplianceCommand(orgId, adminId)` → `ApproveComplianceUseCase` ← admin action
- `RejectComplianceCommand(orgId, adminId, reason)` → `RejectComplianceUseCase` ← admin action
- `ReopenComplianceCommand(orgId, adminId)` → `ReopenComplianceUseCase` ← admin override

---

## 7. Queries

- `GetComplianceStatusQuery(orgId)` → `ComplianceStatusResult` — current step, status, completed steps
- `GetComplianceDetailsQuery(orgId)` → `ComplianceDetailsResult` — full data per step (redacted for display)
- `ListPendingComplianceReviewsQuery(status, page)` → `List<ComplianceReviewResult>` ← admin use

---

## 8. Listeners

- **`OrganizationCreatedListener`**: Listens to `OrganizationCreatedEvent` from `accounts`. Calls `InitializeComplianceRecordUseCase` to bootstrap the compliance record.

---

## 9. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: On `ComplianceRecordJpaEntity`. Prevents concurrent step updates corrupting the record.
- **Guard at Domain Method Level**: `submit()` checks all steps before transitioning state — concurrency cannot bypass this because the version lock ensures only one concurrent update wins.

### Outbox & Inbox
- **Outbox**: `OrganizationComplianceApprovedEvent` and `OrganizationComplianceRejectedEvent` must be delivered reliably — they trigger NUBAN issuance in `pay`. Written to outbox within the approval transaction.
- **Inbox**: `OrganizationCreatedEvent` is processed idempotently — if replayed, the use case checks if a `ComplianceRecord` already exists for the org before creating another.

### Security
- All KYC document URLs must be pre-signed URLs from the object store (time-limited read access). They are never exposed directly in API responses — only admin-level endpoints return the raw URLs.
- BVN/NIN values are stored encrypted at rest (AES-256 via application-level encryption before writing to DB).
