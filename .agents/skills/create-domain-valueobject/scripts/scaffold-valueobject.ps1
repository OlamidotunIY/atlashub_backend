param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$Name,
    [Parameter(Mandatory=$true)][ValidateSet("Record","Enum")][string]$Type
)

$SearchPattern = "src\main\java\com\atlashub\$Module"
$ModulePath = Get-ChildItem -Path . -Recurse -Directory -Filter $Module -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match [regex]::Escape($SearchPattern) } | Select-Object -First 1

if (-not $ModulePath) {
    $TargetDir = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module\domain\valueobject"
} else {
    $TargetDir = Join-Path -Path $ModulePath.FullName -ChildPath "domain\valueobject"
}

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$Name.java"
if (Test-Path -Path $TargetFile) {
    Write-Host "Error: $TargetFile already exists! Aborting."
    exit 1
}

if ($Type -eq "Enum") {
    $Content = @"
package com.atlashub.$Module.domain.valueobject;

public enum $Name {
    // TODO: Agent must inject enum constants here
}
"@
} else {
    $Content = @"
package com.atlashub.$Module.domain.valueobject;

public record $Name(
    // TODO: Agent must inject record fields here
) {
    public $Name {
        // TODO: Agent MUST implement invariant validation checks here
        // If validation fails, throw a Domain Error
    }
}
"@
}

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Value Object Skeleton created at $TargetFile"
