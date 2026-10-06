# Authentication Module Design (`atlashub-platform:authentication`)

## Role & Purpose

The `authentication` module is the **human-authentication engine** of AtlasHub. It owns passwords, login sessions, JWT issuance, token refresh, logout, password recovery, email verification, and trusted devices. IAM owns API-key lifecycle and HMAC credential verification; the executable application owns the HTTP security filters.

The `auth` module does **not** decide what a user is allowed to do (that is `iam`) or whether their organization is verified (that is `compliance`). It answers one question: *are you who you say you are?*

---

## 1. Features

### Human Authentication — JWT + Refresh Tokens
AtlasHub uses a two-token model:
- **Access Token** (JWT, 15-minute expiry): Signed with RS256. Carries user, active organization, session, environment, and permission claims. Signature validation is local, then the `sid` is checked against Redis on every request; authentication never reads the session database.
- **Refresh Token** (opaque UUID, 30-day expiry): Stored in Redis with metadata (device fingerprint, IP, user agent). Used once to obtain a new access token + refresh token pair (rotation).

### Session Fingerprinting
Every refresh token is bound to a **device fingerprint**. If a refresh token is presented with a different fingerprint, the runtime session is invalidated and the request is rejected.

### Token Revocation (Logout)
On logout, the runtime session is invalidated in Redis while its database audit row is retained. The current access token is added to a **Redis revocation list** (TTL = remaining access token lifetime). Every incoming request checks both revocation and the Redis session, without querying the database.

### Password Management
- Passwords are hashed with **bcrypt (cost factor 12)** before storage
- Password reset via time-limited secure token (emailed via `notifications`)

### Email Verification
New accounts require email verification before login is permitted. A verification token is sent via `notifications`. Accounts owns the durable `User.emailVerified` state; Authentication checks it through `UserQueryPort`.

### Device Trust
Login from an untrusted device fingerprint triggers an email OTP challenge. Trusted devices are recorded and do not require re-verification for 90 days.

---

## 2. Machine-to-Machine Authentication (B2B API)

