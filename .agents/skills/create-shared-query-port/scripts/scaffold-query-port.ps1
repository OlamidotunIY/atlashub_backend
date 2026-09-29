param (
    [Parameter(Mandatory=$true)][string]$EntityName
)

$ErrorActionPreference = "Stop"

$PortPath = "atlashub-shared\src\main\java\com\atlashub\shared\domain\ports\query"
$FilePath = "$PortPath\${EntityName}QueryPort.java"

if (-Not (Test-Path $PortPath)) {
    New-Item -ItemType Directory -Force -Path $PortPath | Out-Null
}

if (Test-Path $FilePath) {
    Write-Host "WARNING: Query Port already exists at $FilePath. Skipping scaffolding to prevent overwrite."
    exit 0
}

$Content = @"
package com.atlashub.shared.domain.ports.query;

import java.util.Optional;

public interface ${EntityName}QueryPort {
    
    // TODO: Define cross-module nested DTOs (e.g., record ${EntityName}Dto(...))
}
"@

Set-Content -Path $FilePath -Value $Content
Write-Host "SUCCESS: Query Port created at $FilePath"
