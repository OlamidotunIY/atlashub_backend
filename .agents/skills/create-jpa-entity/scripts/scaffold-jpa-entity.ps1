param (
    [Parameter(Mandatory=$true)] [string]$Module,
    [Parameter(Mandatory=$true)] [string]$EntityName
)
$baseDir = "atlashub-platform/$Module/src/main/java/com/atlashub/$Module/infrastructure/persistence/entities"
if (-not (Test-Path $baseDir)) { New-Item -ItemType Directory -Force -Path $baseDir | Out-Null }
$file = "$baseDir/$($EntityName)Jpa.java"
if (-not (Test-Path $file)) {
    Set-Content -Path $file -Value "package com.atlashub.$Module.infrastructure.persistence.entities;

// TODO: Implement JPA Entity
"
}
Write-Output "Scaffolded $file"
