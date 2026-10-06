[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$EntityName,
    [Parameter(Mandatory = $true)][bool]$IsAggregateRoot,
    [string]$Fields = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact entity) | ConvertFrom-Json
$targetDir = Join-Path $context.sourceRoot 'domain\entities'
$targetFile = Join-Path $targetDir "$EntityName.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }

$members = @($Fields.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
foreach ($member in $members) {
    if ($member -notmatch '^([A-Za-z0-9_$.<>?, ]+)\s+([a-z][A-Za-z0-9]*)$') { throw "Invalid field declaration: '$member'" }
    if ($Matches[2] -eq 'id') { throw 'Do not include id in -Fields; it is generated automatically.' }
}

$extends = if ($IsAggregateRoot) { ' extends AggregateRoot<Long>' } else { '' }
$aggregateImport = if ($IsAggregateRoot) { "import com.atlashub.shared.domain.entities.AggregateRoot;`r`n`r`n" } else { '' }
$fieldLines = @('    private final Long id;') + ($members | ForEach-Object { "    private final $_;" })
$params = @('Long id') + $members
$assignments = @('        this.id = id;') + ($members | ForEach-Object { $name = ($_ -split '\s+')[-1]; "        this.$name = $name;" })
$args = @('id') + ($members | ForEach-Object { ($_ -split '\s+')[-1] })
$getId = if ($IsAggregateRoot) { "`r`n    @Override`r`n    public Long getId() {`r`n        return id;`r`n    }`r`n" } else { '' }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).domain.entities;

$aggregateImport
public class $EntityName$extends {

$($fieldLines -join "`r`n")

    public $EntityName($($params -join ', ')) {
$($assignments -join "`r`n")
    }

    public static $EntityName create($($params -join ', ')) {
        return new $EntityName($($args -join ', '));
    }
$getId}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Add documented invariants, semantic exceptions, events, and state transitions before validation.'
