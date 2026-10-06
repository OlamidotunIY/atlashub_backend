[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*(Event|Created|Updated|Approved|Rejected|Closed|Opened|Issued|Revoked)$')][string]$EventName,
    [string]$PayloadFields = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact event) | ConvertFrom-Json
$targetDir = Join-Path $context.sourceRoot 'domain\events'
$targetFile = Join-Path $targetDir "$EventName.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$fields = @($PayloadFields.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
$payload = ($fields | ForEach-Object { "            $_" }) -join ",`r`n"

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).domain.events;

import com.atlashub.shared.domain.event.DomainEvent;
import java.time.ZonedDateTime;

public record $EventName(
        String eventId,
        Long aggregateId,
        ZonedDateTime occurredAt,
        String correlationId,
        Payload payload
) implements DomainEvent<$EventName.Payload> {

    public record Payload(
$payload
    ) {
    }
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
