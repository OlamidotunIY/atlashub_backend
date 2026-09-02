# Platform Auth Module Design (`atlashub-platform:auth`)

## Role & Purpose

The Auth module is the **security gateway** of the platform. While the `identity` module knows *who* someone is, the Auth module decides *whether they can get in*. It owns every aspect of credential management and session lifecycle: password hashing and verification, email verification, account locking, login session tracking, JWT issuance, and token rotation.

No other module owns passwords or session tokens. When any part of the system needs to verify a user's identity (e.g., "is this refresh token valid?"), it goes through `auth`. When identity needs to send a user their first password setup link, it publishes a `UserCreated` event and Auth reacts by creating the verification code.

Auth does **not** enforce what a user can do — that is the job of RBAC (enforced in each module at the use case level). Auth only answers: *"Is this the right person?"*

---

## 1. Features

### Standard Authentication Flow
When a user logs in with email and password:
1. Auth looks up the `AuthAccount` by email.
2. Validates the password hash.
3. Checks account status (LOCKED, REQUIRE_PASSWORD_CHANGE).
4. On success, creates a `Session` record, issues a signed JWT and a hashed refresh token.
5. If the device fingerprint is new, publishes `AuthNewDeviceLoginEvent` to trigger a "new device login" security email.
6. On failure, increments `failedLoginAttempts`. After 5 consecutive failures, the account is automatically locked until unlocked by the user via password reset or by an admin.

### Invitation-Based Password Setup
When a user is invited via the `identity` module's `InviteMemberUseCase`, they do not yet have a password. The `UserCreated` event (consumed by Auth) automatically creates a skeleton `AuthAccount` with `status = REQUIRE_PASSWORD_CHANGE` and issues a one-time `SETUP_TOKEN` via the `Verification` aggregate. The invited user clicks the email link, which calls `SetupPasswordUseCase` to set their initial password and activate their account.

### Password Reset
A user who forgets their password triggers `ForgotPasswordUseCase`, which creates a `Verification` of type `PASSWORD_RESET`. An email is sent with a secure reset link. The user clicks the link and submits a new password via `ResetPasswordUseCase`, which validates and consumes the verification token before updating the hash.

### Email Verification
On first registration, a `Verification` of type `EMAIL_VERIFICATION` is issued. Until verified, certain features (e.g., inviting members, submitting compliance) are gated. `CompleteVerificationUseCase` validates the code, calls `AuthAccount.verifyEmail()`, and marks the account verified.

### Session & Token Management
Every successful login creates a `Session` record tracking the device info, IP address, and refresh token hash. Sessions expire and can be revoked individually (logout from one device) or in bulk (logout from all devices). The `RefreshTokenUseCase` rotates refresh tokens on each use, invalidating the old token immediately to prevent replay attacks.

---

## 2. Domain Entities & Aggregates

**`AuthAccount` (Aggregate Root)**
- **Fields**:
  - `id`: Long
  - `userId`: Long (references `User` in Identity)
  - `email`: String
  - `passwordHash`: String (nullable — null until `SetupPasswordUseCase` is called for invited users)
  - `isEmailVerified`: Boolean
  - `failedLoginAttempts`: Integer
  - `lastFailedLoginAt`: ZonedDateTime (nullable)
  - `lockedUntil`: ZonedDateTime (nullable — for time-based unlock)
  - `status`: `AuthStatus` (ACTIVE, LOCKED, REQUIRE_PASSWORD_CHANGE)
- **Methods**:
  - `setupPassword(String hash)`
  - `changePassword(String currentHash, String newHash)`
  - `incrementFailedAttempts()` — auto-locks after threshold (5 failures)
  - `resetFailedAttempts()`
  - `lock()`, `unlock()`, `verifyEmail()`, `requirePasswordChange()`

**`Session` (Entity)**
- **Fields**: `id`, `userId`, `refreshTokenHash`, `deviceInfo`, `ipAddress`, `expiresAt`, `isRevoked`, `createdAt`
- **Methods**: `revoke()`

**`Verification` (Aggregate Root)**
Single-use codes for email verification, password resets, and 2FA.
- **Fields**: `id`, `userId`, `code` (hashed), `type` (`VerificationType`: EMAIL_VERIFICATION, PASSWORD_RESET, SETUP_TOKEN, TWO_FACTOR), `expiresAt`, `isUsed`
- **Methods**: `consume()` — marks `isUsed = true`, throws if already used or expired

