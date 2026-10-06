[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string]$Module,
    [string]$SubModule = '',
    [Parameter(Mandatory = $true)][ValidatePattern('^[A-Z][A-Za-z0-9]*$')][string]$ControllerName,
    [Parameter(Mandatory = $true)][string]$BasePath,
    [Parameter(Mandatory = $true)][string]$TagName,
    [string]$ConstructorParameters = ''
)

$ErrorActionPreference = 'Stop'
$contextScript = Join-Path $PSScriptRoot '..\..\atlashub-module-workflow\scripts\Get-AtlashubContext.ps1'
$context = (& $contextScript -Module $Module -SubModule $SubModule -Artifact controller) | ConvertFrom-Json
$targetDir = Join-Path $context.sourceRoot 'presentation\rest'
$targetFile = Join-Path $targetDir "$ControllerName`Controller.java"
if (Test-Path $targetFile) { throw "Refusing to overwrite: $targetFile" }
$parameters = $ConstructorParameters.Trim()

New-Item -ItemType Directory -Force -Path $targetDir | Out-Null
$constructor = if ($parameters) {
@"

    public $ControllerName`Controller($parameters) {
    }
"@
} else { '' }
$content = @"
package $($context.javaPackage).presentation.rest;

import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("$BasePath")
@Tag(name = "$TagName")
public class $ControllerName`Controller {$constructor

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
"@
Set-Content -LiteralPath $targetFile -Value $content -Encoding utf8NoBOM
Write-Host "CREATED: $targetFile"
Write-Host 'Add documented endpoints, handler fields/assignments, DTOs, principal context, and MVC tests.'
