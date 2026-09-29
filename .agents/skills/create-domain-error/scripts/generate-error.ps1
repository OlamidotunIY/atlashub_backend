param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$ErrorName,
    [Parameter(Mandatory=$true)][string]$BaseException
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery
$SearchPattern = "src\main\java\com\atlashub\$Module"
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\main\java\com\atlashub\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$TargetDir = Join-Path -Path $ModulePath -ChildPath "domain\exceptions"

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$ErrorName.java"

if (Test-Path -Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

$PackageModule = $Module -replace '[/\\]', '.'
$Content = @"
package com.atlashub.$PackageModule.domain.exceptions;

import com.atlashub.shared.domain.exception.$BaseException;

public class $ErrorName extends $BaseException {

    public $ErrorName() {
        super("A domain error occurred");
    }

    public $ErrorName(String message) {
        super(message);
    }

    public $ErrorName(String message, Throwable cause) {
        super(message, cause);
    }
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Domain Error created at $TargetFile"
