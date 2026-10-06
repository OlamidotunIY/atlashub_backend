---
name: create-domain-port
description: Create or audit an AtlasHub module-owned port for external capabilities while keeping provider and infrastructure types outside the domain/application contract.
---

# Create a module-owned port

Load `atlashub-module-workflow`, resolve artifact `port`, and read the module design plus provider setup docs named by it. Search existing `domain/ports` and `application/port` packages before creating anything; preserve the module's existing placement convention.

Use the script with complete Java method declarations:

```powershell
.\.agents\skills\create-domain-port\scripts\New-DomainPort.ps1 `
  -Module pay -SubModule accounts -PortName BankingProviderPort `
  -Methods "ProviderAccount createDepositAccount(CreateDepositAccountRequest request); ProviderAccount createReservedAccount(CreateReservedAccountRequest request)"
```

Rules:

- The port speaks AtlasHub domain language. Provider names, SDK response classes, HTTP clients, JSON annotations, and infrastructure exceptions are forbidden.
- Inputs/outputs are domain value objects or nested immutable records owned by the port when no domain entity is appropriate.
- Split unrelated capabilities into separate ports. Do not create generic `ProviderService` interfaces.
- Create semantic module exceptions for failures surfaced to domain/application code.
- After the port, invoke `create-external-adapter`; a runtime-used port without an implementation is incomplete.
