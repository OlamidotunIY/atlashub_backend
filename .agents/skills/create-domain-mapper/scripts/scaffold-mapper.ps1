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
$TargetDir  = Join-Path -Path $ModulePath -ChildPath "infrastructure\persistence\mappers"

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "${EntityName}Mapper.java"

if (Test-Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

$Content = @"
package com.atlashub.$Module.infrastructure.persistence.mappers;

import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import com.atlashub.$Module.domain.entities.$EntityName;
import com.atlashub.$Module.infrastructure.persistence.entities.${EntityName}Jpa;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {ValueObjectMapper.class})
public interface ${EntityName}Mapper {

    // TODO: Agent must inject mapping methods here
    // Example:
    //   ${EntityName}Jpa toJpa($EntityName domain);
    //   $EntityName toDomain(${EntityName}Jpa jpa);
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Mapper created at $TargetFile"