---

## 3. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `AuthVerificationCreatedEvent` | Verification code issued | `notifications` (send OTP/reset/setup email) |
| `AuthNewDeviceLoginEvent` | Login from unrecognized device | `notifications` (send security alert email) |
| `AuthAccountLockedEvent` | Account locked after failed attempts | `notifications` (alert user), `audit` (log security event) |
| `AuthSessionCreatedEvent` | Successful login | `audit` (log) |
| `AuthSessionRevokedEvent` | Session revoked (logout) | `audit` (log) |
| `AuthPasswordChangedEvent` | Password updated | `notifications` (send confirmation email) |

---

## 4. Exceptions & Errors

**`AuthErrorCode`**:
- `INVALID_CREDENTIALS`
- `ACCOUNT_LOCKED`, `ACCOUNT_NOT_FOUND`
- `TOKEN_EXPIRED`, `INVALID_TOKEN`, `TOKEN_ALREADY_USED`
- `VERIFICATION_CODE_INVALID`, `VERIFICATION_CODE_EXPIRED`
- `PASSWORD_NOT_SET` (invited user attempts login before setting password)
- `CURRENT_PASSWORD_INCORRECT`
- `EMAIL_NOT_VERIFIED`
- `SESSION_NOT_FOUND`, `SESSION_REVOKED`

---

## 5. Commands & Use Cases

- **`AuthenticateCommand(email, password, deviceInfo, ipAddress)`** → `AuthenticateUseCase`
  Validates credentials, checks status, creates `Session`, issues JWT + refresh token.
- **`SetupPasswordCommand(Long userId, String setupToken, String newPassword)`** → `SetupPasswordUseCase`
  Used by invited members to set their initial password.
- **`ChangePasswordCommand(Long userId, String currentPassword, String newPassword)`** → `ChangePasswordUseCase`
- **`ForgotPasswordCommand(String email)`** → `ForgotPasswordUseCase`
  Creates a `Verification` of type `PASSWORD_RESET`, publishes `AuthVerificationCreatedEvent`.
- **`ResetPasswordCommand(Long userId, String resetToken, String newPassword)`** → `ResetPasswordUseCase`
- **`ResendVerificationCommand(Long userId, VerificationType type)`** → `ResendVerificationUseCase`
- **`CompleteVerificationCommand(Long userId, String code, VerificationType type)`** → `CompleteVerificationUseCase`
- **`RevokeSessionCommand(Long sessionId, Long userId)`** → `RevokeSessionUseCase`
- **`RevokeAllSessionsCommand(Long userId)`** → `RevokeAllSessionsUseCase`
- **`RefreshTokenCommand(String refreshToken, String ipAddress)`** → `RefreshTokenUseCase`
  Validates and rotates the refresh token, returns new JWT + refresh token pair.

---

## 6. Queries

- `GetAuthenticatedUserQuery(String jwtToken)` → Decodes and validates JWT, returns principal context (userId, orgId, roles).
- `ListActiveSessionsQuery(Long userId)` → `List<SessionResult>` (Active, non-expired, non-revoked sessions.)

---

## 7. Listeners

- **`UserCreatedListener`**: Listens to `UserCreated` (from Identity). Creates a skeleton `AuthAccount` with `status = REQUIRE_PASSWORD_CHANGE` and no `passwordHash`. Creates a `Verification` of type `SETUP_TOKEN` and publishes `AuthVerificationCreatedEvent` to trigger the welcome/setup email. Uses Inbox to guarantee exactly one `AuthAccount` per user.

---

## 8. Distributed Architecture & Transaction Guarantees

### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `AuthAccount` when modifying password hashes, status, or `failedLoginAttempts` to prevent concurrent login races from corrupting the attempt counter.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `AuthVerificationCreatedEvent` and `AuthNewDeviceLoginEvent` to trigger notification emails reliably.
- **Inbox (`EventDeliveryTracker`)**: Idempotent processing of `UserCreated` events to guarantee exactly one `AuthAccount` is created per user. Without this, a retry storm could create duplicate auth accounts.
