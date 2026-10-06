---
name: create-presentation-dto
description: Create or audit AtlasHub REST request and response DTO records in presentation/dto with validation and no domain leakage.
---

# Create presentation DTO

Load `atlashub-module-workflow`, resolve context with artifact `dto`, and read all returned docs plus the target controller/handler contract.

Use the generator; it refuses ambiguous modules and overwrites:

```powershell
.\.agents\skills\create-presentation-dto\scripts\New-PresentationDto.ps1 `
  -Module "<module>" -SubModule "<optional>" -Name "<Name>" `
  -Kind Request -Fields "String name; String email"
```

Rules:

- DTOs live only in `presentation/dto` and are records.
- Request DTOs contain caller-supplied data only. Never include authenticated `userId` or active `organizationId`; controllers obtain those from `AuthenticatedPrincipal`.
- Add Jakarta validation required by the docs (`@NotBlank`, `@Email`, ranges, sizes). DTO validation is shape validation; business invariants remain in the domain.
- Responses expose stable AtlasHub language, not JPA entities, provider payloads, secrets, hashes, tokens, or internal error details.
- Reuse an existing DTO when its contract is identical. Do not create one DTO per handler by habit.

After generation, add required annotations/imports, read the file back, and run the architecture validator and module compile.
