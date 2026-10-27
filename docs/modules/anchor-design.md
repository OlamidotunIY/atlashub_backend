> **LEGACY IMPLEMENTATION / V2 TARGET NOTICE (2026-10-25):** This document describes the existing code/Anchor-based banking programme. User identity registration is now a separate proposed flow from organization creation. No compulsory Atlas-operated bank account or five-step KYB approval in v2. Provider-specific verification and applicable non-banking legal duties remain. The replacement design is in [Identity & Organization](../architecture/identity-organization-separation.md) and [Banking Retirement](../architecture/provider-banking-decommission.md). The old detail below remains for migration reference.

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

The pay-accounts webhook subscription enables included resources and consumes completed inbound NIP transfers,
successful NIP/book transfers, and NIP reversals. Initiated and failed movements never produce balance-changing
business events.

## Webhooks

The endpoint verifies the Anchor signature over the unmodified request body before parsing and publishes a verified
`AnchorWebhookReceivedEvent`. Consumer modules keep their own compatible event contracts and delegate only to command
handlers. Duplicate delivery is safe through consumer delivery tracking and aggregate/provider-reference uniqueness.


---

## Legacy module decommission

The Anchor integration module is existing/legacy Atlas-operated banking implementation, not required for organization creation or live commerce. Do not presume it stays in target architecture. Retire compulsory KYB, deposit/FBO/reserved account provisioning and bank-balance shadow models after migrating existing users, finances and provider contracts. Preserve only optional provider integration if later technically/contractually suitable.

See [provider-banking-decommission](../architecture/provider-banking-decommission.md).
