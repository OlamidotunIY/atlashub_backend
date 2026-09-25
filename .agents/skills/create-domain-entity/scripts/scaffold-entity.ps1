param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName,
    [Parameter(Mandatory=$true)][bool]$IsAggregateRoot
)

$SearchPattern = "src\main\java\com\atlashub\$Module"
$ModulePath = Get-ChildItem -Path . -Recurse -Directory -Filter $Module -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match [regex]::Escape($SearchPattern) } | Select-Object -First 1

if (-not $ModulePath) {
    $TargetDir = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module\domain\entities"
} else {
    $TargetDir = Join-Path -Path $ModulePath.FullName -ChildPath "domain\entities"
}

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$EntityName.java"
if (Test-Path -Path $TargetFile) {
    Write-Host "Error: $TargetFile already exists! Aborting."
    exit 1
}

$BaseClass = ""
$ImportAggregate = ""
$IdOverride = ""

if ($IsAggregateRoot) {
    $BaseClass = " extends AggregateRoot<Long>"
    $ImportAggregate = "import com.atlashub.shared.domain.entities.AggregateRoot;`n"
    $IdOverride = "`n    @Override`n    public Long getId() {`n        return id;`n    }`n"
}

$Content = @"
package com.atlashub.$Module.domain.entities;

$ImportAggregate
import lombok.Getter;
import java.time.ZonedDateTime;

@Getter
public class $EntityName$BaseClass {

    // TODO: Agent must use replace_file_content to inject final ID fields, custom fields, and constructors
    private final Long id;

    // TODO: Agent must implement public static create(...) method here

    // TODO: Agent must implement business mutator methods and invariants here

    private void touch() {
        // TODO: Agent must inject 'this.updatedAt = ZonedDateTime.now();' if updatedAt exists
    }
$IdOverride
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Entity Skeleton created at $TargetFile"
