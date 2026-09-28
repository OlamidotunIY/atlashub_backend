param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\\main\\java\\com\\atlashub\\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$TargetDir  = Join-Path -Path $ModulePath -ChildPath "infrastructure\persistence\entities"

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "${EntityName}Jpa.java"

if (Test-Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

$Content = @"
package com.atlashub.$Module.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "${EntityName.ToLower()}")
public class ${EntityName}Jpa {

    @Id
    @Column(nullable = false, updatable = false)
    private Long id;

    // TODO: Agent must inject mapped fields here

    @Version
    private Long version;
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: JPA Entity created at $TargetFile"
