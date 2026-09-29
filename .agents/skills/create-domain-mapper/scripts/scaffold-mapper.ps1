param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName,
    [Parameter(Mandatory=$false)][string]$SubModule = ""
)

$ErrorActionPreference = "Stop"

if ($SubModule -ne "") {
    $JavaSubPath = "src\main\java\com\atlashub\$Module\$SubModule"
    $JavaPackage = "com.atlashub.$Module.$SubModule"
} else {
    $JavaSubPath = "src\main\java\com\atlashub\$Module"
    $JavaPackage = "com.atlashub.$Module"
}

$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape($JavaSubPath) } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Path '$JavaSubPath' not found in this workspace."
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

# JPA entity class name uses "JpaEntity" suffix per project convention
$JpaEntityName = "${EntityName}JpaEntity"

$Content = @"
package $JavaPackage.infrastructure.persistence.mappers;

import com.atlashub.shared.infrastructure.persistence.mappers.DomainMapper;
import com.atlashub.shared.infrastructure.persistence.mappers.ValueObjectMapper;
import $JavaPackage.domain.entities.$EntityName;
import $JavaPackage.infrastructure.persistence.entities.$JpaEntityName;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.ERROR,
    uses = {ValueObjectMapper.class}
)
public interface ${EntityName}Mapper extends DomainMapper<$EntityName, $JpaEntityName> {
    // DomainMapper<TDomain, TJpa> already declares:
    //   TJpa   toJpa(TDomain domain);
    //   TDomain toDomain(TJpa jpa);
    //
    // If MapStruct cannot auto-map any fields, add @Mapping annotations here.
    // Example:
    //   @Mapping(target = "someField", source = "anotherField")
    //   $JpaEntityName toJpa($EntityName domain);
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Mapper created at $TargetFile"
