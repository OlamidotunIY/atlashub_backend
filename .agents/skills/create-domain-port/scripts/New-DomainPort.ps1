[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*Port$')][string]$PortName,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Methods,
    [ValidateSet('domain', 'application')][string]$Layer = 'domain'
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact port) | ConvertFrom-Json
$relativePackage = if ($Layer -eq 'domain') { 'domain\ports' } else { 'application\port' }
$packageSuffix = $relativePackage.Replace('\', '.')
$targetDir = Join-Path $context.sourceRoot $relativePackage
$targetFile = Join-Path $targetDir "$PortName.java"
if (Test-Path -LiteralPath $targetFile) { throw "Refusing to overwrite existing port: $targetFile" }

$declarations = @($Methods.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
if ($declarations.Count -eq 0) { throw 'At least one method declaration is required.' }
foreach ($declaration in $declarations) {
    if ($declaration -notmatch '\w+\s*\([^)]*\)$') { throw "Invalid method declaration: '$declaration'" }
}

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$methodBlock = ($declarations | ForEach-Object { "    $_;" }) -join "`r`n`r`n"
$content = @"
package $($context.javaPackage).$packageSuffix;

public interface $PortName {

$methodBlock
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
