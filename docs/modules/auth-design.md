# Auth Module Design (`atlashub-platform:auth`)

## Role & Purpose

The `auth` module is the **authentication engine** of AtlasHub. It owns the complete lifecycle of how a user proves their identity to the platform: passwords, login sessions, JWT tokens, token refresh, logout, and password recovery. It also owns **machine-to-machine authentication** — the API key + HMAC signing mechanism that external systems use to call AtlasHub APIs on behalf of an organization.

The `auth` module does **not** decide what a user is allowed to do (that is `iam`) or whether their organization is verified (that is `compliance`). It answers one question: *are you who you say you are?*

---

## 1. Features

### Human Authentication — JWT + Refresh Tokens
AtlasHub uses a two-token model:
- **Access Token** (JWT, 15-minute expiry): Stateless, signed with RS256. Carries user claims (`userId`, `activeOrganizationId`, `permissions`). Verified locally — no database hit on every request.
- **Refresh Token** (opaque UUID, 30-day expiry): Stored in Redis with metadata (device fingerprint, IP, user agent). Used once to obtain a new access token + refresh token pair (rotation).

### Session Fingerprinting
Every refresh token is bound to a **device fingerprint** (hash of user agent + device type). If a refresh token is presented from a different device fingerprint, it is rejected and an alert is sent. This detects token theft.

### Token Revocation (Logout)
On logout, the refresh token is deleted from Redis. The current access token is added to a **Redis revocation list** (TTL = remaining access token lifetime). Every incoming request checks the revocation list. This gives AtlasHub the security of session-based auth with the scalability of stateless JWTs.

### Password Management
- Passwords are hashed with **bcrypt (cost factor 12)** before storage
- Password reset via time-limited secure token (emailed via `notifications`)
- Forced password change after admin reset

### Email Verification
New accounts require email verification before login is permitted. A verification token is sent via `notifications`. The `AuthAccount.emailVerified` flag gates access.

### Device Trust & Suspicious Login Detection
Login from a new device/IP triggers a verification challenge (email OTP). Trusted devices are recorded and do not require re-verification for 90 days.

---

## 2. Machine-to-Machine Authentication (B2B API)

