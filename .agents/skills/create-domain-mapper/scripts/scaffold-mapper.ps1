[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*(Jpa|JPA)$')][string]$JpaClassName
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact adapter) | ConvertFrom-Json
$domainFile = Join-Path $context.sourceRoot "domain\entities\$EntityName.java"
$jpaFile = Join-Path $context.sourceRoot "infrastructure\persistence\entities\$JpaClassName.java"
if (-not (Test-Path $domainFile)) { throw "Domain entity not found: $domainFile" }
if (-not (Test-Path $jpaFile)) { throw "JPA entity not found: $jpaFile" }
$targetDir = Join-Path $context.sourceRoot 'infrastructure\persistence\mappers'
$targetFile = Join-Path $targetDir "$EntityName`Mapper.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).infrastructure.persistence.mappers;

import $($context.javaPackage).domain.entities.$EntityName;
import $($context.javaPackage).infrastructure.persistence.entities.$JpaClassName;
import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface $EntityName`Mapper extends DomainMapper<$EntityName, $JpaClassName> {
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
