param (
    [Parameter(Mandatory=$true)] [string]$Module,
    [Parameter(Mandatory=$true)] [string]$EntityName
)
$baseDir = "atlashub-platform/$Module/src/main/java/com/atlashub/$Module/infrastructure/persistence/repositories"
if (-not (Test-Path $baseDir)) { New-Item -ItemType Directory -Force -Path $baseDir | Out-Null }
$file = "$baseDir/SpringData$($EntityName)Repository.java"
if (-not (Test-Path $file)) {
    Set-Content -Path $file -Value "package com.atlashub.$Module.infrastructure.persistence.repositories;

// TODO: Implement Spring Data Repository
"
}
Write-Output "Scaffolded $file"
