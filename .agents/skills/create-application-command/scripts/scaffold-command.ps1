[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$CommandName,
    [string]$CommandFields = '',
    [Parameter(Mandatory = $true)][string]$ResultType,
    [string]$ResultFields = '',
    [string]$Imports = '',
    [string]$Dependencies = '',
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$ExecuteBody,
    [string]$PreAuthorize = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact command) | ConvertFrom-Json
$applicationDir = Join-Path $context.sourceRoot 'application'
$commandFolder = if (Test-Path (Join-Path $applicationDir 'command')) { 'command' } else { 'commands' }
$targetDir = Join-Path $applicationDir "$commandFolder\$CommandName"
$package = "$($context.javaPackage).application.$commandFolder.$CommandName"
$commandFile = Join-Path $targetDir "$CommandName`Command.java"
$handlerFile = Join-Path $targetDir "$CommandName`Handler.java"
if ((Test-Path $commandFile) -or (Test-Path $handlerFile)) { throw "Refusing to overwrite command files in $targetDir" }

function Parse-Declarations([string]$Value) {
    @($Value.Split(';') | ForEach-Object { $_.Trim() } | Where-Object { $_ })
}
$commandMembers = Parse-Declarations $CommandFields
$recordBody = ($commandMembers | ForEach-Object { "        $_" }) -join ",`r`n"
$importLines = (Parse-Declarations $Imports | ForEach-Object { "import $_;" }) -join "`r`n"
$dependencyMembers = Parse-Declarations $Dependencies
$dependencyFields = ($dependencyMembers | ForEach-Object { "    private final $_;" }) -join "`r`n"
$constructorParams = $dependencyMembers -join ', '
$assignments = ($dependencyMembers | ForEach-Object { $name = ($_ -split '\s+')[-1]; "        this.$name = $name;" }) -join "`r`n"
$securityImport = if ($PreAuthorize) { "import org.springframework.security.access.prepost.PreAuthorize;`r`n" } else { '' }
$securityAnnotation = if ($PreAuthorize) { "    @PreAuthorize(`"$PreAuthorize`")`r`n" } else { '' }

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$commandContent = "package $package;`r`n`r`npublic record $CommandName`Command(`r`n$recordBody`r`n) {`r`n}`r`n"
Set-Content -LiteralPath $commandFile -Value $commandContent -Encoding utf8NoBOM

$actualResultType = $ResultType
if ($ResultType -ne 'Void' -and $ResultFields) {
    $resultFile = Join-Path $targetDir "$ResultType.java"
    if (Test-Path $resultFile) { throw "Refusing to overwrite result: $resultFile" }
    $resultMembers = Parse-Declarations $ResultFields
    $resultBody = ($resultMembers | ForEach-Object { "        $_" }) -join ",`r`n"
    Set-Content -LiteralPath $resultFile -Value "package $package;`r`n`r`npublic record $ResultType(`r`n$resultBody`r`n) {`r`n}`r`n" -Encoding utf8NoBOM
}

$handlerContent = @"
package $package;

import com.atlashub.shared.application.usecase.Command;
$securityImport$importLines
import org.springframework.stereotype.Component;

@Component
public class $CommandName`Handler extends Command<$CommandName`Command, $actualResultType> {

$dependencyFields

    public $CommandName`Handler($constructorParams) {
$assignments
    }

$securityAnnotation    @Override
    public $actualResultType execute($CommandName`Command command) {
$ExecuteBody
    }
}
"@
Set-Content -LiteralPath $handlerFile -Value $handlerContent -Encoding utf8NoBOM
Write-Host "CREATED: $targetDir"
