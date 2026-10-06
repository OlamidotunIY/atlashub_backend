[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$OwnerModule,
    [string]$OwnerSubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*QueryPort$')][string]$PortName,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Members
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
[void]((& $contextScript -Module $OwnerModule -SubModule $OwnerSubModule -Artifact port) | ConvertFrom-Json)
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..\..\..')).Path
$targetDir = Join-Path $repoRoot 'atlashub-shared\src\main\java\com\atlashub\shared\application\port'
$targetFile = Join-Path $targetDir "$PortName.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite existing port: $targetFile" }

$declarations = @($Members.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
if ($declarations.Count -eq 0) { throw 'At least one method or nested record declaration is required.' }
$body = ($declarations | ForEach-Object { "    $_;" }) -join "`r`n`r`n"
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package com.atlashub.shared.application.port;

public interface $PortName {

$body
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Now implement this port in the data-owning module and verify application bean discovery.'
