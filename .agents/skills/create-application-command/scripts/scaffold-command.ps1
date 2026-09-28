param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$CommandName,
    [Parameter(Mandatory=$true)][string]$ResponseType
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery — resolve the actual src path, not a hardcoded assumption
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\\main\\java\\com\\atlashub\\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath = $FoundModulePath.FullName
$CommandDir = Join-Path -Path $ModulePath -ChildPath "application\commands\$CommandName"

if (-Not (Test-Path $CommandDir)) {
    New-Item -ItemType Directory -Force -Path $CommandDir | Out-Null
}

$CommandFile  = Join-Path $CommandDir "${CommandName}Command.java"
$HandlerFile  = Join-Path $CommandDir "${CommandName}Handler.java"
$ResponseFile = Join-Path $CommandDir "${CommandName}Response.java"

if (Test-Path $HandlerFile) {
    Write-Host "WARNING: Command Handler already exists at $HandlerFile. Skipping to prevent overwrite."
    exit 0
}

# 1. Command Record
$CommandContent = @"
package com.atlashub.${Module}.application.commands.${CommandName};

public record ${CommandName}Command() {}
"@
Set-Content -Path $CommandFile -Value $CommandContent

# 2. Response Record — create whenever ResponseType is not void
$isVoid = ($ResponseType -eq "void" -or $ResponseType -eq "Void")

if (-not $isVoid) {
    if (Test-Path $ResponseFile) {
        Write-Host "WARNING: Response file already exists at $ResponseFile. Skipping."
    } else {
        $ResponseContent = @"
package com.atlashub.${Module}.application.commands.${CommandName};

public record ${CommandName}Response() {}
"@
        Set-Content -Path $ResponseFile -Value $ResponseContent
    }
}

# 3. Handler Class — no 'return null' for void
if ($isVoid) {
    $ReturnStatement = ""
    $ReturnType = "void"
} else {
    $ReturnStatement = "`n        return null; // TODO: replace with actual result"
    $ReturnType = $ResponseType
}

$HandlerContent = @"
package com.atlashub.${Module}.application.commands.${CommandName};

import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ${CommandName}Handler extends Command<${CommandName}Command, ${ReturnType}> {

    private static final Logger log = LoggerFactory.getLogger(${CommandName}Handler.class);

    public ${CommandName}Handler() {
        // TODO: Inject dependencies (Repositories, Ports)
    }

    @Override
    public ${ReturnType} execute(${CommandName}Command command) {
        log.info("Executing ${CommandName}Command");

        // TODO: Implement orchestration logic
        // DO NOT implement business logic here. Delegate to Entities, Domain Services, or Ports.$ReturnStatement
    }
}
"@
Set-Content -Path $HandlerFile -Value $HandlerContent

Write-Host "SUCCESS: Scaffolded Command, Handler$(if (-not $isVoid) { ', and Response' }) at $CommandDir"
