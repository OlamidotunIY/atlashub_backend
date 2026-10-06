[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*Exception$')][string]$ErrorName,
    [Parameter(Mandatory = $true)][ValidateSet('BusinessRuleException','ConflictException','NotFoundException','ValidationException','AuthorizationException')][string]$BaseException,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$DefaultMessage
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact error) | ConvertFrom-Json
$singular = Join-Path $context.sourceRoot 'domain\exception'
$plural = Join-Path $context.sourceRoot 'domain\exceptions'
if ((Test-Path $singular) -and (Test-Path $plural)) { throw 'Both domain/exception and domain/exceptions exist; resolve the package inconsistency first.' }
$targetDir = if (Test-Path $singular) { $singular } else { $plural }
$packagePart = if ($targetDir -eq $singular) { 'exception' } else { 'exceptions' }
$targetFile = Join-Path $targetDir "$ErrorName.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$escapedMessage = $DefaultMessage.Replace('\', '\\').Replace('"', '\"')

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$content = @"
package $($context.javaPackage).domain.$packagePart;

import com.atlashub.shared.domain.exception.$BaseException;

public class $ErrorName extends $BaseException {

    public $ErrorName() {
        super("$escapedMessage");
    }

    public $ErrorName(String message) {
        super(message);
    }

    public $ErrorName(String message, Throwable cause) {
        super(message, cause);
    }
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
