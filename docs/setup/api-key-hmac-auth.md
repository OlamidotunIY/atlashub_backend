# API Key & HMAC Request Signing

## Overview

When an external system (a merchant's backend server) calls the AtlasHub API on behalf of an organization, it uses **API Key + HMAC-SHA256 request signing**. This is more secure than passing a plain secret key in an Authorization header because:

1. The secret key is **never transmitted** — only a cryptographic signature derived from it
2. The signature **covers the entire request** — method, path, body, timestamp — so a captured request cannot be modified and reused
3. A **timestamp + nonce** prevent replay attacks: a captured valid request cannot be replayed even within seconds

This design is modelled after AWS Signature Version 4 and is used by Paystack, Flutterwave, and other major African payment APIs.

---

## Key Pair Structure

Every key belongs to exactly one environment (LIVE or TEST):

```
publicKey:  atlas_pk_live_Ab3xYz9qRs...   (URL-safe random value)
secretKey:  atlas_sk_live_Mn7pKd2vWe...   (URL-safe random value) — shown ONCE only
```

- The `publicKey` identifies the organization and environment. It is safe to include in logs and error messages.
- The `secretKey` is shown **once** at creation time. AtlasHub stores only an encrypted ciphertext so the server can recompute HMAC signatures; plaintext is never stored.
- TEST keys operate against test accounts with simulated payment providers. LIVE keys operate with real money.

---

## Signing Algorithm (Client Side)

The merchant's server signs every request before sending. Steps:

### Step 1 — Collect Request Components
```
method    = "POST"
path      = "/api/v1/pay/charges"                  ← URL path only, no query string
timestamp = "1726543200"                            ← Unix seconds, current time
nonce     = "f47ac10b-58cc-4372-a567-0e02b2c3d479" ← random UUID, used once
body      = '{"amount":25000,"currency":"NGN",...}' ← raw request body string (empty string if no body)
```

### Step 2 — Compute Body Hash
```
bodyHash = SHA-256(body)                            ← hex-encoded
         = "a3f5c..."
```

### Step 3 — Build the Canonical Message String
```
message = method + "\n"
        + path + "\n"
        + timestamp + "\n"
        + nonce + "\n"
        + bodyHash

Example:
"POST\n/api/v1/pay/charges\n1726543200\nf47ac10b-58cc-4372-a567-0e02b2c3d479\na3f5c..."
```

### Step 4 — Compute HMAC-SHA256
```
signature = HMAC-SHA256(message, secretKey)         ← hex-encoded
```

### Step 5 — Build the Authorization Header
```http
Authorization: AtlasHmac publicKey=atlas_pk_live_Ab3xYz9qRs,timestamp=1726543200,nonce=f47ac10b-58cc-4372-a567-0e02b2c3d479,signature=a1b2c3d4e5f6...
Content-Type: application/json
```

---

## Verification Algorithm (Server Side)

The `HmacSignatureVerificationFilter` runs on all API requests with `AtlasHmac` scheme:

```java
@Component
public class HmacSignatureVerificationFilter extends OncePerRequestFilter {

    private final ApiKeyQueryPort apiKeyQueryPort;
    private final RedisTemplate<String, String> redis;
    private static final int TIMESTAMP_TOLERANCE_SECONDS = 300;  // 5 minutes

    @Override
    protected void doFilterInternal(HttpServletRequest request, ...) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("AtlasHmac ")) {
            filterChain.doFilter(request, response);  // not HMAC auth — pass to JWT filter
            return;
        }

        // 1. Parse header components
        Map<String, String> params = parseHmacHeader(authHeader);
        String publicKey = params.get("publicKey");
        long timestamp  = Long.parseLong(params.get("timestamp"));
        String nonce     = params.get("nonce");
        String signature = params.get("signature");

        // 2. Timestamp validation — reject if older than 5 minutes
        long now = Instant.now().getEpochSecond();
        if (Math.abs(now - timestamp) > TIMESTAMP_TOLERANCE_SECONDS) {
            sendError(response, 401, "HMAC_TIMESTAMP_EXPIRED",
                "Request timestamp is outside the 5-minute tolerance window");
            return;
        }

        // 3. Nonce check — prevent replay within the tolerance window
        String nonceKey = "nonce:" + nonce;
        Boolean isNewNonce = redis.opsForValue().setIfAbsent(nonceKey, "1",
            Duration.ofSeconds(TIMESTAMP_TOLERANCE_SECONDS + 60));  // TTL slightly longer than window
        if (!Boolean.TRUE.equals(isNewNonce)) {
            sendError(response, 401, "HMAC_NONCE_REPLAYED", "This nonce has already been used");
            return;
        }

        // 4. Read and cache the request body (needed for body hash)
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String body = new String(cachedRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 5. Build the canonical message
        String method   = request.getMethod().toUpperCase();
        String path     = request.getRequestURI();
        String bodyHash = sha256Hex(body);
        String message  = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + bodyHash;
        // 6. IAM owns lookup, revocation, AES-GCM decryption, constant-time HMAC
        //    verification, and effective permission resolution behind this shared read port.
        var apiKey = apiKeyQueryPort.authenticate(publicKey, message, signature)
            .orElseThrow(() -> sendErrorAndReturn(response, 401, "HMAC_AUTHENTICATION_FAILED"));

        // 7. Set security context with the resolved permissions
        var authorities = apiKey.permissions().stream()
            .map(SimpleGrantedAuthority::new).toList();
        AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            null, apiKey.organizationId(), apiKey.environment(), null, null, null);
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(principal, null, authorities));
        SecurityContextHolder.getContext().setAuthentication(
            new HmacAuthenticationToken(principal, List.of()));

        filterChain.doFilter(cachedRequest, response);  // use cached body for downstream processing
    }

}
```

---

## Key Generation

```java
// In IssueApiKeyUseCase
private KeyPair generateKeyPair(ApiEnvironment environment) {
    String envSuffix = environment == ApiEnvironment.LIVE ? "live" : "test";
    String publicKey  = "atlas_pk_" + envSuffix + "_" + secureRandomUrlToken(18);
    String secretKey  = "atlas_sk_" + envSuffix + "_" + secureRandomUrlToken(32);
    String secretCiphertext = aesGcmProtector.encrypt(secretKey); // only ciphertext stored

    return new KeyPair(publicKey, secretKey, secretCiphertext);
}
```

The `secretKey` is returned in the API response **exactly once**. AtlasHub never stores plaintext; it stores AES-256-GCM ciphertext protected by `ATLASHUB_API_KEY_MASTER_KEY` because HMAC verification requires recovery of the signing secret. If the client loses the secret, it must rotate or replace the key.

---

## Key Rotation

`POST /api/v1/iam/api-keys/{id}/rotate` invokes `RotateApiKeyCommand`. In one transaction it locks and revokes the selected key, creates a replacement with the same environment and role binding, and returns the replacement secret once. The optional request name replaces the display name; otherwise the prior name is preserved.

Successful HMAC authentication publishes `ApiKeyAuthenticatedEvent`. IAM consumes it asynchronously to update `lastUsedAt`, keeping the authentication read path free of synchronous audit writes.

---

## Client Implementation Examples

### Node.js (Express Server)
```javascript
const crypto = require('crypto');

function signRequest(method, path, body, secretKey) {
    const timestamp = Math.floor(Date.now() / 1000).toString();
    const nonce = crypto.randomUUID();
    const bodyHash = crypto.createHash('sha256')
        .update(body || '').digest('hex');
    const message = [method.toUpperCase(), path, timestamp, nonce, bodyHash].join('\n');
    const signature = crypto.createHmac('sha256', secretKey)
        .update(message).digest('hex');

    return `AtlasHmac publicKey=${publicKey},timestamp=${timestamp},nonce=${nonce},signature=${signature}`;
}

// Usage:
const response = await fetch('https://api.atlashub.io/api/v1/pay/charges', {
    method: 'POST',
    headers: {
        'Authorization': signRequest('POST', '/api/v1/pay/charges', body, secretKey),
        'Content-Type': 'application/json'
    },
    body: body
});
```

### Python
```python
import hashlib, hmac, uuid, time

def sign_request(method, path, body, secret_key):
    timestamp = str(int(time.time()))
    nonce = str(uuid.uuid4())
    body_hash = hashlib.sha256((body or '').encode()).hexdigest()
    message = f"{method.upper()}\n{path}\n{timestamp}\n{nonce}\n{body_hash}"
    signature = hmac.new(secret_key.encode(), message.encode(), hashlib.sha256).hexdigest()
    return f"AtlasHmac publicKey={public_key},timestamp={timestamp},nonce={nonce},signature={signature}"
```
