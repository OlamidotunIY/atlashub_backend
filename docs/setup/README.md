# Setup — Index

This directory contains setup, configuration, and integration guides for every infrastructure component and third-party integration in AtlasHub.

---

## Infrastructure

| Document | What It Covers |
|---|---|
| [docker-setup.md](./docker-setup.md) | Complete local dev environment with Docker Compose — all services, health checks, `.env` template |
| [kafka-setup.md](./kafka-setup.md) | Kafka topics, partitioning, producer/consumer config, retry/DLQ, schema evolution rules |
| [redis-setup.md](./redis-setup.md) | Key namespaces, session management, token revocation, rate limiting, API key cache |
| [elasticsearch-setup.md](./elasticsearch-setup.md) | Index schemas, edge_ngram analyzers, CDC synchronization via domain events, full-text search queries |
| [timescaledb-analytics.md](./timescaledb-analytics.md) | Hypertable schema, continuous aggregates, data retention/compression, JDBC writes |
| [websocket-setup.md](./websocket-setup.md) | STOMP channels, JWT auth on CONNECT, selective event broadcaster, production scaling with RabbitMQ relay |

## Patterns

| Document | What It Covers |
|---|---|
| [outbox-pattern.md](./outbox-pattern.md) | Transactional Outbox, BaseJpaRepositoryAdapter, OutboxWriter, OutboxRelay, Inbox idempotency |
| [api-key-hmac-auth.md](./api-key-hmac-auth.md) | API key issuance, HMAC-SHA256 signing algorithm, server-side verification, key rotation |
| [webhook-infrastructure.md](./webhook-infrastructure.md) | Outbound webhook subscriptions, retry/backoff, HMAC signature, idempotency for merchants |

## Provider Integrations

| Document | Provider | Handles |
|---|---|---|
| [anchor-integration.md](./anchor-integration.md) | Anchor | NUBAN issuance, inbound transfers, settlements |
| [paystack-integration.md](./paystack-integration.md) | Paystack | Card/USSD charges, BVN verification, bank name enquiry |
| [moniepoint-integration.md](./moniepoint-integration.md) | Moniepoint | POS terminals, card-present, QR payments |
