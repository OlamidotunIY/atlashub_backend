# API Key & HMAC Request Signing

## Overview

When an external system (a merchant's backend server) calls the AtlasHub API on behalf of an organization, it uses **API Key + HMAC-SHA256 request signing**. This is more secure than passing a plain secret key in an Authorization header because:

1. The secret key is **never transmitted** — only a cryptographic signature derived from it
2. The signature **covers the entire request** — method, path, body, timestamp — so a captured request cannot be modified and reused
3. A **timestamp + nonce** prevent replay attacks: a captured valid request cannot be replayed even within seconds

This design is modelled after AWS Signature Version 4 and is used by Paystack, Flutterwave, and other major African payment APIs.

---

## Key Pair Structure

Every organization can have up to 2 active key pairs per environment (LIVE and TEST):

```
publicKey:  atlas_pk_live_Ab3xYz9qRs...   (24 Base62 characters)
secretKey:  atlas_sk_live_Mn7pKd2vWe...   (40 Base62 characters) — shown ONCE only
```

- The `publicKey` identifies the organization and environment. It is safe to include in logs and error messages.
- The `secretKey` is shown **once** at creation time and never stored — only its SHA-256 hash is stored in the database.
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

        // 4. Look up the secret key hash (Redis cache → DB fallback)
        ApiKeyDto apiKey = apiKeyQueryPort.findByPublicKey(publicKey)
            .orElseThrow(() -> sendErrorAndReturn(response, 401, "API_KEY_NOT_FOUND"));
        if (apiKey.isRevoked()) {
            sendError(response, 401, "API_KEY_REVOKED", "This API key has been revoked");
            return;
        }

        // 5. Read and cache the request body (needed for body hash)
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String body = new String(cachedRequest.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // 6. Recompute the signature
        String method   = request.getMethod().toUpperCase();
        String path     = request.getRequestURI();
        String bodyHash = sha256Hex(body);
        String message  = method + "\n" + path + "\n" + timestamp + "\n" + nonce + "\n" + bodyHash;
        String expectedSig = hmacSha256Hex(message, apiKey.secretKeyHash());
        // NOTE: secretKeyHash is the SHA-256 hash of the secret. We compare HMAC(message, hash(secret)).
        // The original secret is not stored anywhere on our servers.

        // 7. Constant-time comparison
        if (!MessageDigest.isEqual(
                hexToBytes(expectedSig),
                hexToBytes(signature))) {
            sendError(response, 401, "HMAC_SIGNATURE_INVALID", "Request signature does not match");
            return;
        }

        // 8. Set security context
        HmacAuthPrincipal principal = new HmacAuthPrincipal(apiKey.organizationId(), apiKey.environment());
        SecurityContextHolder.getContext().setAuthentication(
            new HmacAuthenticationToken(principal, List.of()));

        filterChain.doFilter(cachedRequest, response);  // use cached body for downstream processing
    }

    private String hmacSha256Hex(String message, String keyHex) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(hexToBytes(keyHex), "HmacSHA256"));
        return bytesToHex(mac.doFinal(message.getBytes(StandardCharsets.UTF_8)));
    }
}
```

---

## Key Generation

```java
// In IssueApiKeyUseCase
private KeyPair generateKeyPair(ApiEnvironment environment) {
    String envSuffix = environment == ApiEnvironment.LIVE ? "live" : "test";
    String publicKey  = "atlas_pk_" + envSuffix + "_" + Base62.random(24);
    String secretKey  = "atlas_sk_" + envSuffix + "_" + Base62.random(40);
    String secretHash = sha256Hex(secretKey);   // only hash stored

    return new KeyPair(publicKey, secretKey, secretHash);
}
```

The `secretKey` is returned in the API response **exactly once**. After the response is sent, `secretKey` is not stored anywhere in AtlasHub. The merchant must save it securely. If lost, they must revoke the key and issue a new one.

---

## Key Rotation

To rotate an API key:
1. Issue a new key pair (`IssueApiKeyCommand`)
2. Update the application server with the new keys
3. Revoke the old key (`RevokeApiKeyCommand`)

AtlasHub supports up to 2 active key pairs per environment per organization, allowing zero-downtime rotation: the new key is issued and deployed before the old one is revoked.

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
