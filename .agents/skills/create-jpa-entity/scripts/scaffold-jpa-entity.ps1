param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName,
    [Parameter(Mandatory=$false)][string]$SubModule = "",
    [Parameter(Mandatory=$false)][string]$TableName = ""
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
$TargetDir  = Join-Path -Path $ModulePath -ChildPath "infrastructure\persistence\entities"

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

# Use JpaEntity suffix per project convention
$JpaEntityName = "${EntityName}JpaEntity"
$TargetFile = Join-Path -Path $TargetDir -ChildPath "${JpaEntityName}.java"

if (Test-Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

# Compute snake_case table name from EntityName if not explicitly provided
if ($TableName -eq "") {
    # Convert PascalCase to snake_case: e.g. SalesOrder -> sales_orders
    $snake = ($EntityName -creplace '(?<!^)([A-Z])', '_$1').ToLower()
    $TableName = "${snake}s"
}

$Content = @"
package $JavaPackage.infrastructure.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "$TableName",
    indexes = {
        // TODO: Add @Index entries for frequently queried columns, e.g.:
        // @Index(name = "Idx_${EntityName.ToLower()}_org_id", columnList = "organization_id")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class $JpaEntityName {

    @Id
    private Long id;

    // TODO: Add @Column fields here.
    // Rules:
    //   - Use @Column(name = "snake_case_name") for every field
    //   - Only use nullable = false when the DB column is genuinely NOT NULL
    //   - NO @GeneratedValue — IDs come from DomainSequenceGenerator
    //   - NO @OneToMany / @ManyToOne — store foreign-key IDs as plain Long fields

    @Version
    private Long version;
}
"@

# PowerShell here-strings don't support method calls on variable interpolation,
# so we patch the table name placeholder in the Index comment manually
$Content = $Content -replace '@Index\(name = "Idx_\$\{EntityName\.ToLower\(\)\}_org_id"', "@Index(name = `"Idx_${EntityName.ToLower()}_org_id`""

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: JPA Entity created at $TargetFile"
Write-Host "  Table name: $TableName"
Write-Host "  Remember: NO @GeneratedValue on @Id, @Version at bottom, NO relationship annotations"
