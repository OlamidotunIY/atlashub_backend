param (
    [Parameter(Mandatory=$true)][string]$Module,
    [Parameter(Mandatory=$true)][string]$SchedulerName,
    [Parameter(Mandatory=$true)][string]$CronExpression,
    [Parameter(Mandatory=$true)][string]$HandlerName
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
$TargetDir    = Join-Path -Path $ModulePath -ChildPath "infrastructure\schedulers"

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Force -Path $TargetDir | Out-Null
}

$TargetFile = Join-Path -Path $TargetDir -ChildPath "${SchedulerName}Scheduler.java"

if (Test-Path $TargetFile) {
    Write-Host "ERROR: $TargetFile already exists! Aborting to prevent overwrite."
    exit 1
}

$Content = @"
package com.atlashub.$Module.infrastructure.schedulers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

// TODO: Add import for $HandlerName and its command record

@Component
public class ${SchedulerName}Scheduler {

    private static final Logger log = LoggerFactory.getLogger(${SchedulerName}Scheduler.class);

    private final $HandlerName handler;

    public ${SchedulerName}Scheduler($HandlerName handler) {
        this.handler = handler;
    }

    @Scheduled(cron = "$CronExpression")
    public void run() {
        log.info("${SchedulerName}Scheduler triggered — cron: $CronExpression");
        // TODO: Replace with the correct command instantiation
        // handler.execute(new <CommandName>Command());
    }
}
"@

Set-Content -Path $TargetFile -Value $Content
Write-Host "SUCCESS: Scheduler created at $TargetFile"
