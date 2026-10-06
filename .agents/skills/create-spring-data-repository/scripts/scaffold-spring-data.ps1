[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*(Jpa|JPA)$')][string]$JpaClassName,
    [string]$Methods = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact repository) | ConvertFrom-Json
$jpaFile = Join-Path $context.sourceRoot "infrastructure\persistence\entities\$JpaClassName.java"
if (-not (Test-Path $jpaFile)) { throw "JPA entity not found: $jpaFile" }
$targetDir = Join-Path $context.sourceRoot 'infrastructure\persistence\repositories'
$targetFile = Join-Path $targetDir "SpringData$EntityName`Repository.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }

$declarations = @($Methods.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
$body = if ($declarations.Count) { "`r`n" + (($declarations | ForEach-Object { "    $_;" }) -join "`r`n`r`n") + "`r`n" } else { '' }
New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).infrastructure.persistence.repositories;

import $($context.javaPackage).infrastructure.persistence.entities.$JpaClassName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringData$EntityName`Repository extends JpaRepository<$JpaClassName, Long> {$body}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Verify every derived-query property against the JPA fields and add repository tests.'