Businesses that integrate with AtlasHub APIs (e.g., a merchant's e-commerce store initiating payments) use **API Key + HMAC-SHA256 request signing**.

This is more secure than a plain API key because:
1. The API key is never sent in plaintext in the request body
2. The HMAC signature covers the full request (method + URL + body + timestamp) — a captured request cannot be replayed
3. A nonce prevents the same request from being replayed even within the timestamp window

### How It Works

**Step 1 — API Key Issuance** (managed by `iam` module, but the key hash and signing logic live in `auth`):
```
publicKey:  atlas_pk_live_abc123...   (shown once, safe to expose — identifies the org)
secretKey:  atlas_sk_live_xyz789...   (shown once only, used for signing — never transmitted)
```

**Step 2 — Building the Signature** (done by the merchant's server):
```
timestamp = current Unix timestamp in seconds (e.g., "1726543200")
nonce     = random UUID (e.g., "f47ac10b-58cc-4372-a567-0e02b2c3d479")
bodyHash  = SHA-256(request body as string)       # empty string for no body
message   = METHOD + "\n" + PATH + "\n" + timestamp + "\n" + nonce + "\n" + bodyHash
signature = HMAC-SHA256(message, secretKey)       # hex-encoded
```

**Step 3 — Request Headers**:
```http
Authorization: AtlasHmac publicKey=atlas_pk_live_abc123,timestamp=1726543200,nonce=f47ac10b-...,signature=a1b2c3d4...
Content-Type: application/json
```

**Step 4 — Server-Side Verification** (`HmacSignatureFilter`):
1. Extract `publicKey`, `timestamp`, `nonce`, `signature` from the Authorization header
2. Look up the org's `secretKeyHash` from Redis cache (fallback: DB)
3. Reject if `|current_time - timestamp| > 300` seconds (5-minute replay window)
4. Check nonce in Redis: if already seen, reject as replay attack (store nonce with TTL = 10 minutes)
5. Recompute the HMAC and compare using a constant-time comparison (`MessageDigest.isEqual`)
6. If valid, inject `HmacAuthPrincipal(orgId, environment)` into the security context

---

## 3. Domain Entities & Aggregates

### `AuthAccountJpa` (Aggregate Root)

Created by reacting to `UserCreatedEvent`. One `AuthAccountJpa` per `User`.

```
AuthAccount
├── id: Long
├── userId: Long                        ← references accounts:User
├── passwordHash: String                ← bcrypt(cost=12)
├── emailVerified: Boolean              ← false until verification link clicked
├── emailVerificationToken: String      ← nullable, short-lived
├── emailVerificationExpiresAt: ZonedDateTime ← nullable
├── passwordResetToken: String          ← nullable, hashed, short-lived
├── passwordResetExpiresAt: ZonedDateTime ← nullable
├── failedLoginAttempts: Integer        ← resets to 0 on success
├── lockedUntil: ZonedDateTime          ← nullable, set after 5 failed attempts
├── lastLoginAt: ZonedDateTime          ← nullable
├── lastLoginIp: String                 ← nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `setPassword(String rawPassword)` — hashes and stores the password
- `verifyEmail(String token)` — validates token, sets `emailVerified = true`, clears token
- `initiatePasswordReset(String token, ZonedDateTime expiresAt)` — stores reset token
- `resetPassword(String token, String newRawPassword)` — validates token, updates hash, clears token
- `recordFailedLogin()` — increments counter; locks account at 5 consecutive failures
- `recordSuccessfulLogin(String ip)` — resets counter, records IP, sets `lastLoginAt`
- `unlock()` — admin action to unlock an account

**Domain Rules:**
- Account is locked after 5 consecutive failed login attempts for 30 minutes
- A locked account cannot log in, even with the correct password
- Email must be verified before login is permitted

---

### `Session` (Value Object stored in Redis)

Not a JPA entity — stored entirely in Redis.

```
RefreshToken (Redis key: "refresh:{token}")
├── token: String          ← opaque UUID, stored as SHA-256 hash in Redis
├── userId: Long
├── orgId: Long
├── deviceFingerprint: String   ← SHA-256(userAgent + deviceType)
├── issuedAt: ZonedDateTime
├── expiresAt: ZonedDateTime    ← Redis TTL matches this
└── lastUsedAt: ZonedDateTime
```

---

### `TrustedDevice` (Entity)

```
TrustedDevice
├── id: Long
├── userId: Long
├── deviceFingerprint: String
├── deviceName: String           ← e.g., "Chrome on Windows"
├── lastSeenIp: String
├── trustedAt: ZonedDateTime
└── expiresAt: ZonedDateTime     ← 90 days from trustedAt
```

---

## 4. JWT Structure

Access tokens are signed with RS256 (asymmetric). The private key is held only by the auth service. All other modules verify using the public key (available at `/.well-known/jwks.json`).

**JWT Claims:**
```json
{
  "sub": "12345",                         // userId
  "org": "67890",                         // activeOrganizationId
  "jti": "a1b2c3d4-...",                  // unique token ID (for revocation)
  "permissions": ["pay:charges:create",   // RBAC permission claims from iam
                   "commerce:orders:read"],
  "env": "LIVE",                          // API environment (LIVE or TEST)
  "iat": 1726543200,
  "exp": 1726544100                       // 15 minutes
}
```

---

## 5. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `AuthAccountCreatedEvent` | `UserCreatedEvent` received from accounts | `notifications` (send verification email) |
| `EmailVerifiedEvent` | User clicks verification link | `accounts` (log), `notifications` (welcome message) |
| `PasswordResetInitiatedEvent` | User requests password reset | `notifications` (send reset email) |
| `SuspiciousLoginDetectedEvent` | Login from new device/IP | `notifications` (email security alert), `audit` |
| `AccountLockedEvent` | 5 failed login attempts | `notifications` (email user), `audit` |

---

## 6. Exceptions & Errors

**`AuthErrorCode`**:
- `INVALID_CREDENTIALS`, `ACCOUNT_LOCKED`, `ACCOUNT_NOT_FOUND`
- `EMAIL_NOT_VERIFIED`, `EMAIL_ALREADY_VERIFIED`
- `INVALID_VERIFICATION_TOKEN`, `VERIFICATION_TOKEN_EXPIRED`
- `INVALID_RESET_TOKEN`, `RESET_TOKEN_EXPIRED`
- `INVALID_REFRESH_TOKEN`, `REFRESH_TOKEN_EXPIRED`, `REFRESH_TOKEN_DEVICE_MISMATCH`
- `HMAC_SIGNATURE_INVALID`, `HMAC_TIMESTAMP_EXPIRED`, `HMAC_NONCE_REPLAYED`
- `API_KEY_NOT_FOUND`, `API_KEY_REVOKED`

---

## 7. Commands & Use Cases

### Login & Sessions
- `LoginCommand(email, password, deviceFingerprint, ipAddress, userAgent)` → `LoginUseCase`
  - Flow: find `AuthAccountJpa` → check lock → verify password → if new device, challenge → issue access token + refresh token
- `RefreshTokenCommand(refreshToken, deviceFingerprint)` → `RefreshTokenUseCase`
  - Flow: look up token in Redis → verify device fingerprint → rotate (delete old, issue new pair)
- `LogoutCommand(userId, refreshToken, accessTokenJti)` → `LogoutUseCase`
  - Flow: delete refresh token from Redis → add access token JTI to revocation set (TTL = remaining lifetime)
- `LogoutAllDevicesCommand(userId)` → `LogoutAllDevicesUseCase`
  - Flow: delete all Redis keys matching `refresh:userId:*` → add all active access token JTIs to revocation set

### Password Management
- `InitiatePasswordResetCommand(email)` → `InitiatePasswordResetUseCase`
- `ResetPasswordCommand(token, newPassword)` → `ResetPasswordUseCase`
- `ChangePasswordCommand(userId, currentPassword, newPassword)` → `ChangePasswordUseCase`

### Email Verification
- `SendVerificationEmailCommand(userId)` → `SendVerificationEmailUseCase`
- `VerifyEmailCommand(token)` → `VerifyEmailUseCase`

### Trusted Devices
- `TrustDeviceCommand(userId, deviceFingerprint, deviceName)` → `TrustDeviceUseCase`
- `RevokeTrustedDeviceCommand(userId, deviceId)` → `RevokeTrustedDeviceUseCase`

---

## 8. Queries

- `GetActiveSessions(userId)` → `List<SessionResult>` — all active devices/refresh tokens
- `GetTrustedDevicesQuery(userId)` → `List<TrustedDeviceResult>`

---

## 9. Listeners

- **`UserCreatedListener`**: Listens to `UserCreatedEvent` from `accounts`. Creates `AuthAccountJpa` with a hashed temporary password (or no password if SSO-only). Triggers email verification.
- **`InvitationAcceptedListener`**: Listens to `InvitationAcceptedEvent` from `iam`. If the invited user is new, sets their `emailVerified = true` (invitation acceptance implies email confirmation).

---

## 10. Security Design

### Constant-Time Comparison
All token comparisons (HMAC verification, password reset token matching) use `MessageDigest.isEqual()` to prevent timing side-channel attacks.

### Redis Key Design
```
refresh:{sha256(token)}          → RefreshToken JSON (TTL: 30 days)
revoke:{jti}                     → "1" (TTL: remaining access token lifetime)
nonce:{nonce}                    → "1" (TTL: 10 minutes, HMAC replay prevention)
lock:{userId}                    → failedAttemptCount (TTL: 30 minutes)
device:trust:{userId}:{fp}       → TrustedDevice JSON (TTL: 90 days)
```

### Token Rotation Security
Refresh token rotation means every use of a refresh token creates a new token and invalidates the old one. If an attacker steals a refresh token and uses it AFTER the legitimate user, the legitimate user's next refresh attempt will fail (old token is gone). This triggers `SuspiciousLoginDetectedEvent` and forces re-authentication.

### HMAC Signing
See [setup/api-key-hmac-auth.md](../setup/api-key-hmac-auth.md) for the complete implementation guide.
