# Platform Auth Module Design (`atlashub-platform:auth`)

## 1. Domain Entities & Aggregates

**`AuthAccount` (Aggregate Root)**
- **Fields**: 
  - `id`: Long
  - `userId`: Long (references `User` in Identity)
  - `email`: String
  - `passwordHash`: String
  - `isEmailVerified`: Boolean
  - `failedLoginAttempts`: Integer
  - `status`: `AuthStatus` (ACTIVE, LOCKED, REQUIRE_PASSWORD_CHANGE)
- **Methods**: `changePassword(String hash)`, `incrementFailedAttempts()`, `lock()`, `verifyEmail()`

**`Session` (Entity)**
- **Fields**: 
  - `id`: Long
  - `userId`: Long
  - `refreshTokenHash`: String
  - `deviceInfo`: String
  - `ipAddress`: String
  - `expiresAt`: LocalDateTime
  - `isRevoked`: Boolean
- **Methods**: `revoke()`

**`Verification` (Aggregate Root)**
- **Fields**: 
  - `id`: Long
  - `userId`: Long
  - `code`: String
  - `type`: `VerificationType` (EMAIL_VERIFICATION, PASSWORD_RESET, 2FA)
  - `expiresAt`: LocalDateTime

## 2. Domain Events
- `AuthSessionCreatedEvent(Long sessionId, Long userId, String ipAddress)`
- `AuthSessionRevokedEvent(Long sessionId)`
- `AuthVerificationCreatedEvent(Long verificationId, String code, VerificationType type)`
- `AuthVerificationCompletedEvent(Long verificationId)`
- `AuthNewDeviceLoginEvent(Long userId, String deviceInfo, String ipAddress)`

## 3. Exceptions & Errors
**`AuthErrorCode`**:
- `INVALID_CREDENTIALS`
- `ACCOUNT_LOCKED`
- `TOKEN_EXPIRED`, `INVALID_TOKEN`
- `VERIFICATION_CODE_INVALID`

## 4. Commands & Use Cases
- `AuthenticateUseCase` (Validates password, issues JWT and Refresh tokens, creates `Session`).
- `SetupPasswordUseCase` (Sets initial password for invited members).
- `CompleteVerificationUseCase` (Validates 2FA or email confirmation codes).
- `RevokeSessionUseCase` (Logs out a user from specific or all devices).

## 5. Queries
- `GetAuthenticatedUserUseCase` (Extracts context from JWT).

## 6. Listeners
- `UserCreatedListener`: Listens to `UserCreated` (from Identity) and automatically creates a skeleton `AuthAccount` requiring password setup.

## 7. Distributed Architecture & Transaction Guarantees
### Locking Strategy
- **Optimistic Locking (`@Version`)**: Applied to `AuthAccount` (specifically when modifying password hashes or status).

### Inbox & Outbox Patterns
- **Outbox**: Publishes `AuthNewDeviceLoginEvent` to trigger a notification email to the user for security auditing.
- **Inbox**: Uses the `EventDeliveryTracker` to process `UserCreated` events idempotently.
