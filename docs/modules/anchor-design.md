# Anchor Infrastructure Design (`atlashub-infrastructure:anchor`)

## Purpose

This module is the only Anchor transport boundary. It owns sandbox/live credentials, typed HTTP clients, provider
DTOs, webhook subscription and signature verification, and the adapter that implements the provider-neutral
`AnchorBankingPort` and `AnchorComplianceTransportPort` from `atlashub-shared`.

## Package structure

- `application`: verified raw-webhook ingestion command.
- `infrastructure/external/anchor/client`: Anchor HTTP interfaces and environment registry.
- `infrastructure/external/anchor/configuration`: credentials, capabilities, webhooks, timeouts, and Spring wiring.
- `infrastructure/external/anchor/dto`: Anchor request/response models; these never leave the module.
- `infrastructure/external/anchor/adapters`: mapping between Anchor DTOs and shared provider-neutral port records.
- `infrastructure/external/anchor/messaging`: verified event publication.
- `infrastructure/external/anchor/webhook`: signature verification and subscription provisioning.
- `presentation/rest`: raw-body webhook endpoint only.

Business modules depend on shared contracts and compatible event copies, never on Anchor clients, configuration,
DTOs, or implementation packages.

## Environment rules

AtlasHub TEST maps to Anchor SANDBOX only where a caller explicitly requests a supported sandbox capability.
AtlasHub LIVE maps to Anchor LIVE. There is no environment fallback. Current business registration does not create
Anchor sandbox resources; live compliance provisions the operating deposit account.

## Banking behavior

`AnchorBankingPort` supports deposit account provisioning, optional transfer-collection subaccounts and reserved
accounts, authoritative account reads, and deposit-account freeze/unfreeze. Provider resources and status values are
translated into provider-neutral results before leaving the module.

`AnchorComplianceTransportPort` supports document-requirement preview, live business-customer creation, document
upload, verification triggering, and authoritative verification reads. The compliance module maps its domain value
objects into this shared transport contract and never imports Anchor DTOs or clients.

## Webhooks

The endpoint verifies the Anchor signature over the unmodified request body before parsing and publishes a verified
`AnchorWebhookReceivedEvent`. Consumer modules keep their own compatible event contracts and delegate only to command
handlers. Duplicate delivery is safe through consumer delivery tracking and aggregate/provider-reference uniqueness.
