[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$Name,
    [Parameter(Mandatory = $true)][ValidateSet('Record','Enum')][string]$Type,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Definition,
    [string]$ValidationBody = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact value-object) | ConvertFrom-Json
$targetDir = Join-Path $context.sourceRoot 'domain\valueobject'
$targetFile = Join-Path $targetDir "$Name.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$parts = @($Definition.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
if (-not $parts.Count) { throw 'Definition cannot be empty.' }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
if ($Type -eq 'Enum') {
    $content = "package $($context.javaPackage).domain.valueobject;`r`n`r`npublic enum $Name {`r`n    $($parts -join ",`r`n    ")`r`n}`r`n"
} else {
    $fields = ($parts | ForEach-Object { "        $_" }) -join ",`r`n"
    $validation = $ValidationBody.Trim()
    $content = "package $($context.javaPackage).domain.valueobject;`r`n`r`npublic record $Name(`r`n$fields`r`n) {`r`n    public $Name {`r`n$validation`r`n    }`r`n}`r`n"
}
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
