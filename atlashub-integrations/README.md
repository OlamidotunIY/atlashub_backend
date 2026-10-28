# Provider integrations

`provider-offerings.csv` is a reviewed list of possible provider operations by merchant country, currency, and environment. A row never grants an organization that capability. Each merchant connection starts in `PENDING_VERIFICATION`; only verified, active connection capabilities may be routed.

Set `ATLAS_INTEGRATIONS_KEY` to a base64 encoded 32 byte AES key before accepting provider credentials. The key is not stored in the database. Back it up securely: losing it makes stored credentials unreadable. TEST and LIVE connections are separate records, and Paystack and Stripe secret key prefixes must match the selected environment.

Connection verification, provider operation adapters, signed webhooks, and reconciliation are still required before a connection can become active or process money. Do not activate a connection by editing the database.
# Provider connections

An organization has one connection per provider and merchant country. A connection can contain an optional TEST
authorization and an optional LIVE authorization. Credentials, health, verified capabilities, webhooks, and provider
account identity belong to the authorization, so data and secrets never cross environments.

Stripe uses a hosted Connect authorization session and callback. Paystack and Anchor use the dedicated credential
endpoint until those providers make an Atlas Hub partner authorization flow available. The backend derives the
environment from those credentials; clients do not submit an environment value.

