param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$ListenerName,
    [Parameter(Mandatory=$true)][string]$Topic,
    [Parameter(Mandatory=$true)][string]$GroupId,
    [Parameter(Mandatory=$true)][string]$EventType
)

$ErrorActionPreference = "Stop"

# Dynamic module discovery
$FoundModulePath = Get-ChildItem -Path . -Recurse -Directory |
    Where-Object { $_.FullName -match [regex]::Escape("src\\main\\java\\com\\atlashub\\$Module") } |
    Select-Object -First 1

if (-not $FoundModulePath) {
    Write-Host "ERROR: Module '$Module' not found under any src\main\java\com\atlashub\$Module path."
    exit 1
}

$ModulePath   = $FoundModulePath.FullName
$ListenerDir  = Join-Path -Path $ModulePath -ChildPath "infrastructure\messaging\listeners"
$EventDir     = Join-Path -Path $ModulePath -ChildPath "infrastructure\messaging\events"

foreach ($dir in @($ListenerDir, $EventDir)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
}

$ListenerFile = Join-Path -Path $ListenerDir -ChildPath "${ListenerName}Listener.java"
$EventFile    = Join-Path -Path $EventDir    -ChildPath "${EventType}Payload.java"

if (Test-Path $ListenerFile) {
    Write-Host "ERROR: $ListenerFile already exists! Aborting to prevent overwrite."
    exit 1
}

if (Test-Path $EventFile) {
    Write-Host "ERROR: $EventFile already exists! Aborting to prevent overwrite."
    exit 1
}

# --- Listener class ---
$ListenerContent = @"
package com.atlashub.$Module.infrastructure.messaging.listeners;

import com.atlashub.$Module.infrastructure.messaging.events.${EventType}Payload;
import com.atlashub.shared.infrastructure.messaging.BaseKafkaEventListener;
import jakarta.annotation.PostConstruct;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ${ListenerName}Listener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(${ListenerName}Listener.class);

    public ${ListenerName}Listener() {
        // TODO: Inject required Command Handler(s) via constructor
    }

    @PostConstruct
    public void init() {
        log.info("${ListenerName}Listener initialized — topic: $Topic, group: $GroupId");
    }

    @KafkaListener(topics = "$Topic", groupId = "$GroupId")
    public void listen(String message) {
        processEventIfMatches(message, ${EventType}Payload.class, payload -> {
            // TODO: Build command from payload and call handler.execute(command)
        });
    }
}
"@

# --- Event payload record ---
$EventContent = @"
package com.atlashub.$Module.infrastructure.messaging.events;

public record ${EventType}Payload(
        String eventId
        // TODO: Add domain-specific fields here
) {}
"@

Set-Content -Path $ListenerFile -Value $ListenerContent
Write-Host "SUCCESS: Listener created at $ListenerFile"

Set-Content -Path $EventFile -Value $EventContent
Write-Host "SUCCESS: Event payload created at $EventFile"
