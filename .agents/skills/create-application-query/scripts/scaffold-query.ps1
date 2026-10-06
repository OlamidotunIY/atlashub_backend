[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$QueryName,
    [string]$QueryFields = '',
    [Parameter(Mandatory = $true)][string]$ResultType,
    [string]$ResultFields = '',
    [string]$Imports = '',
    [string]$Dependencies = '',
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$ExecuteBody,
    [string]$PreAuthorize = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact query) | ConvertFrom-Json
$applicationDir = Join-Path $context.sourceRoot 'application'
$folder = if (Test-Path (Join-Path $applicationDir 'query')) { 'query' } else { 'queries' }
$targetDir = Join-Path $applicationDir "$folder\$QueryName"
$package = "$($context.javaPackage).application.$folder.$QueryName"
$queryFile = Join-Path $targetDir "$QueryName`Query.java"
$handlerFile = Join-Path $targetDir "$QueryName`Handler.java"
if ((Test-Path $queryFile) -or (Test-Path $handlerFile)) { throw "Refusing to overwrite query files in $targetDir" }

function Parse-Declarations([string]$Value) { @($Value.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ }) }
$queryBody = (Parse-Declarations $QueryFields | ForEach-Object { "        $_" }) -join ",`r`n"
$importLines = (Parse-Declarations $Imports | ForEach-Object { "import $_;" }) -join "`r`n"
$deps = Parse-Declarations $Dependencies
$fields = ($deps | ForEach-Object { "    private final $_;" }) -join "`r`n"
$params = $deps -join ', '
$assignments = ($deps | ForEach-Object { $name = ($_ -split '\s+')[-1]; "        this.$name = $name;" }) -join "`r`n"
$securityImport = if ($PreAuthorize) { "import org.springframework.security.access.prepost.PreAuthorize;`r`n" } else { '' }
$securityAnnotation = if ($PreAuthorize) { "    @PreAuthorize(`"$PreAuthorize`")`r`n" } else { '' }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
Set-Content -LiteralPath $queryFile -Value "package $package;`r`n`r`npublic record $QueryName`Query(`r`n$queryBody`r`n) {`r`n}`r`n" -Encoding utf8NoBOM
if ($ResultFields) {
    $resultFile = Join-Path $targetDir "$ResultType.java"
    $resultBody = (Parse-Declarations $ResultFields | ForEach-Object { "        $_" }) -join ",`r`n"
    Set-Content -LiteralPath $resultFile -Value "package $package;`r`n`r`npublic record $ResultType(`r`n$resultBody`r`n) {`r`n}`r`n" -Encoding utf8NoBOM
}
$handlerContent = @"
package $package;

import com.atlashub.shared.application.usecase.Query;
$securityImport$importLines
import org.springframework.stereotype.Component;

@Component
public class $QueryName`Handler extends Query<$QueryName`Query, $ResultType> {

$fields

    public $QueryName`Handler($params) {
$assignments
    }

$securityAnnotation    @Override
    public $ResultType execute($QueryName`Query query) {
$ExecuteBody
    }
}
"@
Set-Content -LiteralPath $handlerFile -Value $handlerContent -Encoding utf8NoBOM
Write-Host "CREATED: $targetDir"
