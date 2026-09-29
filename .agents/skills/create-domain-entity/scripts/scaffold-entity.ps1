param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$EntityName,
    [Parameter(Mandatory=$true)][bool]$IsAggregateRoot,
    [Parameter(Mandatory=$false)][string]$SubModule = ""
)

$ErrorActionPreference = "Stop"

# Build the Java sub-path.
# With SubModule: src\main\java\com\atlashub\<Module>\<SubModule>
# Without:        src\main\java\com\atlashub\<Module>
if ($SubModule -ne "") {
    $JavaSubPath = "src\main\java\com\atlashub\$Module\$SubModule"
    $PackageName = "com.atlashub.$Module.$SubModule.domain.entities"
} else {
    $JavaSubPath = "src\main\java\com\atlashub\$Module"
    $PackageName = "com.atlashub.$Module.domain.entities"
}

$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape($JavaSubPath) } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Path '$JavaSubPath' not found in this workspace."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$TargetDir  = Join-Path -Path $ModulePath -ChildPath "domain\entities"

if (-not (Test-Path -Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "$EntityName.java"
if (Test-Path -Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting."
    exit 1
}

$BaseClass       = ""
$ImportAggregate = ""
$IdOverride      = ""
$ExtendsClause   = ""
$StaticFactory   = "    // TODO: Add public static create(...) factory method enforcing invariants"

if ($IsAggregateRoot) {
    $BaseClass       = " extends AggregateRoot<Long>"
    $ImportAggregate = "import com.atlashub.shared.domain.entities.AggregateRoot;`n"
    $IdOverride      = @"

    @Override
    public Long getId() {
        return id;
    }
"@
    $StaticFactory = @"
    // All-args constructor (used by MapStruct — keep package-private or public)
    public ${EntityName}(Long id /*, TODO: other fields */) {
        this.id = id;
        // TODO: assign other fields
    }

    // Static factory — enforces creation invariants
    public static ${EntityName} create(/* TODO: creation params */) {
        // TODO: validate invariants, then:
        return new ${EntityName}(null /*, TODO: other fields */);
    }
"@
}

$Content = @"
package $PackageName;

${ImportAggregate}import lombok.Getter;
import java.time.ZonedDateTime;

@Getter
public class $EntityName$BaseClass {

    private final Long id;
    // TODO: Add domain fields here (all final)

$StaticFactory

    // TODO: Add business methods here (state transitions, invariant enforcement, registerEvent(...))
$IdOverride
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Entity skeleton created at $TargetFile"
