param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName,
    [Parameter(Mandatory=$true)][bool]$IsAggregateRoot,
    [Parameter(Mandatory=$false)][string]$SubModule = ""
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery — works regardless of project layout
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\\main\\java\\com\\atlashub\\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$TargetDir = Join-Path -Path $ModulePath -ChildPath "domain\entities"

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$EntityName.java"
if (Test-Path -Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting."
    exit 1
}

# Build package name — include sub-module segment when provided
if ($SubModule -ne "") {
    $PackageName = "com.atlashub.$Module.$SubModule.domain.entities"
} else {
    $PackageName = "com.atlashub.$Module.domain.entities"
}

$BaseClass      = ""
$ImportAggregate = ""
$IdOverride     = ""

if ($IsAggregateRoot) {
    $BaseClass       = " extends AggregateRoot<Long>"
    $ImportAggregate = "import com.atlashub.shared.domain.entities.AggregateRoot;`n"
    $IdOverride      = "`n    @Override`n    public Long getId() {`n        return id;`n    }`n"
}

$Content = @"
package $PackageName;

${ImportAggregate}import lombok.Getter;
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
