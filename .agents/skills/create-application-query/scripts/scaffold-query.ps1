param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$QueryName,
    [Parameter(Mandatory=$true)][string]$ResponseType
)

$ErrorActionPreference = "Stop"

$AppPath = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module\application"
if (Test-Path "$AppPath\query") {
    $QueryDir = "$AppPath\query\$QueryName"
} elseif (Test-Path "$AppPath\queries") {
    $QueryDir = "$AppPath\queries\$QueryName"
} else {
    $QueryDir = "$AppPath\queries\$QueryName"
}

if (-Not (Test-Path $QueryDir)) {
    New-Item -ItemType Directory -Force -Path $QueryDir | Out-Null
}

$QueryFile = "$QueryDir\${QueryName}Query.java"
$HandlerFile = "$QueryDir\${QueryName}Handler.java"

if (Test-Path $HandlerFile) {
    Write-Host "WARNING: Query Handler already exists at $HandlerFile. Skipping to prevent overwrite."
    exit 0
}

# 1. Query Record
$QueryContent = @"
package com.atlashub.${Module}.application.queries.${QueryName};

public record ${QueryName}Query() {}
"@
# Replace "queries" with "query" in package if needed
if ($QueryDir -match "application\\query\\") {
    $QueryContent = $QueryContent -replace "application\.queries", "application.query"
}
Set-Content -Path $QueryFile -Value $QueryContent

# 2. Response Record
$ResponseGeneric = $ResponseType
if ($ResponseType -eq "${QueryName}Response") {
    $ResponseFile = "$QueryDir\${QueryName}Response.java"
    $ResponseContent = @"
package com.atlashub.${Module}.application.queries.${QueryName};

public record ${QueryName}Response() {}
"@
    if ($QueryDir -match "application\\query\\") {
        $ResponseContent = $ResponseContent -replace "application\.queries", "application.query"
    }
    Set-Content -Path $ResponseFile -Value $ResponseContent
}

# 3. Handler Class
$HandlerContent = @"
package com.atlashub.${Module}.application.queries.${QueryName};

import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ${QueryName}Handler extends Query<${QueryName}Query, ${ResponseGeneric}> {

    private static final Logger log = LoggerFactory.getLogger(${QueryName}Handler.class);

    public ${QueryName}Handler() {
        // TODO: Inject dependencies (Query Ports, Repositories)
    }

    @Override
    public ${ResponseGeneric} execute(${QueryName}Query query) {
        log.info("Executing ${QueryName}Query");
        
        // TODO: Implement read orchestration logic
        
        return null;
    }
}
"@
if ($QueryDir -match "application\\query\\") {
    $HandlerContent = $HandlerContent -replace "application\.queries", "application.query"
}
Set-Content -Path $HandlerFile -Value $HandlerContent

Write-Host "SUCCESS: Scaffolded Query, Handler, and Response at $QueryDir"
