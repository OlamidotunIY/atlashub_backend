[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [string]$Methods = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact repository) | ConvertFrom-Json
$entityFile = Join-Path $context.sourceRoot "domain\entities\$EntityName.java"
if (-not (Test-Path $entityFile)) { throw "Entity not found: $entityFile" }
$targetDir = Join-Path $context.sourceRoot 'domain\repositories'
$targetFile = Join-Path $targetDir "$EntityName`Repository.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$declarations = @($Methods.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
$body = if ($declarations.Count) { "`r`n" + (($declarations | ForEach-Object { "    $_;" }) -join "`r`n`r`n") + "`r`n" } else { '' }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).domain.repositories;

import $($context.javaPackage).domain.entities.$EntityName;
import com.atlashub.shared.domain.repository.Repository;

public interface $EntityName`Repository extends Repository<$EntityName> {$body}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
