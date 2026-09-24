# Docker Setup — Local Development

## Prerequisites

- Docker Desktop 4.x
- Docker Compose v2
- At minimum 8GB RAM allocated to Docker

---

## Service Inventory

| Service | Image | Port | Purpose |
|---|---|---|---|
| `postgres` | `postgres:16-alpine` | 5432 | Primary operational database |
| `timescaledb` | `timescale/timescaledb:2.14.2-pg16` | 5433 | Analytics time-series database |
| `redis` | `redis:7.2-alpine` | 6379 | Sessions, token revocation, rate limiting |
| `kafka` | `confluentinc/cp-kafka:7.6.0` | 9092 | Event bus |
| `zookeeper` | `confluentinc/cp-zookeeper:7.6.0` | 2181 | Kafka coordination |
| `kafka-setup` | `confluentinc/cp-kafka:7.6.0` | — | One-shot topic creation |
| `elasticsearch` | `elasticsearch:8.13.0` | 9200 | Full-text search |
| `atlashub` | (built from source) | 8080 | The application |

---

## docker-compose.yml

```yaml
version: "3.9"

services:

  postgres:
    image: postgres:16-alpine
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: atlashub
      POSTGRES_USER: ${POSTGRES_USER:-atlashub}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD:-secret}
    volumes:
      - pg-data:/var/lib/postgresql/data
      - ./infra/postgres/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER:-atlashub}"]
      interval: 10s
      timeout: 5s
      retries: 5

  timescaledb:
    image: timescale/timescaledb:2.14.2-pg16
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: atlashub_analytics
      POSTGRES_USER: ${TIMESCALE_USER:-analytics}
      POSTGRES_PASSWORD: ${TIMESCALE_PASSWORD:-secret}
    volumes:
      - timescale-data:/var/lib/postgresql/data
      - ./infra/timescaledb/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${TIMESCALE_USER:-analytics}"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.2-alpine
    ports:
      - "6379:6379"
    command: >
      redis-server
      --requirepass ${REDIS_PASSWORD:-secret}
      --maxmemory 512mb
      --maxmemory-policy allkeys-lru
      --appendonly yes
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD:-secret}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5

  zookeeper:
    image: confluentinc/cp-zookeeper:7.6.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    healthcheck:
      test: ["CMD-SHELL", "echo ruok | nc localhost 2181"]
      interval: 10s
      timeout: 5s
      retries: 5

  kafka:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      zookeeper:
        condition: service_healthy
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_INTERNAL://kafka:29092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT_INTERNAL
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: "false"
    healthcheck:
      test: ["CMD-SHELL", "kafka-topics --bootstrap-server localhost:9092 --list"]
      interval: 15s
      timeout: 10s
      retries: 10

  kafka-setup:
    image: confluentinc/cp-kafka:7.6.0
    depends_on:
      kafka:
        condition: service_healthy
    entrypoint: ["/bin/bash", "-c"]
    command: |
      "
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic pay-events --partitions 6 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic commerce-events --partitions 6 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic hr-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic logistics-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic accounting-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic identity-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic compliance-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic billing-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic iam-events --partitions 3 --replication-factor 1
      kafka-topics --bootstrap-server kafka:29092 --create --if-not-exists --topic support-events --partitions 3 --replication-factor 1
      echo 'Topics created successfully'
      "

  elasticsearch:
    image: elasticsearch:8.13.0
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - ES_JAVA_OPTS=-Xms512m -Xmx512m
    ulimits:
      memlock:
        soft: -1
        hard: -1
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:9200/_cluster/health || exit 1"]
      interval: 15s
      timeout: 10s
      retries: 10

  atlashub:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
      timescaledb:
        condition: service_healthy
      redis:
        condition: service_healthy
      kafka:
        condition: service_healthy
      elasticsearch:
        condition: service_healthy
    env_file:
      - .env
    environment:
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/atlashub
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER:-atlashub}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD:-secret}
      ANALYTICS_DATASOURCE_URL: jdbc:postgresql://timescaledb:5432/atlashub_analytics
      ANALYTICS_DATASOURCE_USERNAME: ${TIMESCALE_USER:-analytics}
      ANALYTICS_DATASOURCE_PASSWORD: ${TIMESCALE_PASSWORD:-secret}
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PASSWORD: ${REDIS_PASSWORD:-secret}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
      ELASTICSEARCH_HOST: elasticsearch
      ELASTICSEARCH_PORT: 9200

volumes:
  pg-data:
  timescale-data:
  redis-data:
  es-data:
```

---

## `.env` File (Local Development)

```env
# Postgres
POSTGRES_USER=atlashub
POSTGRES_PASSWORD=localdev_secret

# TimescaleDB
TIMESCALE_USER=analytics
TIMESCALE_PASSWORD=localdev_secret

# Redis
REDIS_PASSWORD=localdev_secret

# JWT
JWT_SECRET=a_very_long_local_dev_jwt_secret_at_least_256_bits_long_replace_in_production
JWT_ACCESS_EXPIRY_MINUTES=15
JWT_REFRESH_EXPIRY_DAYS=30

# Paystack (use test keys)
PAYSTACK_SECRET_KEY=sk_test_...
PAYSTACK_WEBHOOK_SECRET=sk_test_...

# Anchor (use sandbox)
ANCHOR_API_KEY=anc_sandbox_...
ANCHOR_WEBHOOK_SECRET=...

# Moniepoint (use sandbox)
MONIEPOINT_API_KEY=...
MONIEPOINT_MERCHANT_CODE=...

# Feature flags
PAYSTACK_TEST_MODE=true
```

---

## Startup Order

Docker Compose `depends_on` with `condition: service_healthy` guarantees this startup sequence:

```
Zookeeper → Kafka → kafka-setup (one-shot, exits) → atlashub starts
Postgres ─────────────────────────────────────────→ atlashub starts
TimescaleDB ──────────────────────────────────────→ atlashub starts
Redis ────────────────────────────────────────────→ atlashub starts
Elasticsearch ────────────────────────────────────→ atlashub starts
```

The application will not start until all infrastructure services pass their health checks.

---

## Dockerfile (Multi-Stage Build)

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime (minimal image)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S atlashub && adduser -S atlashub -G atlashub
USER atlashub
COPY --from=builder /app/atlashub-bootstrap/target/atlashub.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Useful Commands

```bash
# Start all services
docker compose up -d

# Start only infrastructure (no app — for running app from IDE)
docker compose up -d postgres timescaledb redis kafka kafka-setup elasticsearch

# View logs
docker compose logs -f atlashub
docker compose logs -f kafka

# Stop and remove volumes (full reset)
docker compose down -v

# Rebuild app image after code changes
docker compose build atlashub && docker compose up -d atlashub
```
