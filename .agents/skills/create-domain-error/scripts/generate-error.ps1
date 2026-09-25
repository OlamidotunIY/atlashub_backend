param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$ErrorName,
    [Parameter(Mandatory=$true)][string]$BaseException
)

$SearchPattern = "src\main\java\com\atlashub\$Module"
$ModulePath = Get-ChildItem -Path . -Recurse -Directory -Filter $Module -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match [regex]::Escape($SearchPattern) } | Select-Object -First 1

if (-not $ModulePath) {
    $TargetDir = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module\domain\exception"
} else {
    $TargetDir = Join-Path -Path $ModulePath.FullName -ChildPath "domain\exception"
}

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$ErrorName.java"

if (Test-Path -Path $TargetFile) {
    Write-Host "Error: $TargetFile already exists! Aborting."
    exit 1
}

$Content = @"
package com.atlashub.$Module.domain.exception;

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
