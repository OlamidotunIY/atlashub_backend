# Platform Auth Module Design (`atlashub-platform:auth`)

## 1. Domain Entities & Aggregates

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
  - `setupPassword(String hash)` — initial password setup for invited users
  - `changePassword(String currentHash, String newHash)` — validates current password before changing
  - `incrementFailedAttempts()` — auto-locks account after threshold (e.g., 5 consecutive failures)
  - `resetFailedAttempts()`
  - `lock()`
  - `unlock()`
  - `verifyEmail()`
  - `requirePasswordChange()`

**`Session` (Entity)**
- **Fields**:
  - `id`: Long
  - `userId`: Long
  - `refreshTokenHash`: String
  - `deviceInfo`: String
  - `ipAddress`: String
  - `expiresAt`: ZonedDateTime
  - `isRevoked`: Boolean
  - `createdAt`: ZonedDateTime
- **Methods**: `revoke()`

**`Verification` (Aggregate Root)**
Single-use codes for email verification, password resets, and 2FA.
- **Fields**:
  - `id`: Long
  - `userId`: Long
  - `code`: String (hashed)
  - `type`: `VerificationType` (EMAIL_VERIFICATION, PASSWORD_RESET, SETUP_TOKEN, TWO_FACTOR)
  - `expiresAt`: ZonedDateTime
  - `isUsed`: Boolean
- **Methods**: `consume()` (marks `isUsed = true`, throws if already used or expired)

## 2. Domain Events
- `AuthSessionCreatedEvent(Long sessionId, Long userId, String ipAddress, String deviceInfo)`
- `AuthSessionRevokedEvent(Long sessionId, Long userId)`
- `AuthVerificationCreatedEvent(Long verificationId, Long userId, String code, VerificationType type)`
- `AuthVerificationCompletedEvent(Long verificationId, Long userId, VerificationType type)`
- `AuthNewDeviceLoginEvent(Long userId, String deviceInfo, String ipAddress)`
- `AuthAccountLockedEvent(Long userId, String reason)`
- `AuthPasswordChangedEvent(Long userId)`

## 3. Exceptions & Errors
**`AuthErrorCode`**:
- `INVALID_CREDENTIALS`
- `ACCOUNT_LOCKED`, `ACCOUNT_NOT_FOUND`
- `TOKEN_EXPIRED`, `INVALID_TOKEN`, `TOKEN_ALREADY_USED`
- `VERIFICATION_CODE_INVALID`, `VERIFICATION_CODE_EXPIRED`
- `PASSWORD_NOT_SET` (invited user attempts login before setting password)
- `CURRENT_PASSWORD_INCORRECT`
- `EMAIL_NOT_VERIFIED`
- `SESSION_NOT_FOUND`, `SESSION_REVOKED`

## 4. Commands & Use Cases
- **`AuthenticateCommand(email, password, deviceInfo, ipAddress)`** → `AuthenticateUseCase`
  Validates password hash, checks account status, creates `Session`, issues JWT + refresh token. If device is new, publishes `AuthNewDeviceLoginEvent`.
- **`SetupPasswordCommand(Long userId, String setupToken, String newPassword)`** → `SetupPasswordUseCase`
  Used by invited members to set their initial password. Validates `Verification` of type `SETUP_TOKEN`, calls `AuthAccount.setupPassword()`.
- **`ChangePasswordCommand(Long userId, String currentPassword, String newPassword)`** → `ChangePasswordUseCase`
  Validates current password, calls `AuthAccount.changePassword()`.
- **`ForgotPasswordCommand(String email)`** → `ForgotPasswordUseCase`
  Looks up `AuthAccount` by email. Creates a `Verification` of type `PASSWORD_RESET`, publishes `AuthVerificationCreatedEvent` (triggers email with reset link).
- **`ResetPasswordCommand(Long userId, String resetToken, String newPassword)`** → `ResetPasswordUseCase`
  Consumes `Verification` of type `PASSWORD_RESET`, updates password hash.
- **`ResendVerificationCommand(Long userId, VerificationType type)`** → `ResendVerificationUseCase`
  Invalidates any prior unused `Verification` for the same user+type, creates a new one, publishes `AuthVerificationCreatedEvent`.
- **`CompleteVerificationCommand(Long userId, String code, VerificationType type)`** → `CompleteVerificationUseCase`
  Validates and consumes a `Verification`. For `EMAIL_VERIFICATION`, calls `AuthAccount.verifyEmail()`.
- **`RevokeSessionCommand(Long sessionId, Long userId)`** → `RevokeSessionUseCase`
  Revokes a single session (logout from one device).
- **`RevokeAllSessionsCommand(Long userId)`** → `RevokeAllSessionsUseCase`
  Revokes all active sessions for the user (logout from all devices).
- **`RefreshTokenCommand(String refreshToken, String ipAddress)`** → `RefreshTokenUseCase`
  Validates refresh token, rotates it (invalidates old, issues new), returns new JWT + refresh token.

## 5. Queries
- `GetAuthenticatedUserQuery(String jwtToken)` → Decodes and validates JWT, returns principal context (userId, orgId, roles).
- `ListActiveSessionsQuery(Long userId)` → `List<SessionResult>` (Active, non-expired, non-revoked sessions.)

## 6. Listeners
- `UserCreatedListener`: Listens to `UserCreated` (from Identity). Creates a skeleton `AuthAccount` with `status = REQUIRE_PASSWORD_CHANGE` and no `passwordHash`. Creates a `Verification` of type `SETUP_TOKEN` and publishes `AuthVerificationCreatedEvent` to trigger the welcome/setup email.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `AuthAccount` when modifying password hashes, status, or `failedLoginAttempts` to prevent concurrent login races from corrupting the attempt counter.

### Inbox & Outbox Patterns
- **Outbox**: Publishes `AuthVerificationCreatedEvent` and `AuthNewDeviceLoginEvent` to trigger notification emails reliably.
- **Inbox (`EventDeliveryTracker`)**: Idempotent processing of `UserCreated` events to guarantee exactly one `AuthAccount` is created per user.
