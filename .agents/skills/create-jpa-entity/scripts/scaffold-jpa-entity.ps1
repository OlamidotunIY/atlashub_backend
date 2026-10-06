[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*(Jpa|JPA)$')][string]$JpaClassName,
    [Parameter(Mandatory = $true)][ValidatePattern('^[a-z][a-z0-9_]*$')][string]$TableName,
    [string]$Fields = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact adapter) | ConvertFrom-Json
$targetDir = Join-Path $context.sourceRoot 'infrastructure\persistence\entities'
$targetFile = Join-Path $targetDir "$JpaClassName.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }

$specs = @($Fields.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
$fieldLines = [Collections.Generic.List[string]]::new()
foreach ($spec in $specs) {
    $parts = $spec.Split(':')
    if ($parts.Count -lt 2 -or $parts.Count -gt 4) { throw "Use Type:name[:nullable[:unique]], got '$spec'" }
    $type = $parts[0]; $name = $parts[1]
    if ($type -notmatch '^[A-Za-z0-9_$.<>?, ]+$' -or $name -notmatch '^[a-z][A-Za-z0-9]*$') { throw "Invalid field spec '$spec'" }
    $nullable = if ($parts.Count -ge 3) { [bool]::Parse($parts[2]) } else { $true }
    $unique = if ($parts.Count -ge 4) { [bool]::Parse($parts[3]) } else { $false }
    $column = ($name -creplace '(?<!^)([A-Z])', '_$1').ToLowerInvariant()
    $attrs = @("name = `"$column`"")
    if (-not $nullable) { $attrs += 'nullable = false' }
    if ($unique) { $attrs += 'unique = true' }
    $fieldLines.Add("    @Column($($attrs -join ', '))`r`n    private $type $name;")
}

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).infrastructure.persistence.entities;

import com.atlashub.shared.infrastructure.persistence.entities.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "$TableName")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class $JpaClassName implements BaseJpaEntity {

    @Id
    private Long id;

$($fieldLines -join "`r`n`r`n")

    @Version
    private Long version;
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Add documented indexes and JSON/enum annotations before compiling.'
