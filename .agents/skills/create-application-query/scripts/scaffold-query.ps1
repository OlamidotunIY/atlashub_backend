param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$QueryName,
    [Parameter(Mandatory=$true)][string]$ResultType,
    [Parameter(Mandatory=$false)][string]$SubModule = ""
)

$ErrorActionPreference = "Stop"

# Build the Java sub-path
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

# Detect whether this module uses singular "query" or plural "queries"
$AppPath = Join-Path $ModulePath "application"
if (Test-Path (Join-Path $AppPath "query")) {
    $QueriesDir = "query"
} else {
    $QueriesDir = "queries"
}

$QueryDir    = Join-Path -Path $ModulePath -ChildPath "application\$QueriesDir\$QueryName"
$QueryPkg    = "$JavaPackage.application.$QueriesDir.$QueryName"

if (-Not (Test-Path $QueryDir)) {
    New-Item -ItemType Directory -Force -Path $QueryDir | Out-Null
}

$QueryFile   = Join-Path $QueryDir "${QueryName}Query.java"
$HandlerFile = Join-Path $QueryDir "${QueryName}Handler.java"
$ResultFile  = Join-Path $QueryDir "${QueryName}Result.java"

if (Test-Path $HandlerFile) {
    Write-Host "WARNING: Query Handler already exists at $HandlerFile. Skipping to prevent overwrite."
    exit 0
}

# 1. Query Record
$QueryContent = @"
package $QueryPkg;

public record ${QueryName}Query() {
    // TODO: Add query parameters here
}
"@
Set-Content -Path $QueryFile -Value $QueryContent

# 2. Result Record — create if ResultType matches the convention (ends with "Result")
$createResultFile = ($ResultType -eq "${QueryName}Result")

if ($createResultFile) {
    $ResultContent = @"
package $QueryPkg;

public record ${QueryName}Result() {
    // TODO: Add result fields here
}
"@
    Set-Content -Path $ResultFile -Value $ResultContent
}

# 3. Handler Class
$HandlerContent = @"
package $QueryPkg;

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ${QueryName}Handler extends Query<${QueryName}Query, $ResultType> {

    private static final Logger log = LoggerFactory.getLogger(${QueryName}Handler.class);

    public ${QueryName}Handler(
        // TODO: Inject repositories / read ports
    ) {}

    @Override
    public $ResultType execute(${QueryName}Query query) {
        log.info("Executing ${QueryName}Query");

        // TODO: Implement read-only orchestration logic
        throw new UnsupportedOperationException("${QueryName}Handler not implemented");
    }
}
"@
Set-Content -Path $HandlerFile -Value $HandlerContent

$createdFiles = "Query, Handler"
if ($createResultFile) { $createdFiles += ", Result" }
Write-Host "SUCCESS: Scaffolded $createdFiles at $QueryDir"
