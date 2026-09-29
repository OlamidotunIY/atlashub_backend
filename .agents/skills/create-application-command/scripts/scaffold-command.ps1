param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$CommandName,
    [Parameter(Mandatory=$true)][string]$ResponseType,
    [Parameter(Mandatory=$false)][string]$SubModule = ""
)

$ErrorActionPreference = "Stop"

# Build the Java package sub-path to search for
# If SubModule is provided: com.atlashub.<Module>.<SubModule>
# Else:                     com.atlashub.<Module>
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

$isVoid = ($ResponseType -eq "void" -or $ResponseType -eq "Void")

# 1. Command Record
$CommandContent = @"
package $JavaPackage.application.commands.${CommandName};

public record ${CommandName}Command() {
    // TODO: Add command fields here
}
"@
Set-Content -Path $CommandFile -Value $CommandContent

# 2. Response Record — only when not void
if (-not $isVoid) {
    if (Test-Path $ResponseFile) {
        Write-Host "WARNING: Response file already exists at $ResponseFile. Skipping."
    } else {
        $ResponseContent = @"
package $JavaPackage.application.commands.${CommandName};

public record ${CommandName}Response() {
    // TODO: Add response fields here
}
"@
        Set-Content -Path $ResponseFile -Value $ResponseContent
    }
}

# 3. Handler Class
if ($isVoid) {
    $ReturnType    = "Void"
    $ReturnComment = "// TODO: Implement orchestration logic. Return null for Void handlers."
    $ReturnLine    = "        return null;"
} else {
    $ReturnType    = "${CommandName}Response"
    $ReturnComment = "// TODO: Implement orchestration logic and return the response."
    $ReturnLine    = "        // TODO: replace with actual result`n        throw new UnsupportedOperationException(`"${CommandName}Handler not implemented`");"
}

$HandlerContent = @"
package $JavaPackage.application.commands.${CommandName};

import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ${CommandName}Handler extends Command<${CommandName}Command, ${ReturnType}> {

    private static final Logger log = LoggerFactory.getLogger(${CommandName}Handler.class);

    public ${CommandName}Handler(
        // TODO: Inject repositories and ports
    ) {}

    @Override
    public ${ReturnType} execute(${CommandName}Command command) {
        log.info("Executing ${CommandName}Command");
        $ReturnComment
$ReturnLine
    }
}
"@
Set-Content -Path $HandlerFile -Value $HandlerContent

$createdFiles = "Command, Handler"
if (-not $isVoid) { $createdFiles += ", Response" }
Write-Host "SUCCESS: Scaffolded $createdFiles at $CommandDir"
