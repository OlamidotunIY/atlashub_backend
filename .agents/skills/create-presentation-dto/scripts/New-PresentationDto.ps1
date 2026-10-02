[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = "",
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$Name,
    [Parameter(Mandatory = $true)][ValidateSet('Request', 'Response')][string]$Kind,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Fields
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact dto) | ConvertFrom-Json
$recordName = if ($Name.EndsWith($Kind)) { $Name } else { "$Name$Kind" }
$targetDir = Join-Path $context.sourceRoot 'presentation\dto'
$targetFile = Join-Path $targetDir "$recordName.java"
if (Test-Path -LiteralPath $targetFile) { throw "Refusing to overwrite existing DTO: $targetFile" }

$members = @($Fields.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
if ($members.Count -eq 0) { throw 'At least one field is required.' }
foreach ($member in $members) {
    if ($member -notmatch '^[A-Za-z0-9_$.<>?, ]+\s+[a-z][A-Za-z0-9]*$') {
        throw "Invalid Java record field declaration: '$member'"
    }
}

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$body = ($members | ForEach-Object { "        $_" }) -join ",`r`n"
$content = @"
package $($context.javaPackage).presentation.dto;

public record $recordName(
$body
) {
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Review request fields for Jakarta validation before compiling.'
