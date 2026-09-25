param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$CommandName,
    [Parameter(Mandatory=$true)][string]$ResponseType
)

$ErrorActionPreference = "Stop"

$ModulePath = "atlashub-platform\$Module\src\main\java\com\atlashub\$Module"
$CommandDir = "$ModulePath\application\commands\$CommandName"

if (-Not (Test-Path $CommandDir)) {
    New-Item -ItemType Directory -Force -Path $CommandDir | Out-Null
}

$CommandFile = "$CommandDir\${CommandName}Command.java"
$HandlerFile = "$CommandDir\${CommandName}Handler.java"

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

# 2. Response Record (if not Void)
$ResponseImport = ""
$ResponseGeneric = $ResponseType
if ($ResponseType -eq "${CommandName}Response") {
    $ResponseFile = "$CommandDir\${CommandName}Response.java"
    $ResponseContent = @"
package com.atlashub.${Module}.application.commands.${CommandName};

public record ${CommandName}Response() {}
"@
    Set-Content -Path $ResponseFile -Value $ResponseContent
}

# 3. Handler Class
$HandlerContent = @"
package com.atlashub.${Module}.application.commands.${CommandName};

import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ${CommandName}Handler extends Command<${CommandName}Command, ${ResponseGeneric}> {

    private static final Logger log = LoggerFactory.getLogger(${CommandName}Handler.class);

    public ${CommandName}Handler() {
        // TODO: Inject dependencies (Repositories, Ports)
    }

    @Override
    public ${ResponseGeneric} execute(${CommandName}Command command) {
        log.info("Executing ${CommandName}Command");
        
        // TODO: Implement orchestration logic
        // DO NOT implement business logic here. Delegate to Entities, Domain Services, or Ports.

        return null;
    }
}
"@
Set-Content -Path $HandlerFile -Value $HandlerContent

Write-Host "SUCCESS: Scaffolded Command, Handler, and Response at $CommandDir"
