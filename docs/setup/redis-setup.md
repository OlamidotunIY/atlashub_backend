# Redis Setup

## Overview

AtlasHub uses Redis for four distinct purposes: **session/token management**, **token revocation**, **rate limiting**, and **distributed caching**. Each purpose uses a carefully designed key namespace to prevent collisions.

---

## Redis Key Namespaces

| Namespace | Purpose | TTL |
|---|---|---|
| `refresh:{sha256(token)}` | Refresh token storage | 30 days |
| `revoke:{jti}` | Access token revocation list | Remaining token lifetime |
| `nonce:{nonce}` | HMAC nonce replay prevention | 10 minutes |
| `ratelimit:ip:{ip}:{window}` | IP-based rate limiting counter | 1 minute |
| `ratelimit:org:{orgId}:{window}` | Org-based rate limiting counter | 1 minute |
| `apikey:pk:{publicKey}` | API key cache (from DB) | 5 minutes |
| `lock:{resource}:{id}` | Distributed lock for critical sections | Operation-dependent |
| `device:trust:{userId}:{fingerprint}` | Trusted device record | 90 days |
| `otp:{userId}:{purpose}` | One-time passwords (email OTP, 2FA) | 10 minutes |

---

## Session Management (Refresh Tokens)

```java
// Storing a refresh token
String tokenHash = sha256(rawToken);
String key = "refresh:" + tokenHash;

RefreshTokenData data = new RefreshTokenData(userId, orgId, deviceFingerprint, issuedAt);
redis.opsForValue().set(key, objectMapper.writeValueAsString(data), Duration.ofDays(30));

// Retrieving on refresh
String tokenHash = sha256(rawTokenFromRequest);
String json = redis.opsForValue().get("refresh:" + tokenHash);
if (json == null) throw new BusinessRuleException(AuthErrorCode.INVALID_REFRESH_TOKEN);
RefreshTokenData data = objectMapper.readValue(json, RefreshTokenData.class);

// Token rotation — delete old, create new
redis.delete("refresh:" + oldTokenHash);
// ... create new token
```

---

## Token Revocation

On logout, the current access token's `jti` (JWT ID) is added to the revocation list:

```java
// On logout
long remainingLifetime = jwtClaims.expiresAt().getEpochSecond() - Instant.now().getEpochSecond();
if (remainingLifetime > 0) {
    redis.opsForValue().set("revoke:" + jwtClaims.jti(), "1", Duration.ofSeconds(remainingLifetime));
}

// On every authenticated request (in JwtAuthFilter)
String jti = jwtClaims.jti();
if (Boolean.TRUE.equals(redis.hasKey("revoke:" + jti))) {
    throw new BusinessRuleException(AuthErrorCode.TOKEN_REVOKED);
}
```

---

## Rate Limiting (Sliding Window)

AtlasHub uses a **sliding window counter** in Redis. Both IP-level and org-level limits apply independently.

```java
@Component
public class RateLimiter {

    private final RedisTemplate<String, String> redis;

    private static final int IP_LIMIT     = 300;   // requests per minute per IP
    private static final int ORG_LIMIT    = 1000;  // requests per minute per org

    public void checkIpLimit(String ip) {
        String key = "ratelimit:ip:" + ip + ":" + currentWindowMinute();
        Long count = redis.opsForValue().increment(key);
        if (count == 1) redis.expire(key, Duration.ofMinutes(2));  // expire after 2 windows
        if (count > IP_LIMIT) {
            throw new RateLimitException("IP rate limit exceeded. Retry in " + secondsUntilNextWindow() + "s");
        }
    }

    public void checkOrgLimit(Long orgId) {
        String key = "ratelimit:org:" + orgId + ":" + currentWindowMinute();
        Long count = redis.opsForValue().increment(key);
        if (count == 1) redis.expire(key, Duration.ofMinutes(2));
        if (count > ORG_LIMIT) {
            throw new RateLimitException("Organization rate limit exceeded");
        }
    }

    private String currentWindowMinute() {
        return String.valueOf(Instant.now().getEpochSecond() / 60);
    }

    private long secondsUntilNextWindow() {
        long second = Instant.now().getEpochSecond();
        return 60 - (second % 60);
    }
}
```

### Rate Limit HTTP Response Headers
Every response includes:
```http
X-RateLimit-Limit: 300
X-RateLimit-Remaining: 247
X-RateLimit-Reset: 1726543260   ← Unix timestamp when the window resets
Retry-After: 34                  ← only present when rate limited (429 response)
```

---

## API Key Cache

```java
@Component
public class ApiKeyCacheAdapter implements ApiKeyQueryPort {

    private final RedisTemplate<String, String> redis;
    private final SpringDataApiKeyRepository jpa;

    private static final Duration CACHE_TTL = Duration.ofMinutes(5);

    @Override
    public Optional<ApiKeyDto> findByPublicKey(String publicKey) {
        String cacheKey = "apikey:pk:" + publicKey;
        String cached = redis.opsForValue().get(cacheKey);

        if (cached != null) {
            return Optional.of(objectMapper.readValue(cached, ApiKeyDto.class));
        }

        Optional<ApiKeyDto> dto = jpa.findByPublicKey(publicKey).map(this::toDto);
        dto.ifPresent(d -> redis.opsForValue().set(cacheKey, objectMapper.writeValueAsString(d), CACHE_TTL));
        return dto;
    }
}
```

When an API key is revoked, the cache entry is invalidated immediately:
```java
// In RevokeApiKeyUseCase
apiKeyRepository.save(apiKey);   // mark revoked in DB
redis.delete("apikey:pk:" + apiKey.getPublicKey());  // bust cache
publishEvents(apiKey, publisher);
```

---

## Distributed Lock

For operations where pessimistic DB locking is insufficient (e.g., multi-step operations spanning services), Redis-based distributed locking ensures mutual exclusion:

```java
@Component
public class RedisDistributedLock {

    private final RedisTemplate<String, String> redis;
    private static final String LOCK_PREFIX = "lock:";

    public boolean acquire(String resource, String lockId, Duration ttl) {
        String key = LOCK_PREFIX + resource;
        return Boolean.TRUE.equals(redis.opsForValue().setIfAbsent(key, lockId, ttl));
    }

    public void release(String resource, String lockId) {
        String key = LOCK_PREFIX + resource;
        String current = redis.opsForValue().get(key);
        if (lockId.equals(current)) {   // only release if we own the lock
            redis.delete(key);
        }
    }
}
```

---

## Configuration

```yaml
spring:
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      password: ${REDIS_PASSWORD:}
      timeout: 2000ms
      lettuce:
        pool:
          max-active: 20
          max-idle: 10
          min-idle: 5
          max-wait: 1000ms
```

### Docker Compose
```yaml
redis:
  image: redis:7.2-alpine
  ports:
    - "6379:6379"
  command: redis-server --requirepass ${REDIS_PASSWORD} --maxmemory 512mb --maxmemory-policy allkeys-lru
  volumes:
    - redis-data:/data
```

`allkeys-lru` eviction policy: when Redis hits its memory limit, it evicts the least recently used keys first. This is appropriate for AtlasHub's Redis usage since all critical session data has explicit TTLs and the cache is a write-through cache backed by the DB.
