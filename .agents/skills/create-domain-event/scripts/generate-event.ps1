param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EventName,
    [Parameter(Mandatory=$true)][string]$PayloadFields
)

# 1. Locate the correct module path dynamically
$SearchPattern = "src\main\java\com\atlashub\$Module"
$ModulePath = Get-ChildItem -Path . -Recurse -Directory -Filter $Module -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match [regex]::Escape($SearchPattern) } | Select-Object -First 1

if (-not $ModulePath) {
    Write-Host "Warning: Could not dynamically find module directory. Falling back to standard atlashub-platform structure..."
    $TargetDir = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module\domain\events"
} else {
    $TargetDir = Join-Path -Path $ModulePath.FullName -ChildPath "domain\events"
}

# 2. Ensure the events directory exists
if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$EventName.java"

if (Test-Path -Path $TargetFile) {
    Write-Host "Error: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

# 3. Format the Payload Fields correctly for Java
$FormattedPayload = ""
if ($PayloadFields.Trim() -ne "") {
    $Fields = $PayloadFields -split ","
    $FormattedLines = @()
    foreach ($Field in $Fields) {
        $FormattedLines += "        $($Field.Trim())"
    }
    $FormattedPayload = $FormattedLines -join ",`n"
}

# 4. Generate the perfectly structured Java Record
$Content = @"
package com.atlashub.$Module.domain.events;

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
$FormattedPayload
    ) {
    }
}
"@

# 5. Write file
Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Domain Event created strictly at $TargetFile"