Businesses that integrate with AtlasHub APIs (e.g., a merchant's e-commerce store initiating payments) use **API Key + HMAC-SHA256 request signing**.

This is more secure than a plain API key because:
1. The API key is never sent in plaintext in the request body
2. The HMAC signature covers the full request (method + URL + body + timestamp) — a captured request cannot be replayed
3. A nonce prevents the same request from being replayed even within the timestamp window

### How It Works

**Step 1 — API Key Issuance** (managed by IAM; HMAC verification is performed by IAM's authentication service from the main HTTP filter):
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
2. Resolve the active key in IAM and decrypt its protected secret only for signature verification
3. Reject if `|current_time - timestamp| > 300` seconds (5-minute replay window)
4. Check nonce in Redis: if already seen, reject as replay attack (store nonce with TTL = 10 minutes)
5. Recompute the HMAC and compare using a constant-time comparison (`MessageDigest.isEqual`)
6. If valid, inject `HmacAuthPrincipal(orgId, environment)` into the security context

---

## 3. Domain Entities & Aggregates

### `AuthAccount` (Aggregate Root)

Created by reacting to `UserCreated`. One `AuthAccountJpa` audit/persistence record per `User`.

```
AuthAccount
├── id: Long
├── userId: Long                        ← references accounts:User
├── password: String                    ← bcrypt(cost=12) hash
├── failedLoginAttempts: Integer        ← resets to 0 on success
├── lockedUntil: ZonedDateTime          ← nullable, set after 5 failed attempts
├── lastLoginAt: ZonedDateTime          ← nullable
├── lastLoginIp: String                 ← nullable
└── createdAt: ZonedDateTime
```

**Business Methods:**
- `updatePassword(String passwordHash)` — stores a newly validated and encoded password
- `recordEmailVerified()` — publishes the verification result for accounts
- `recordFailedLogin()` — increments counter; locks account at 5 consecutive failures
- `recordSuccessfulLogin(String ip)` — resets counter, records IP, sets `lastLoginAt`
- `unlock()` — admin action to unlock an account

**Domain Rules:**
- Account is locked after 5 consecutive failed login attempts for 30 minutes
- A locked account cannot log in, even with the correct password
- Email verification state is owned by accounts and read through `UserQueryPort` before login
- OAuth provider tokens are not stored in `AuthAccount`; OAuth is not implemented yet

---

### `Session` (Redis runtime record with a database audit copy)

Not a JPA entity — stored entirely in Redis.

```
Session (Redis keys: `session:{tokenHash}` and `session:id:{id}`)
├── id: Long              ← included in access-token `sid`
├── tokenHash: String     ← SHA-256 of the opaque refresh token; raw token is never persisted
├── userId: Long
├── organizationId: Long
├── environment: TEST | LIVE
├── deviceFingerprint: String   ← SHA-256(userAgent + deviceType)
├── issuedAt: ZonedDateTime
├── expiresAt: ZonedDateTime    ← Redis TTL matches this
└── tokenFamilyId: String
```

Redis is the exclusive authentication/session-validation source. The JPA row is retained only as an audit record and is not read for authentication. Logout, rotation, member deactivation, and organization bans invalidate Redis records without deleting audit rows.

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
  "sid": "98765",                         // Redis session ID
  "jti": "a1b2c3d4-...",                  // unique token ID (for revocation)
  "permissions": ["pay:charges:create",   // RBAC permission claims from iam
                   "commerce:orders:read"],
  "env": "LIVE",                          // API environment (LIVE or TEST)
  "iat": 1726543200,
  "exp": 1726544100                       // 15 minutes
}
```

The environment and active organization are trusted token context. Controllers must read both from `AuthenticatedPrincipal`; request DTOs must not accept them. Switching organization or environment rotates the session and refresh token, recalculates permissions, and issues a new access token. Switching to LIVE requires approved compliance. TEST and LIVE state, provider resources, idempotency keys, and ledger postings never fall back into each other.

---

## 5. Domain Events

| Event | Published When | Consumed By |
|---|---|---|
| `OtpVerificationCreated` | Email verification, password reset, or new-device OTP is issued | `notifications` |
| `AuthEmailVerifiedEvent` | User verifies their email, or an invited user is trusted | `accounts`, `notifications` |
| `AuthAccountLocked` | 5 failed login attempts | `notifications`, `audit` |
| `ActiveOrganizationSwitchedEvent` | Active organization and permissions are rotated | `accounts` |

---

## 6. Exceptions & Errors

Authentication uses module-owned exceptions for invalid credentials, locked accounts, required email verification, invalid or expired verification tokens, invalid sessions, unavailable registration credentials, live-mode compliance gating, and domain invariant failures. HMAC/API-key failures belong to IAM and the main security filter rather than this module.

---

## 7. Commands & Use Cases

### Login & Sessions
- `LoginCommand(email, password, deviceFingerprint, ipAddress, userAgent)` → `LoginUseCase`
  - Flow: find `AuthAccountJpa` → check lock → verify password → if new device, challenge → issue access token + refresh token
- `RefreshTokenCommand(refreshToken, deviceFingerprint)` → `RefreshTokenUseCase`
  - Flow: look up token in Redis → verify device fingerprint → rotate (delete old, issue new pair)
- `LogoutCommand(userId, sessionId, accessTokenJti, accessTokenExpiresAt)` → `LogoutHandler`
  - All values come from the validated access-token principal, never the request body
  - Flow: invalidate the Redis session → add access token JTI to the revocation set for its remaining lifetime
- `LogoutAllDevicesCommand(userId)` → `LogoutAllDevicesUseCase`
  - Flow: invalidate every session referenced by `user:{userId}:sessions`; database audit rows remain

### Password Management
- `InitiatePasswordResetCommand(email)` → `InitiatePasswordResetUseCase`
- `ResetPasswordCommand(token, newPassword)` → `ResetPasswordUseCase`
- `ChangePasswordCommand(userId, currentPassword, newPassword)` → `ChangePasswordUseCase`

### Email Verification
- `SendVerificationEmailCommand(userId)` → `SendVerificationEmailUseCase`
- `VerifyEmailCommand(token)` → `VerifyEmailUseCase`

### Trusted Devices
- `AuthorizeDeviceCommand(email, otp, deviceFingerprint, deviceName, ipAddress)` → `AuthorizeDeviceHandler`
  - Public challenge-completion endpoint; verifies the pending device OTP and creates or renews the trusted device. The user then retries login with their password.
- `RevokeTrustedDeviceCommand(userId, deviceId)` → `RevokeTrustedDeviceUseCase`

---

## 8. Queries

- `GetActiveSessions(userId)` → `List<SessionResult>` — all active devices/refresh tokens
- `GetTrustedDevicesQuery(userId)` → `List<TrustedDeviceResult>`

---

## 9. Listeners

- **`UserAuthenticationListener`** — topic: `user-events`, groupId: `authentication-group`
  - Listens for `UserCreated` event (published by `accounts` module after registration)
  - Payload consumed: `aggregateId` (userId), `payload.email`, `payload.credentialReference`, `payload.isInvited`
  - Calls `AuthAccountHandler` which:
    1. Checks idempotency — if `AuthAccount` already exists for that email, returns immediately
    2. Claims the one-time credential, validates it, hashes it, and creates `AuthAccount`
    3. Issues an email verification OTP via `OtpVerificationIssuer.issue()` → creates a `Verification` aggregate
    4. Stores OTP for async transmission via `OtpTransmissionPort.storeForTransmission()`
    5. Saves `AuthAccount` + `Verification` (outbox publishes `OtpVerificationCreated` event)
  - Invited users skip OTP verification and publish the verified-email event immediately
- **`MemberDeactivatedListener`**: topic=`iam-events`. Event=`MemberDeactivatedEvent`. Payload: `userId`, `organizationId`, `deactivatedAt`. Revokes all refresh tokens for this user (adds to Redis revocation set). Forces logout immediately on next request.
- **`OrganizationBannedListener`**: topic=`admin-events`. Event=`OrganizationBannedEvent`. Payload: `organizationId`, `reason`, `bannedAt`. Revokes ALL active sessions for every user in the banned organization. Users are immediately logged out and cannot re-authenticate until the ban is lifted.


---

## 10. Security Design

### Constant-Time Comparison
All token comparisons (HMAC verification, password reset token matching) use `MessageDigest.isEqual()` to prevent timing side-channel attacks.

### Redis Key Design
```
session:{sha256(token)}          → Session JSON (TTL: 30 days)
session:id:{sessionId}           → Session JSON (TTL: 30 days)
user:{userId}:sessions           → Set<tokenHash> (TTL: active-session window)
organization:{orgId}:sessions    → Set<tokenHash> (TTL: active-session window)
revoke:{jti}                     → "1" (TTL: remaining access token lifetime)
nonce:{nonce}                    → "1" (TTL: 10 minutes, HMAC replay prevention)
lock:{userId}                    → failedAttemptCount (TTL: 30 minutes)
device:trust:{userId}:{fp}       → TrustedDevice JSON (TTL: 90 days)
```

### OAuth (future)
OAuth is deliberately not active yet. A future Google implementation must use authorization-code flow with PKCE, validate issuer/audience/nonce, map the verified provider subject to an AtlasHub user, and keep provider access/refresh/ID tokens out of the `AuthAccount` table. Provider tokens must use a dedicated encrypted credential store with rotation and revocation rather than raw JPA columns.

### Token Rotation Security
Refresh token rotation means every use creates a new token and invalidates the old Redis session. Any later use of the old token is rejected and requires re-authentication.

### HMAC Signing
See [setup/api-key-hmac-auth.md](../setup/api-key-hmac-auth.md) for the complete implementation guide.
