param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName
)

$ErrorActionPreference = "Stop"

$ModulePath = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module"
$RepoPath = "$ModulePath\domain\repositories"
$FilePath = "$RepoPath\${EntityName}Repository.java"

if (-Not (Test-Path $RepoPath)) {
    New-Item -ItemType Directory -Force -Path $RepoPath | Out-Null
}

if (Test-Path $FilePath) {
    Write-Host "WARNING: Repository already exists at $FilePath. Skipping scaffolding to prevent overwrite."
    exit 0
}

$Content = @"
package com.atlashub.${Module}.domain.repositories;

import com.atlashub.shared.domain.repository.Repository;
import com.atlashub.${Module}.domain.entities.${EntityName};
import java.util.Optional;

public interface ${EntityName}Repository extends Repository<${EntityName}> {
    
}
"@

Set-Content -Path $FilePath -Value $Content
Write-Host "SUCCESS: Repository created at $FilePath"
